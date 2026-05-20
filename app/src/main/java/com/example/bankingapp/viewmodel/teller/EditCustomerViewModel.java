package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;
import com.example.bankingapp.viewmodel.UiState;

/**
 * Teller edits the editable subset of a customer (SRS 3.8 — Address, City,
 * State, PIN, Phone, Email, DailyLimit). Read-only fields (Name, Gender,
 * DOB, CMND) are NOT modifiable.
 */
public class EditCustomerViewModel extends ViewModel {

    private final UserRepository users;
    private final TellerRepository tellers;
    private final AuditLogger audit;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<User> loadedCustomer = new MutableLiveData<>();

    public EditCustomerViewModel(UserRepository users,
                                  TellerRepository tellers,
                                  AuditLogger audit,
                                  AppExecutors executors) {
        this.users = users;
        this.tellers = tellers;
        this.audit = audit;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }
    public LiveData<User> getLoadedCustomer() { return loadedCustomer; }

    /** Step 1: lookup the customer to populate the edit form. */
    public void load(int tellerUserId, String customerIdStr) {
        ValidationResult v = Validators.validateCustomerId(customerIdStr);
        if (!v.isValid()) { fail(v); return; }

        int customerId;
        try { customerId = Integer.parseInt(customerIdStr); }
        catch (NumberFormatException nfe) { state.setValue(UiState.error("T64", "Invalid id")); return; }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            User target = users.findById(customerId);
            if (target == null) {
                state.postValue(UiState.error("F24", "Customer not found"));
                return;
            }
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessCustomer(
                    teller == null ? null : teller.getBranchId(), target.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F25", "Customer is not in your branch"));
                return;
            }
            loadedCustomer.postValue(target);
            state.postValue(UiState.success("Loaded", target));
        });
    }

    /** Step 2: save edits. Inputs already validated by Activity if desired. */
    public void save(int tellerUserId,
                     int customerId,
                     String address,
                     String city,
                     String stateName,
                     String pin,
                     String phone,
                     String email,
                     long dailyLimit) {
        ValidationResult v;
        if (!(v = Validators.validateAddress(address)).isValid()) { fail(v); return; }
        if (!(v = Validators.validateCity(city)).isValid())       { fail(v); return; }
        if (!(v = Validators.validateState(stateName)).isValid()) { fail(v); return; }
        if (!(v = Validators.validatePin(pin)).isValid())         { fail(v); return; }
        if (!(v = Validators.validatePhone(phone)).isValid())     { fail(v); return; }
        if (!(v = Validators.validateEmail(email)).isValid())     { fail(v); return; }
        if (dailyLimit <= 0 || dailyLimit > 3_000_000_000L) {
            state.setValue(UiState.error("T35", "Daily limit must be positive and ≤ 3,000,000,000"));
            return;
        }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            User target = users.findById(customerId);
            if (target == null) {
                state.postValue(UiState.error("F24", "Customer not found"));
                return;
            }
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessCustomer(
                    teller == null ? null : teller.getBranchId(), target.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F25", "Customer is not in your branch"));
                return;
            }
            // F23: email collision with a different customer.
            User dup = users.findByEmail(email);
            if (dup != null && dup.getCustomerId() != customerId) {
                state.postValue(UiState.error("F23", "Email already used by another customer"));
                return;
            }

            String beforeJson = "{\"email\":\"" + safe(target.getEmail())
                    + "\",\"phone\":\"" + safe(target.getPhone())
                    + "\",\"daily_limit\":" + target.getDailyLimit() + "}";

            target.setAddress(address);
            target.setCity(city);
            target.setState(stateName);
            target.setPin(pin);
            target.setPhone(phone);
            target.setEmail(email);
            target.setDailyLimit(dailyLimit);
            users.save(target);

            String afterJson = "{\"email\":\"" + safe(email)
                    + "\",\"phone\":\"" + safe(phone)
                    + "\",\"daily_limit\":" + dailyLimit + "}";

            audit.log(tellerUserId, AuditLogger.ACTION_UPDATE, "users", beforeJson, afterJson);
            state.postValue(UiState.success("Customer updated", target));
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
