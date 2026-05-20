package com.example.bankingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.model.enums.TransactionStatus;
import com.example.bankingapp.model.enums.TransactionType;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;

public class WithdrawViewModel extends ViewModel {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final AppExecutors executors;

    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public WithdrawViewModel(AccountRepository accounts,
                              TransactionRepository transactions,
                              AppExecutors executors) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }

    public void withdraw(int customerId, String accountNumber, String amountStr, String description) {
        withdraw(customerId, accountNumber, amountStr, description, System.currentTimeMillis());
    }

    public void withdraw(int customerId, String accountNumber, String amountStr, String description, long nowMillis) {
        ValidationResult v;
        if (!(v = Validators.validateAccountNumber(accountNumber)).isValid()) { fail(v); return; }
        if (!(v = Validators.validateAmount(amountStr)).isValid())            { fail(v); return; }
        if (!(v = Validators.validateDescription(description)).isValid())     { fail(v); return; }

        long amount = Validators.parseAmount(amountStr);

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            Account account = accounts.findByNumber(accountNumber);

            if (account != null && account.getOwnerCustomerId() != customerId) {
                state.postValue(UiState.error("NOT_OWNER", "You do not own this account"));
                return;
            }

            int month = BusinessRules.monthOf(nowMillis);
            BusinessRules.WithdrawResult result = BusinessRules.canWithdraw(account, amount, month);
            if (result != BusinessRules.WithdrawResult.SUCCESS) {
                state.postValue(UiState.error(result.name(), errorMessage(result)));
                return;
            }

            account.setBalance(account.getBalance() - amount);
            if (account.getType() == AccountType.SAVING) {
                if (account.getCountersMonth() != month) {
                    account.setCountersMonth(month);
                    account.setMonthlyWithdrawCount(0);
                    account.setMonthlyTransferCount(0);
                }
                account.setMonthlyWithdrawCount(account.getMonthlyWithdrawCount() + 1);
            }
            accounts.save(account);

            Transaction saved = transactions.save(new Transaction(
                    0L,
                    null,
                    accountNumber,
                    amount,
                    0L,
                    TransactionType.WITHDRAW,
                    description,
                    nowMillis,
                    customerId,
                    TransactionStatus.SUCCESS,
                    account.getBalance()));

            state.postValue(UiState.success("Withdrawal successful", saved));
        });
    }

    private static String errorMessage(BusinessRules.WithdrawResult r) {
        switch (r) {
            case ACCOUNT_NOT_FOUND:             return "Account not found";
            case ACCOUNT_INACTIVE:              return "Account is not active";
            case INVALID_AMOUNT:                return "Amount must be greater than zero";
            case INSUFFICIENT_FUNDS:            return "Insufficient funds";
            case SAVING_MONTHLY_LIMIT_REACHED:  return "Monthly withdrawal limit reached for savings account";
            case SAVING_BALANCE_FLOOR_VIOLATED: return "Savings account must keep a minimum balance of 500 VND";
            default:                            return "Withdrawal failed";
        }
    }

    private void fail(ValidationResult v) {
        state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
    }
}
