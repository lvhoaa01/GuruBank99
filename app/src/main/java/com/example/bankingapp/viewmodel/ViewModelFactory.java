package com.example.bankingapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.repository.RepositoryProvider;
import com.example.bankingapp.utils.AccountNumberGenerator;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.utils.CustomerIdGenerator;
import com.example.bankingapp.utils.OtpGenerator;
import com.example.bankingapp.utils.SessionManager;
import com.example.bankingapp.utils.TempPasswordGenerator;
import com.example.bankingapp.viewmodel.teller.DeleteAccountViewModel;
import com.example.bankingapp.viewmodel.teller.DeleteCustomerViewModel;
import com.example.bankingapp.viewmodel.teller.EditAccountViewModel;
import com.example.bankingapp.viewmodel.teller.EditCustomerViewModel;
import com.example.bankingapp.viewmodel.teller.NewAccountViewModel;
import com.example.bankingapp.viewmodel.teller.NewCustomerViewModel;
import com.example.bankingapp.viewmodel.teller.TellerDashboardViewModel;
import com.example.bankingapp.viewmodel.teller.TellerDepositViewModel;
import com.example.bankingapp.viewmodel.teller.TellerStatementViewModel;
import com.example.bankingapp.viewmodel.teller.TellerTransferViewModel;
import com.example.bankingapp.viewmodel.teller.TellerWithdrawViewModel;

/**
 * Manual DI for ViewModels — Customer + Teller.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        AppExecutors execs = AppExecutors.get();
        AuditLogger audit = new AuditLogger(RepositoryProvider.auditLog(), execs);

        // ===== Customer =====
        if (modelClass == LoginViewModel.class) {
            return (T) new LoginViewModel(RepositoryProvider.users(), SessionManager.get(), execs);
        }
        if (modelClass == RegisterViewModel.class) {
            return (T) new RegisterViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    new CustomerIdGenerator(11000),
                    new AccountNumberGenerator(),
                    execs);
        }
        if (modelClass == DashboardViewModel.class) {
            return (T) new DashboardViewModel(RepositoryProvider.users(), RepositoryProvider.accounts(), execs);
        }
        if (modelClass == DepositViewModel.class) {
            return (T) new DepositViewModel(RepositoryProvider.accounts(), RepositoryProvider.transactions(), execs);
        }
        if (modelClass == WithdrawViewModel.class) {
            return (T) new WithdrawViewModel(RepositoryProvider.accounts(), RepositoryProvider.transactions(), execs);
        }
        if (modelClass == TransferViewModel.class) {
            return (T) new TransferViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.transactions(),
                    new OtpGenerator(),
                    execs);
        }
        if (modelClass == TransactionHistoryViewModel.class) {
            return (T) new TransactionHistoryViewModel(RepositoryProvider.transactions(), execs);
        }
        if (modelClass == ChangePasswordViewModel.class) {
            return (T) new ChangePasswordViewModel(RepositoryProvider.users(), execs);
        }
        if (modelClass == ProfileViewModel.class) {
            return (T) new ProfileViewModel(RepositoryProvider.users(), execs);
        }

        // ===== Teller =====
        if (modelClass == TellerDashboardViewModel.class) {
            return (T) new TellerDashboardViewModel(RepositoryProvider.tellers(), execs);
        }
        if (modelClass == NewCustomerViewModel.class) {
            return (T) new NewCustomerViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.tellers(),
                    new CustomerIdGenerator(11000),
                    new TempPasswordGenerator(),
                    audit,
                    execs);
        }
        if (modelClass == EditCustomerViewModel.class) {
            return (T) new EditCustomerViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.tellers(),
                    audit,
                    execs);
        }
        if (modelClass == DeleteCustomerViewModel.class) {
            return (T) new DeleteCustomerViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.tellers(),
                    audit,
                    execs);
        }
        if (modelClass == NewAccountViewModel.class) {
            return (T) new NewAccountViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.tellers(),
                    new AccountNumberGenerator(),
                    audit,
                    execs);
        }
        if (modelClass == EditAccountViewModel.class) {
            return (T) new EditAccountViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.tellers(),
                    audit,
                    execs);
        }
        if (modelClass == DeleteAccountViewModel.class) {
            return (T) new DeleteAccountViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.tellers(),
                    audit,
                    execs);
        }
        if (modelClass == TellerDepositViewModel.class) {
            return (T) new TellerDepositViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.transactions(),
                    RepositoryProvider.tellers(),
                    audit,
                    execs);
        }
        if (modelClass == TellerWithdrawViewModel.class) {
            return (T) new TellerWithdrawViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.transactions(),
                    RepositoryProvider.tellers(),
                    audit,
                    execs);
        }
        if (modelClass == TellerTransferViewModel.class) {
            return (T) new TellerTransferViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.transactions(),
                    RepositoryProvider.tellers(),
                    audit,
                    execs);
        }
        if (modelClass == TellerStatementViewModel.class) {
            return (T) new TellerStatementViewModel(
                    RepositoryProvider.users(),
                    RepositoryProvider.accounts(),
                    RepositoryProvider.transactions(),
                    RepositoryProvider.tellers(),
                    execs);
        }

        throw new IllegalArgumentException("Unknown ViewModel " + modelClass);
    }
}
