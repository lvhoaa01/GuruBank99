package com.example.bankingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;

public class ProfileViewModel extends ViewModel {

    private final UserRepository users;
    private final AppExecutors executors;
    private final MutableLiveData<User> user = new MutableLiveData<>();

    public ProfileViewModel(UserRepository users, AppExecutors executors) {
        this.users = users;
        this.executors = executors;
    }

    public LiveData<User> getUser() { return user; }

    public void load(int customerId) {
        executors.io().execute(() -> user.postValue(users.findById(customerId)));
    }
}
