package com.example.bankingapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AccountNumberGenerator;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.CustomerIdGenerator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RegisterViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    @Mock AccountRepository accounts;
    @Mock CustomerIdGenerator customerIds;
    @Mock AccountNumberGenerator accountNumbers;

    private RegisterViewModel vm;

    @Before public void setUp() {
        when(customerIds.next()).thenReturn(11000);
        when(accountNumbers.next()).thenReturn("9900000099");
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        vm = new RegisterViewModel(users, accounts, customerIds, accountNumbers, sync);
    }

    @Test public void invalidName_failsFast() {
        run("John1", "123456789", "01/01/2000", "1 Main St", "Hanoi", "North", "123456", "0901234567", "a@b.co", "Aa1!aaaa", "1000");
        assertEquals("T4", vm.getState().getValue().errorCode);
    }

    @Test public void duplicateEmail_F21() {
        when(users.findByEmail("dup@example.com")).thenReturn(mockUser());
        run("John", "123456789", "01/01/2000", "1 Main St", "Hanoi", "North", "123456", "0901234567", "dup@example.com", "Aa1!aaaa", "1000");
        assertEquals("F21", vm.getState().getValue().errorCode);
    }

    @Test public void duplicateIdNumber_F22() {
        when(users.findByEmail(any())).thenReturn(null);
        when(users.findByIdNumber("123456789")).thenReturn(mockUser());
        run("John", "123456789", "01/01/2000", "1 Main St", "Hanoi", "North", "123456", "0901234567", "a@b.co", "Aa1!aaaa", "1000");
        assertEquals("F22", vm.getState().getValue().errorCode);
    }

    @Test public void initialDepositBelowMin_F31() {
        run("John", "123456789", "01/01/2000", "1 Main St", "Hanoi", "North", "123456", "0901234567", "a@b.co", "Aa1!aaaa", "100");
        assertEquals("F31", vm.getState().getValue().errorCode);
    }

    @Test public void success_savesUserAndAccount() {
        run("John", "123456789", "01/01/2000", "1 Main St", "Hanoi", "North", "123456", "0901234567", "a@b.co", "Aa1!aaaa", "1000");
        assertEquals(UiState.Kind.SUCCESS, vm.getState().getValue().kind);
        verify(users, times(1)).save(any(User.class));
        verify(accounts, times(1)).save(any(Account.class));
    }

    private void run(String name, String id, String dob, String addr, String city, String state,
                     String pin, String phone, String email, String pwd, String deposit) {
        vm.register(name, id, dob, addr, city, state, pin, phone, email, pwd, deposit);
    }

    private static User mockUser() {
        return new User(99999, "C99999", "h");
    }
}
