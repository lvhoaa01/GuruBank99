package com.example.bankingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AccountNumberGenerator;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.CustomerIdGenerator;
import com.example.bankingapp.utils.PasswordHasher;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;

public class RegisterViewModel extends ViewModel {

    public static final long MIN_INITIAL_DEPOSIT = 500L;

    private final UserRepository users;
    private final AccountRepository accounts;
    private final CustomerIdGenerator customerIds;
    private final AccountNumberGenerator accountNumbers;
    private final AppExecutors executors;

    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public RegisterViewModel(UserRepository users,
                             AccountRepository accounts,
                             CustomerIdGenerator customerIds,
                             AccountNumberGenerator accountNumbers,
                             AppExecutors executors) {
        this.users = users;
        this.accounts = accounts;
        this.customerIds = customerIds;
        this.accountNumbers = accountNumbers;
        this.executors = executors;
    }

    public LiveData<UiState> getState() {
        return state;
    }

    public void register(String fullName,
                         String idNumber,
                         String dateOfBirth,
                         String address,
                         String city,
                         String stateName,
                         String pin,
                         String phone,
                         String email,
                         String password,
                         String initialDepositStr) {

        // Pure validation on the calling thread — no DB hit.
        ValidationResult v;
        if (!(v = Validators.validateCustomerName(fullName)).isValid())  { fail(v); return; }
        if (!(v = Validators.validateIdNumber(idNumber)).isValid())      { fail(v); return; }
        if (!(v = Validators.validateDateOfBirth(dateOfBirth)).isValid()){ fail(v); return; }
        if (!(v = Validators.validateAddress(address)).isValid())        { fail(v); return; }
        if (!(v = Validators.validateCity(city)).isValid())              { fail(v); return; }
        if (!(v = Validators.validateState(stateName)).isValid())        { fail(v); return; }
        if (!(v = Validators.validatePin(pin)).isValid())                { fail(v); return; }
        if (!(v = Validators.validatePhone(phone)).isValid())            { fail(v); return; }
        if (!(v = Validators.validateEmail(email)).isValid())            { fail(v); return; }
        if (!(v = Validators.validateNewPassword(password)).isValid())   { fail(v); return; }
        if (!(v = Validators.validateAmount(initialDepositStr)).isValid()) { fail(v); return; }

        long initial = Validators.parseAmount(initialDepositStr);
        if (initial < MIN_INITIAL_DEPOSIT) {
            state.setValue(UiState.error("F31",
                    "Initial deposit must be at least " + MIN_INITIAL_DEPOSIT + " VND"));
            return;
        }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            if (users.findByEmail(email) != null) {
                state.postValue(UiState.error("F21", "Email is already registered"));
                return;
            }
            if (users.findByIdNumber(idNumber) != null) {
                state.postValue(UiState.error("F22", "ID number is already registered"));
                return;
            }

            int customerId = customerIds.next();
            String username = "C" + customerId;
            User user = new User(customerId, username, PasswordHasher.hash(password));
            user.setFullName(fullName);
            user.setIdNumber(idNumber);
            user.setDateOfBirth(dateOfBirth);
            user.setAddress(address);
            user.setCity(city);
            user.setState(stateName);
            user.setPin(pin);
            user.setPhone(phone);
            user.setEmail(email);
            users.save(user);

            String acctNum = accountNumbers.next();
            Account openingAccount = new Account(acctNum, customerId, AccountType.SAVING, initial);
            accounts.save(openingAccount);

            state.postValue(UiState.success("Registration successful. Your username: " + username, username));
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
