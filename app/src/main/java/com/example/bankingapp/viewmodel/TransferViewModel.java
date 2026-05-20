package com.example.bankingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.OtpChallenge;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.model.enums.TransactionStatus;
import com.example.bankingapp.model.enums.TransactionType;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.OtpGenerator;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;

/**
 * Two-phase transfer state machine.
 *
 *   IDLE
 *     │  submitDetails(...)        — DB lookups + F9..F13 + F55 → io()
 *     ▼
 *   OTP_SENT
 *     │  submitOtp(...)            — SRS 3.5 OTP rules (no DB)
 *     │   ├─ wrong → still OTP_SENT
 *     │   ├─ 3 wrongs → FAILED
 *     │   └─ correct → executeAtomic() on io() → COMPLETED
 *     ▼
 *   COMPLETED  /  FAILED
 */
public class TransferViewModel extends ViewModel {

    public enum Phase { IDLE, OTP_SENT, COMPLETED, FAILED }

    private final UserRepository users;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final OtpGenerator otpGenerator;
    private final AppExecutors executors;

    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<Phase> phase = new MutableLiveData<>(Phase.IDLE);
    private final MutableLiveData<String> generatedOtp = new MutableLiveData<>();

    private OtpChallenge pendingChallenge;
    private String pendingSrc;
    private String pendingDst;
    private long pendingAmount;
    private String pendingDescription;
    private int pendingCustomerId;

    public TransferViewModel(UserRepository users,
                              AccountRepository accounts,
                              TransactionRepository transactions,
                              OtpGenerator otpGenerator,
                              AppExecutors executors) {
        this.users = users;
        this.accounts = accounts;
        this.transactions = transactions;
        this.otpGenerator = otpGenerator;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }
    public LiveData<Phase> getPhase() { return phase; }
    public LiveData<String> getGeneratedOtp() { return generatedOtp; }

    /* ------------------------------------------------------------------ */
    /* Phase 1: validate inputs + check business rules + issue OTP        */
    /* ------------------------------------------------------------------ */
    public void submitDetails(int customerId,
                               String sourceAccount,
                               String destAccount,
                               String amountStr,
                               String description) {
        submitDetails(customerId, sourceAccount, destAccount, amountStr, description, System.currentTimeMillis());
    }

