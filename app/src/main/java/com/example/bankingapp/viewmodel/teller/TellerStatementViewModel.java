package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;
import com.example.bankingapp.viewmodel.UiState;

import java.util.Collections;
import java.util.List;

/**
 * Teller-side balance enquiry + mini/customized statement.
 * Gated by branch isolation on the account's owning customer.
 */
public class TellerStatementViewModel extends ViewModel {

    public static final int MINI_STATEMENT_SIZE = 5;

    private final UserRepository users;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TellerRepository tellers;
    private final AppExecutors executors;

    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<Account> accountInfo = new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> txnList =
            new MutableLiveData<>(Collections.emptyList());

    public TellerStatementViewModel(UserRepository users,
                                     AccountRepository accounts,
                                     TransactionRepository transactions,
                                     TellerRepository tellers,
                                     AppExecutors executors) {
        this.users = users;
        this.accounts = accounts;
        this.transactions = transactions;
        this.tellers = tellers;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }
    public LiveData<Account> getAccountInfo() { return accountInfo; }
    public LiveData<List<Transaction>> getTxnList() { return txnList; }

    public void loadBalance(int tellerUserId, String accountNumber) {
        ValidationResult v = Validators.validateAccountNumber(accountNumber);
        if (!v.isValid()) { fail(v); return; }
        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            Account a = accounts.findByNumber(accountNumber);
            if (a == null) {
                state.postValue(UiState.error("F2", "Account not found"));
                return;
            }
            User owner = users.findById(a.getOwnerCustomerId());
            if (BusinessRules.tellerCanAccessAccount(
                    teller == null ? null : teller.getBranchId(),
                    owner == null ? null : owner.getBranchId())
                    != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F1", "Account is not in your branch"));
                return;
            }
            accountInfo.postValue(a);
            state.postValue(UiState.success("Loaded", a));
        });
    }

    public void loadMini(int tellerUserId, String accountNumber) {
        ValidationResult v = Validators.validateAccountNumber(accountNumber);
        if (!v.isValid()) { fail(v); return; }
        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            Account a = accounts.findByNumber(accountNumber);
            if (a == null) {
                state.postValue(UiState.error("F45", "Account not found"));
                return;
            }
            User owner = users.findById(a.getOwnerCustomerId());
            if (BusinessRules.tellerCanAccessAccount(
                    teller == null ? null : teller.getBranchId(),
                    owner == null ? null : owner.getBranchId())
                    != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F47", "Account is not in your branch"));
                return;
            }
            List<Transaction> mini = transactions.findLastN(accountNumber, MINI_STATEMENT_SIZE);
            txnList.postValue(mini);
            state.postValue(UiState.success(
                    mini.isEmpty() ? "F46: no transactions" : "Loaded " + mini.size() + " entries",
                    mini));
        });
    }

    public void loadCustomized(int tellerUserId, String accountNumber,
                                long fromMillis, long toMillis,
                                long amountLowerLimit, int maxCount) {
        ValidationResult v = Validators.validateAccountNumber(accountNumber);
        if (!v.isValid()) { fail(v); return; }
        if (fromMillis > toMillis) {
            state.setValue(UiState.error("F41", "From-date must be ≤ to-date"));
            txnList.setValue(Collections.emptyList());
            return;
        }
        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            Account a = accounts.findByNumber(accountNumber);
            if (a == null) {
                state.postValue(UiState.error("F40", "Account not found"));
                return;
            }
            User owner = users.findById(a.getOwnerCustomerId());
            if (BusinessRules.tellerCanAccessAccount(
                    teller == null ? null : teller.getBranchId(),
                    owner == null ? null : owner.getBranchId())
                    != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F47", "Account is not in your branch"));
                return;
            }
            List<Transaction> result = transactions.findCustomized(
                    accountNumber, fromMillis, toMillis, amountLowerLimit, maxCount);
            txnList.postValue(result);
            state.postValue(UiState.success("Found " + result.size() + " entries", result));
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
