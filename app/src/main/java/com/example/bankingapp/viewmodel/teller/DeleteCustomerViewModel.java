package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.UserStatus;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;
import com.example.bankingapp.viewmodel.UiState;

import java.util.List;

/**
 * Teller "deletes" a customer (per SRS the row stays in DB for audit;
 * we mark the User status DISABLED). Only allowed when the customer has
 * no active accounts (F28).
 */
public class DeleteCustomerViewModel extends ViewModel {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final TellerRepository tellers;
    private final AuditLogger audit;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public DeleteCustomerViewModel(UserRepository users,
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

    public void delete(int tellerUserId, String customerIdStr) {
        ValidationResult v = Validators.validateCustomerId(customerIdStr);
        if (!v.isValid()) { fail(v); return; }
        int customerId;
        try { customerId = Integer.parseInt(customerIdStr); }
        catch (NumberFormatException nfe) { state.setValue(UiState.error("T51", "Invalid id")); return; }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            User target = users.findById(customerId);
            if (target == null) {
                state.postValue(UiState.error("F27", "Customer not found"));
                return;
            }
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessCustomer(
                    teller == null ? null : teller.getBranchId(), target.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F29", "Customer is not in your branch"));
                return;
            }

            // F28: any active account → block.
            List<Account> owned = accounts.findByOwner(customerId);
            for (Account a : owned) {
                if (a.isActive()) {
                    state.postValue(UiState.error("F28", "Customer still has active accounts"));
                    return;
                }
            }

            target.setStatus(UserStatus.DISABLED);
            users.save(target);
            audit.log(tellerUserId, AuditLogger.ACTION_DELETE, "users",
                    "{\"customer_id\":" + customerId + "}", null);

            state.postValue(UiState.success("Customer " + customerId + " deleted", customerId));
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
