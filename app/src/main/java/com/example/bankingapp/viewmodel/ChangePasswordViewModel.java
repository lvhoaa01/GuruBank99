package com.example.bankingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.PasswordHasher;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;

public class ChangePasswordViewModel extends ViewModel {

    private final UserRepository users;
    private final AppExecutors executors;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    public ChangePasswordViewModel(UserRepository users, AppExecutors executors) {
        this.users = users;
        this.executors = executors;
    }

    public LiveData<UiState> getState() { return state; }

    public void change(int customerId, String oldPassword, String newPassword, String confirm) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            state.setValue(UiState.error("T104", "Old password is required"));
            return;
        }
        ValidationResult v;
        if (!(v = Validators.validateNewPassword(newPassword)).isValid()) {
            state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
            return;
        }
        if (!(v = Validators.validatePasswordConfirmation(newPassword, confirm)).isValid()) {
            state.setValue(UiState.error(v.getErrorCode(), v.getMessage()));
            return;
        }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            User user = users.findById(customerId);
            if (user == null) {
                state.postValue(UiState.error("USER_NOT_FOUND", "User not found"));
                return;
            }
            String oldHash = PasswordHasher.hash(oldPassword);
            if (!oldHash.equals(user.getPasswordHash())) {
                state.postValue(UiState.error("F39", "Old password is incorrect"));
                return;
            }
            user.setPasswordHash(PasswordHasher.hash(newPassword));
            users.save(user);
            state.postValue(UiState.success("Password changed. Please log in again."));
        });
    }
}
