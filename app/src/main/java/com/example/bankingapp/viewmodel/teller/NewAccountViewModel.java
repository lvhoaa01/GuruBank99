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
import com.example.bankingapp.utils.AccountNumberGenerator;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;
import com.example.bankingapp.viewmodel.UiState;

/**
 * Teller opens a new account for an existing customer (SRS 3.1 New Account).
 * F30: customer must exist; F31: initial deposit ≥ 500; F32: customer in
 * teller's branch.
 */
public class NewAccountViewModel extends ViewModel {

    public static final long MIN_INITIAL_DEPOSIT = 500L;

    private final UserRepository users;
    private final AccountRepository accounts;
    private final TellerRepository tellers;
    private final AccountNumberGenerator accountNumbers;
    private final AuditLogger audit;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public NewAccountViewModel(UserRepository users,
                                AccountRepository accounts,
                                TellerRepository tellers,
                                AccountNumberGenerator accountNumbers,
                                AuditLogger audit,
                                AppExecutors executors) {
        this.users = users;
        this.accounts = accounts;
        this.tellers = tellers;
        this.accountNumbers = accountNumbers;
        this.audit = audit;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }

    public void create(int tellerUserId, String customerIdStr, AccountType type, String initialDepositStr) {
        ValidationResult v;
        if (!(v = Validators.validateCustomerId(customerIdStr)).isValid()) { fail(v); return; }
        if (!(v = Validators.validateAmount(initialDepositStr)).isValid()) { fail(v); return; }
        if (type == null) {
            state.setValue(UiState.error("T-TYPE", "Account type must be SAVING or CURRENT"));
            return;
        }
        long initial = Validators.parseAmount(initialDepositStr);
        if (initial < MIN_INITIAL_DEPOSIT) {
            state.setValue(UiState.error("F31",
                    "Initial deposit must be at least " + MIN_INITIAL_DEPOSIT + " VND"));
            return;
        }
        int customerId;
        try { customerId = Integer.parseInt(customerIdStr); }
        catch (NumberFormatException nfe) {
            state.setValue(UiState.error("T1", "Invalid customer id")); return;
        }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Teller teller = tellers.findByUserId(tellerUserId);
            User customer = users.findById(customerId);
            if (customer == null) {
                state.postValue(UiState.error("F30", "Customer not found"));
                return;
            }
            BusinessRules.BranchAccessResult ba = BusinessRules.tellerCanAccessCustomer(
                    teller == null ? null : teller.getBranchId(), customer.getBranchId());
            if (ba != BusinessRules.BranchAccessResult.ALLOWED) {
                state.postValue(UiState.error("F32", "Customer is not in your branch"));
                return;
            }

            String accountNumber = accountNumbers.next();
            // Defence: collision check.
            while (accounts.findByNumber(accountNumber) != null) {
                accountNumber = accountNumbers.next();
            }
            Account account = new Account(accountNumber, customerId, type, initial);
            accounts.save(account);

            audit.log(tellerUserId, AuditLogger.ACTION_CREATE, "accounts", null,
                    "{\"account\":\"" + accountNumber + "\",\"type\":\"" + type
                            + "\",\"initial\":" + initial + "}");

            state.postValue(UiState.success("Account opened: " + accountNumber, account));
        });
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
