package com.example.bankingapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.utils.AppExecutors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DepositViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock AccountRepository accounts;
    @Mock TransactionRepository transactions;

    private DepositViewModel vm;

    @Before public void setUp() {
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        vm = new DepositViewModel(accounts, transactions, sync);
    }

    @Test public void emptyAccount_fails() {
        vm.deposit(10001, "", "100", "test", 0L);
        assertEquals("ACCT_EMPTY", vm.getState().getValue().errorCode);
    }

    @Test public void invalidAmount_fails() {
        vm.deposit(10001, "9900000001", "abc", "test", 0L);
        assertEquals("AMT_LETTERS", vm.getState().getValue().errorCode);
    }

    @Test public void zeroAmount_fails() {
        vm.deposit(10001, "9900000001", "0", "test", 0L);
        assertEquals("AMT_NONPOSITIVE", vm.getState().getValue().errorCode);
    }

    @Test public void accountMissing_fails() {
        when(accounts.findByNumber("9900000099")).thenReturn(null);
        vm.deposit(10001, "9900000099", "100", "test", 0L);
        assertEquals("ACCOUNT_NOT_FOUND", vm.getState().getValue().errorCode);
    }

    @Test public void notOwner_fails() {
        Account a = new Account("9900000001", 99999, AccountType.CURRENT, 100L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        vm.deposit(10001, "9900000001", "100", "test", 0L);
        assertEquals("NOT_OWNER", vm.getState().getValue().errorCode);
    }

    @Test public void success_updatesBalanceAndSavesTxn() {
        Account a = new Account("9900000001", 10001, AccountType.CURRENT, 500L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        vm.deposit(10001, "9900000001", "1000", "test", 1L);
        assertEquals(UiState.Kind.SUCCESS, vm.getState().getValue().kind);
        assertEquals(1500L, a.getBalance());
        verify(accounts).save(a);
        verify(transactions).save(any(Transaction.class));
    }
}
