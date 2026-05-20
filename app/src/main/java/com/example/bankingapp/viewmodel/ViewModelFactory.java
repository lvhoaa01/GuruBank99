package com.example.bankingapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.repository.RepositoryProvider;
import com.example.bankingapp.utils.AccountNumberGenerator;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.CustomerIdGenerator;
import com.example.bankingapp.utils.OtpGenerator;
import com.example.bankingapp.utils.SessionManager;

/**
 * Manual DI for ViewModels. Each ViewModel gets the repos from
 * {@link RepositoryProvider} and the shared {@link AppExecutors}.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        AppExecutors execs = AppExecutors.get();

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
        throw new IllegalArgumentException("Unknown ViewModel " + modelClass);
    }
}
