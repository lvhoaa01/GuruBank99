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
public class WithdrawViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock AccountRepository accounts;
    @Mock TransactionRepository transactions;

    private WithdrawViewModel vm;
    private static final long NOW = 1_700_000_000_000L;

    @Before public void setUp() {
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        vm = new WithdrawViewModel(accounts, transactions, sync);
    }

    @Test public void notOwner_fails() {
        Account a = new Account("9900000001", 99999, AccountType.CURRENT, 1000L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        vm.withdraw(10001, "9900000001", "100", "x", NOW);
        assertEquals("NOT_OWNER", vm.getState().getValue().errorCode);
    }

    @Test public void insufficientFunds_fails() {
        Account a = new Account("9900000001", 10001, AccountType.CURRENT, 100L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        vm.withdraw(10001, "9900000001", "500", "x", NOW);
        assertEquals("INSUFFICIENT_FUNDS", vm.getState().getValue().errorCode);
    }

    @Test public void savingBalanceFloor_blocks() {
        Account a = new Account("9900000001", 10001, AccountType.SAVING, 600L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        vm.withdraw(10001, "9900000001", "200", "x", NOW);
        assertEquals("SAVING_BALANCE_FLOOR_VIOLATED", vm.getState().getValue().errorCode);
    }

    @Test public void success_decrementsBalance_andIncrementsMonthlyCounter() {
        Account a = new Account("9900000001", 10001, AccountType.SAVING, 5_000L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        vm.withdraw(10001, "9900000001", "1000", "x", NOW);
        assertEquals(4_000L, a.getBalance());
        assertEquals(1, a.getMonthlyWithdrawCount());
        verify(accounts).save(a);
        verify(transactions).save(any(Transaction.class));
        assertEquals(UiState.Kind.SUCCESS, vm.getState().getValue().kind);
    }
}
