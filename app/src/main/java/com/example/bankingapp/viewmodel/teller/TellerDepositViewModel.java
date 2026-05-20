package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.User;
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
 * Teller deposits cash on behalf of a customer (F18, F19, F20).
 * Mã Teller is recorded as {@code performedByUserId} on the transaction
 * row so the deposit can be audited later (F20).
 */
public class TellerDepositViewModel extends ViewModel {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TellerRepository tellers;
    private final AuditLogger audit;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public TellerDepositViewModel(UserRepository users,
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

    public void deposit(int tellerUserId, String accountNumber, String amountStr, String description) {
        deposit(tellerUserId, accountNumber, amountStr, description, System.currentTimeMillis());
    }

    public void deposit(int tellerUserId, String accountNumber, String amountStr, String description, long nowMillis) {
        ValidationResult v;
        if (!(v = Validators.validateAccountNumber(accountNumber)).isValid()) { fail(v); return; }
        if (!(v = Validators.validateAmount(amountStr)).isValid())            { fail(v); return; }
        if (!(v = Validators.validateDescription(description)).isValid())     { fail(v); return; }
        long amount = Validators.parseAmount(amountStr);

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            Account account = accounts.findByNumber(accountNumber);
            if (account == null) {
                state.postValue(UiState.error("F18", "Account not found"));
                return;
            }
            User owner = users.findById(account.getOwnerCustomerId());
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessAccount(
                    teller == null ? null : teller.getBranchId(),
                    owner == null ? null : owner.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F19", "Account is not in your branch"));
                return;
            }
            BusinessRules.DepositResult result = BusinessRules.canDeposit(account, amount);
            if (result != BusinessRules.DepositResult.SUCCESS) {
                state.postValue(UiState.error(result.name(), errorMessage(result)));
                return;
            }

            account.setBalance(account.getBalance() + amount);
            accounts.save(account);

            Transaction saved = transactions.save(new Transaction(
                    0L, accountNumber, null, amount, 0L,
                    TransactionType.DEPOSIT, description, nowMillis,
                    tellerUserId,                         // F20: ghi mã Teller
                    TransactionStatus.SUCCESS, account.getBalance()));

            audit.log(tellerUserId, AuditLogger.ACTION_DEPOSIT, "transactions", null,
                    "{\"txn_id\":" + saved.getTransactionId()
                            + ",\"account\":\"" + accountNumber
                            + "\",\"amount\":" + amount + "}");

            state.postValue(UiState.success("Deposit successful", saved));
        });
    }

    private static String errorMessage(BusinessRules.DepositResult r) {
        switch (r) {
            case ACCOUNT_NOT_FOUND: return "Account not found";
            case ACCOUNT_INACTIVE:  return "Account is not active";
            case INVALID_AMOUNT:    return "Amount must be greater than zero";
            default:                return "Deposit failed";
        }
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
