package com.example.bankingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;

import java.util.Collections;
import java.util.List;

public class DashboardViewModel extends ViewModel {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final AppExecutors executors;

    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<List<Account>> accountList = new MutableLiveData<>(Collections.emptyList());

    public DashboardViewModel(UserRepository users, AccountRepository accounts, AppExecutors executors) {
        this.users = users;
        this.accounts = accounts;
        this.executors = executors;
    }

    public LiveData<User> getUser() { return user; }
    public LiveData<List<Account>> getAccounts() { return accountList; }

    public void load(int customerId) {
        executors.io().execute(() -> {
            user.postValue(users.findById(customerId));
            accountList.postValue(accounts.findByOwner(customerId));
        });
    }
}
