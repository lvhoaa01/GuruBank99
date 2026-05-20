package com.example.bankingapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.PasswordHasher;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChangePasswordViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    private ChangePasswordViewModel vm;

    @Before public void setUp() {
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        vm = new ChangePasswordViewModel(users, sync);
    }

    @Test public void oldEmpty_T104() {
        vm.change(10001, "", "Aa1!aaaa", "Aa1!aaaa");
        assertEquals("T104", vm.getState().getValue().errorCode);
    }

    @Test public void newPasswordTooShort_T108() {
        vm.change(10001, "old", "A1!a", "A1!a");
        assertEquals("T108", vm.getState().getValue().errorCode);
    }

    @Test public void confirmMismatch_T110() {
        vm.change(10001, "old", "Aa1!aaaa", "DIFFERENT!");
        assertEquals("T110", vm.getState().getValue().errorCode);
    }

    @Test public void wrongOldPassword_F39() {
        User u = new User(10001, "C10001", PasswordHasher.hash("correct"));
        when(users.findById(10001)).thenReturn(u);
        vm.change(10001, "wrong", "Aa1!aaaa", "Aa1!aaaa");
        assertEquals("F39", vm.getState().getValue().errorCode);
    }

    @Test public void success_updatesHash() {
        User u = new User(10001, "C10001", PasswordHasher.hash("old"));
        when(users.findById(10001)).thenReturn(u);
        vm.change(10001, "old", "Aa1!aaaa", "Aa1!aaaa");
        assertEquals(UiState.Kind.SUCCESS, vm.getState().getValue().kind);
        assertEquals(PasswordHasher.hash("Aa1!aaaa"), u.getPasswordHash());
    }
}