    public void submitDetails(int customerId,
                               String sourceAccount,
                               String destAccount,
                               String amountStr,
                               String description,
                               long nowMillis) {
        ValidationResult v;
        if (!(v = Validators.validateAccountNumber(sourceAccount)).isValid()) { fail(v); return; }
        if (!(v = Validators.validateAccountNumber(destAccount)).isValid())   { fail(v); return; }
        if (!(v = Validators.validateAmount(amountStr)).isValid())            { fail(v); return; }
        if (!(v = Validators.validateDescription(description)).isValid())     { fail(v); return; }

        long amount = Validators.parseAmount(amountStr);

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Account src = accounts.findByNumber(sourceAccount);
            Account dst = accounts.findByNumber(destAccount);
            User user = users.findById(customerId);
            long dailyLimit = (user == null) ? Long.MAX_VALUE : user.getDailyLimit();

            long todayStart = BusinessRules.startOfUtcDay(nowMillis);
            long todayEnd = todayStart + 24L * 60L * 60L * 1000L - 1L;
            long todaysTransferTotal = transactions.sumTransfersOut(sourceAccount, todayStart, todayEnd);
            int month = BusinessRules.monthOf(nowMillis);

            BusinessRules.TransferResult result = BusinessRules.canTransfer(
                    src, dst, customerId, amount, 0L, dailyLimit, todaysTransferTotal, month);
            if (result != BusinessRules.TransferResult.SUCCESS) {
                state.postValue(UiState.error(result.name(), errorMessage(result)));
                phase.postValue(Phase.FAILED);
                return;
            }

            String code = otpGenerator.generate();
            pendingChallenge = new OtpChallenge(code, nowMillis + OtpChallenge.VALIDITY_MILLIS);
            pendingSrc = sourceAccount;
            pendingDst = destAccount;
            pendingAmount = amount;
            pendingDescription = description;
            pendingCustomerId = customerId;

            generatedOtp.postValue(code);
            phase.postValue(Phase.OTP_SENT);
            state.postValue(UiState.success("OTP sent. Code is displayed for testing purposes.", code));
        });
    }

    /* ------------------------------------------------------------------ */
    /* Phase 2: validate OTP + atomically apply the transfer              */
    /* ------------------------------------------------------------------ */
    public void submitOtp(String input) {
        submitOtp(input, System.currentTimeMillis());
    }

    public void submitOtp(String input, long nowMillis) {
        if (pendingChallenge == null) {
            state.setValue(UiState.error("NO_CHALLENGE", "No pending OTP. Start a new transfer."));
            phase.setValue(Phase.FAILED);
            return;
        }

        BusinessRules.OtpResult result = BusinessRules.validateOtp(pendingChallenge, input, nowMillis);
        switch (result) {
            case EXPIRED:
                state.setValue(UiState.error("OTP_EXPIRED", "OTP expired. Please start a new transfer."));
                clearPending();
                phase.setValue(Phase.FAILED);
                return;
            case ATTEMPTS_EXHAUSTED:
                state.setValue(UiState.error("OTP_ATTEMPTS_EXHAUSTED",
                        "Too many wrong OTP attempts. Transfer cancelled."));
                clearPending();
                phase.setValue(Phase.FAILED);
                return;
            case WRONG_CODE:
                int left = OtpChallenge.MAX_WRONG_ATTEMPTS - pendingChallenge.getWrongAttempts();
                state.setValue(UiState.error("OTP_WRONG", "Wrong OTP. Attempts left: " + left));
                return;
            case SUCCESS:
                state.setValue(UiState.loading());
                executors.io().execute(() -> executeAtomic(nowMillis));
                return;
            default:
                state.setValue(UiState.error("UNKNOWN", "OTP validation failed"));
                phase.setValue(Phase.FAILED);
        }
    }

    public void resendOtp() {
        resendOtp(System.currentTimeMillis());
    }

    public void resendOtp(long nowMillis) {
        if (pendingChallenge == null) {
            state.setValue(UiState.error("NO_CHALLENGE", "No pending OTP"));
            return;
        }
        if (pendingChallenge.getResends() >= OtpChallenge.MAX_RESENDS) {
            state.setValue(UiState.error("OTP_RESEND_LIMIT", "OTP resend limit reached"));
            return;
        }
        String code = otpGenerator.generate();
        pendingChallenge = new OtpChallenge(code, nowMillis + OtpChallenge.VALIDITY_MILLIS);
        pendingChallenge.incrementResends();
        generatedOtp.setValue(code);
        state.setValue(UiState.success("New OTP sent", code));
    }

    private void executeAtomic(long nowMillis) {
        Account src = accounts.findByNumber(pendingSrc);
        Account dst = accounts.findByNumber(pendingDst);

        if (src == null || dst == null || src.getBalance() < pendingAmount) {
            state.postValue(UiState.error("RACE_CONDITION", "Transfer could not be completed. Please retry."));
            clearPending();
            phase.postValue(Phase.FAILED);
            return;
        }

        long srcBeforeBal = src.getBalance();
        long dstBeforeBal = dst.getBalance();
        try {
            src.setBalance(srcBeforeBal - pendingAmount);
            dst.setBalance(dstBeforeBal + pendingAmount);

            int month = BusinessRules.monthOf(nowMillis);
            if (src.getType() == AccountType.SAVING) {
                if (src.getCountersMonth() != month) {
                    src.setCountersMonth(month);
                    src.setMonthlyWithdrawCount(0);
                    src.setMonthlyTransferCount(0);
                }
                src.setMonthlyTransferCount(src.getMonthlyTransferCount() + 1);
            }
            accounts.save(src);
            accounts.save(dst);

            Transaction saved = transactions.save(new Transaction(
                    0L,
                    pendingSrc,
                    pendingDst,
                    pendingAmount,
                    0L,
                    TransactionType.TRANSFER,
                    pendingDescription,
                    nowMillis,
                    pendingCustomerId,
                    TransactionStatus.SUCCESS,
                    src.getBalance()));

            state.postValue(UiState.success("Transfer completed", saved));
            phase.postValue(Phase.COMPLETED);
        } catch (RuntimeException re) {
            // Best-effort rollback of in-memory state. The DB UPDATE has not been
            // wrapped in a JDBC transaction yet — adding that is a TODO once the
            // app moves beyond the testing-course scope.
            src.setBalance(srcBeforeBal);
            dst.setBalance(dstBeforeBal);
            try {
                accounts.save(src);
                accounts.save(dst);
            } catch (RuntimeException ignored) { }
            state.postValue(UiState.error("ROLLBACK", "Transfer failed. Changes rolled back."));
            phase.postValue(Phase.FAILED);
        } finally {
            clearPending();
        }
    }

    private void clearPending() {
        pendingChallenge = null;
        pendingSrc = pendingDst = pendingDescription = null;
        pendingAmount = 0L;
        pendingCustomerId = 0;
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
        phase.setValue(Phase.FAILED);
    }

    private static String errorMessage(BusinessRules.TransferResult r) {
        switch (r) {
            case SRC_NOT_FOUND:                  return "Source account not found";
            case DST_NOT_FOUND:                  return "Destination account not found";
            case SRC_INACTIVE:                   return "Source account is not active";
            case DST_INACTIVE:                   return "Destination account is not active";
            case SAME_ACCOUNT:                   return "Source and destination must differ";
            case NOT_OWNER:                      return "You do not own the source account";
            case INSUFFICIENT_FUNDS:             return "Insufficient funds";
            case DAILY_LIMIT_EXCEEDED:           return "Daily transfer limit exceeded";
            case SAVING_MONTHLY_LIMIT_REACHED:   return "Monthly transfer limit reached for savings account";
            case SAVING_BALANCE_FLOOR_VIOLATED:  return "Savings account must keep a minimum balance of 500 VND";
            case INVALID_AMOUNT:                 return "Amount must be greater than zero";
            default:                             return "Transfer failed";
        }
    }
}
