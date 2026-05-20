package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
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
 * Teller changes the editable subset of an account (only {@code LoaiTK}
 * per SRS 3.8). SoTaiKhoan / MaKH / SoDu / NgayMo / TrangThai are read-only.
 */
public class EditAccountViewModel extends ViewModel {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final TellerRepository tellers;
    private final AuditLogger audit;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<Account> loadedAccount = new MutableLiveData<>();

    public EditAccountViewModel(UserRepository users,
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
    public LiveData<Account> getLoadedAccount() { return loadedAccount; }

    public void load(int tellerUserId, String accountNumber) {
        ValidationResult v = Validators.validateAccountNumber(accountNumber);
        if (!v.isValid()) { fail(v); return; }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            Account acct = accounts.findByNumber(accountNumber);
            if (acct == null) {
                state.postValue(UiState.error("F33", "Account not found"));
                return;
            }
            User owner = users.findById(acct.getOwnerCustomerId());
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessAccount(
                    teller == null ? null : teller.getBranchId(),
                    owner == null ? null : owner.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F34", "Account is not in your branch"));
                return;
            }
            loadedAccount.postValue(acct);
            state.postValue(UiState.success("Loaded", acct));
        });
    }

    public void changeType(int tellerUserId, String accountNumber, AccountType newType) {
        if (newType == null) {
            state.setValue(UiState.error("T-TYPE", "Account type required"));
            return;
        }
        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            Account acct = accounts.findByNumber(accountNumber);
            if (acct == null) {
                state.postValue(UiState.error("F33", "Account not found"));
                return;
            }
            User owner = users.findById(acct.getOwnerCustomerId());
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessAccount(
                    teller == null ? null : teller.getBranchId(),
                    owner == null ? null : owner.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F34", "Account is not in your branch"));
                return;
            }
            // Account doesn't carry a settable type in our model — we create
            // a replacement Account row keyed on the same number. The DB
            // upsert reuses the PK, so the existing balance + counters
            // survive but the type is overwritten via SQL.
            Account updated = new Account(acct.getAccountNumber(), acct.getOwnerCustomerId(),
                    newType, acct.getBalance());
            updated.setActive(acct.isActive());
            updated.setMonthlyWithdrawCount(acct.getMonthlyWithdrawCount());
            updated.setMonthlyTransferCount(acct.getMonthlyTransferCount());
            updated.setCountersMonth(acct.getCountersMonth());
            accounts.save(updated);

            audit.log(tellerUserId, AuditLogger.ACTION_UPDATE, "accounts",
                    "{\"type\":\"" + acct.getType() + "\"}",
                    "{\"type\":\"" + newType + "\"}");

            state.postValue(UiState.success("Account type updated to " + newType, updated));
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
