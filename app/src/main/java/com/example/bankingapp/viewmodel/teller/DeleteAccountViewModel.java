package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;
import com.example.bankingapp.viewmodel.UiState;

/**
 * Close an account: only when balance = 0 (F36) and within the teller's
 * branch (F37). We flip {@link Account#setActive(boolean)} to false rather
 * than physically deleting so transaction history survives.
 */
public class DeleteAccountViewModel extends ViewModel {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final TellerRepository tellers;
    private final AuditLogger audit;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public DeleteAccountViewModel(UserRepository users,
                                   AccountRepository accounts,
                                   TellerRepository tellers,
                                   AuditLogger audit,
                                   AppExecutors executors) {
        this.users = users;
        this.accounts = accounts;
        this.tellers = tellers;
        this.audit = audit;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }

    public void delete(int tellerUserId, String accountNumber) {
        ValidationResult v = Validators.validateAccountNumber(accountNumber);
        if (!v.isValid()) { fail(v); return; }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            Account acct = accounts.findByNumber(accountNumber);
            if (acct == null) {
                state.postValue(UiState.error("F35", "Account not found"));
                return;
            }
            User owner = users.findById(acct.getOwnerCustomerId());
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessAccount(
                    teller == null ? null : teller.getBranchId(),
                    owner == null ? null : owner.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F37", "Account is not in your branch"));
                return;
            }
            if (acct.getBalance() != 0L) {
                state.postValue(UiState.error("F36", "Balance must be zero before closing"));
                return;
            }

            acct.setActive(false);
            accounts.save(acct);
            audit.log(tellerUserId, AuditLogger.ACTION_DELETE, "accounts",
                    "{\"account\":\"" + accountNumber + "\",\"active\":true}",
                    "{\"account\":\"" + accountNumber + "\",\"active\":false}");

            state.postValue(UiState.success("Account " + accountNumber + " closed", acct));
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
