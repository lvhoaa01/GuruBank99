package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.UserRole;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.utils.CustomerIdGenerator;
import com.example.bankingapp.utils.PasswordHasher;
import com.example.bankingapp.utils.TempPasswordGenerator;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;
import com.example.bankingapp.viewmodel.UiState;

/**
 * Teller-side: create a brand new customer.
 *
 * Output payload (per SRS 3.2) is a {@link NewCustomerResult} carrying the
 * generated MaKH + Username + one-time temporary password so the Activity
 * can display them once.
 */
public class NewCustomerViewModel extends ViewModel {

    public static final class NewCustomerResult {
        public final int customerId;
        public final String username;
        public final String tempPassword;
        public NewCustomerResult(int id, String u, String p) {
            this.customerId = id; this.username = u; this.tempPassword = p;
        }
    }

    private final UserRepository users;
    private final TellerRepository tellers;
    private final CustomerIdGenerator customerIds;
    private final TempPasswordGenerator passwordGen;
    private final AuditLogger audit;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public NewCustomerViewModel(UserRepository users,
                                 TellerRepository tellers,
                                 CustomerIdGenerator customerIds,
                                 TempPasswordGenerator passwordGen,
                                 AuditLogger audit,
                                 AppExecutors executors) {
        this.users = users;
        this.tellers = tellers;
        this.customerIds = customerIds;
        this.passwordGen = passwordGen;
        this.audit = audit;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }

    public void create(int tellerUserId,
                       String fullName,
                       String idNumber,
                       String dateOfBirth,
                       String address,
                       String city,
                       String stateName,
                       String pin,
                       String phone,
                       String email,
                       long dailyLimit) {
        ValidationResult v;
        if (!(v = Validators.validateCustomerName(fullName)).isValid())   { fail(v); return; }
        if (!(v = Validators.validateIdNumber(idNumber)).isValid())       { fail(v); return; }
        if (!(v = Validators.validateDateOfBirth(dateOfBirth)).isValid()) { fail(v); return; }
        if (!(v = Validators.validateAddress(address)).isValid())         { fail(v); return; }
        if (!(v = Validators.validateCity(city)).isValid())               { fail(v); return; }
        if (!(v = Validators.validateState(stateName)).isValid())         { fail(v); return; }
        if (!(v = Validators.validatePin(pin)).isValid())                 { fail(v); return; }
        if (!(v = Validators.validatePhone(phone)).isValid())             { fail(v); return; }
        if (!(v = Validators.validateEmail(email)).isValid())             { fail(v); return; }
        if (dailyLimit <= 0 || dailyLimit > 3_000_000_000L) {
            state.setValue(UiState.error("T35", "Daily limit must be positive and ≤ 3,000,000,000"));
            return;
        }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            if (teller == null) {
                state.postValue(UiState.error("AUTH", "Teller not found"));
                return;
            }
            if (users.findByEmail(email) != null) {
                state.postValue(UiState.error("F21", "Email is already registered"));
                return;
            }
            if (users.findByIdNumber(idNumber) != null) {
                state.postValue(UiState.error("F22", "ID number is already registered"));
                return;
            }

            int customerId = customerIds.next();
            // ID collision avoidance — bump beyond any existing customer.
            while (users.findById(customerId) != null) {
                customerId = customerIds.next();
            }
            String username = "C" + customerId;
            String tempPassword = passwordGen.generate();

            User user = new User(customerId, username, PasswordHasher.hash(tempPassword));
            user.setFullName(fullName);
            user.setIdNumber(idNumber);
            user.setDateOfBirth(dateOfBirth);
            user.setAddress(address);
            user.setCity(city);
            user.setState(stateName);
            user.setPin(pin);
            user.setPhone(phone);
            user.setEmail(email);
            user.setDailyLimit(dailyLimit);
            user.setRole(UserRole.CUSTOMER);
            user.setBranchId(teller.getBranchId());      // new customer joins teller's branch
            users.save(user);

            audit.log(tellerUserId, AuditLogger.ACTION_CREATE, "users", null,
                    "{\"customer_id\":" + customerId + ",\"branch_id\":\"" + teller.getBranchId() + "\"}");

            state.postValue(UiState.success(
                    "Customer created. Show the temporary password to the customer.",
                    new NewCustomerResult(customerId, username, tempPassword)));
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
