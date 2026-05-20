package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.model.enums.TransactionStatus;
import com.example.bankingapp.model.enums.TransactionType;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;
import com.example.bankingapp.viewmodel.UiState;

/**
 * Teller transfer (SRS 3.3.2). NO OTP step (per SRS 3.5 — teller already
 * authenticated at the counter). F5–F8 plus the standard transfer rules
 * still apply, gated by branch isolation on the source account.
 */
public class TellerTransferViewModel extends ViewModel {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TellerRepository tellers;
    private final AuditLogger audit;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public TellerTransferViewModel(UserRepository users,
                                    AccountRepository accounts,
                                    TransactionRepository transactions,
                                    TellerRepository tellers,
                                    AuditLogger audit,
                                    AppExecutors executors) {
        this.users = users;
        this.accounts = accounts;
        this.transactions = transactions;
        this.tellers = tellers;
        this.audit = audit;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }

    public void transfer(int tellerUserId, String sourceAccount, String destAccount,
                         String amountStr, String description) {
        transfer(tellerUserId, sourceAccount, destAccount, amountStr, description,
                System.currentTimeMillis());
    }

    public void transfer(int tellerUserId, String sourceAccount, String destAccount,
                         String amountStr, String description, long nowMillis) {
        ValidationResult v;
        if (!(v = Validators.validateAccountNumber(sourceAccount)).isValid()) { fail(v); return; }
        if (!(v = Validators.validateAccountNumber(destAccount)).isValid())   { fail(v); return; }
        if (!(v = Validators.validateAmount(amountStr)).isValid())            { fail(v); return; }
        if (!(v = Validators.validateDescription(description)).isValid())     { fail(v); return; }
        long amount = Validators.parseAmount(amountStr);

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            Account src = accounts.findByNumber(sourceAccount);
            Account dst = accounts.findByNumber(destAccount);
            if (src == null) {
                state.postValue(UiState.error("F5", "Source account not found"));
                return;
            }
            if (dst == null) {
                state.postValue(UiState.error("F5", "Destination account not found"));
                return;
            }
            // F8: source must belong to teller's branch.
            User srcOwner = users.findById(src.getOwnerCustomerId());
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessAccount(
                    teller == null ? null : teller.getBranchId(),
                    srcOwner == null ? null : srcOwner.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F8", "Source account is not in your branch"));
                return;
            }
            if (src.getAccountNumber().equals(dst.getAccountNumber())) {
                state.postValue(UiState.error("F6", "Source and destination must differ"));
                return;
            }
            // For Teller, daily limit & ownership-by-customer don't apply (teller
            // acts on behalf of any customer in his branch). Saving floor + monthly
            // counter still apply on the source.
            int month = BusinessRules.monthOf(nowMillis);
            if (amount <= 0) { state.postValue(UiState.error("INVALID_AMOUNT", "Amount must be > 0")); return; }
            if (src.getBalance() < amount) {
                state.postValue(UiState.error("F7", "Insufficient funds"));
                return;
            }
            if (src.getType() == AccountType.SAVING) {
                int monthlyTransfers = (src.getCountersMonth() == month)
                        ? src.getMonthlyTransferCount() : 0;
                if (monthlyTransfers >= Account.MAX_SAVING_TRANSFERS_PER_MONTH) {
                    state.postValue(UiState.error("SAVING_MONTHLY_LIMIT_REACHED",
                            "Monthly transfer limit reached for savings account"));
                    return;
                }
                if (src.getBalance() - amount < Account.MIN_SAVING_BALANCE) {
                    state.postValue(UiState.error("SAVING_BALANCE_FLOOR_VIOLATED",
                            "Savings account must keep a minimum balance of 500 VND"));
                    return;
                }
            }

            long srcBeforeBal = src.getBalance();
            long dstBeforeBal = dst.getBalance();
            try {
                src.setBalance(srcBeforeBal - amount);
                dst.setBalance(dstBeforeBal + amount);
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
                        0L, sourceAccount, destAccount, amount, 0L,
                        TransactionType.TRANSFER, description, nowMillis,
                        tellerUserId,                       // NguoiThucHien = Teller
                        TransactionStatus.SUCCESS, src.getBalance()));

                audit.log(tellerUserId, AuditLogger.ACTION_TRANSFER, "transactions", null,
                        "{\"txn_id\":" + saved.getTransactionId()
                                + ",\"src\":\"" + sourceAccount + "\",\"dst\":\"" + destAccount
                                + "\",\"amount\":" + amount + "}");

                state.postValue(UiState.success("Transfer completed", saved));
            } catch (RuntimeException re) {
                src.setBalance(srcBeforeBal);
                dst.setBalance(dstBeforeBal);
                try { accounts.save(src); accounts.save(dst); } catch (RuntimeException ignored) {}
                state.postValue(UiState.error("ROLLBACK", "Transfer failed. Changes rolled back."));
            }
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
