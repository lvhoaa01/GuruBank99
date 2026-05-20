package com.example.bankingapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.OtpGenerator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransferViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    @Mock AccountRepository accounts;
    @Mock TransactionRepository transactions;
    @Mock OtpGenerator otpGenerator;

    private TransferViewModel vm;
    private static final long NOW = 1_700_000_000_000L;

    @Before public void setUp() {
        when(otpGenerator.generate()).thenReturn("123456");
        when(transactions.sumTransfersOut(anyString(), anyLong(), anyLong())).thenReturn(0L);

        User u = new User(10001, "C10001", "h");
        u.setDailyLimit(10_000_000L);
        when(users.findById(10001)).thenReturn(u);

        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        vm = new TransferViewModel(users, accounts, transactions, otpGenerator, sync);
    }

    @Test public void submitDetails_sameAccount_fails() {
        Account a = new Account("9900000001", 10001, AccountType.CURRENT, 10_000L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        vm.submitDetails(10001, "9900000001", "9900000001", "100", "x", NOW);
        assertEquals("SAME_ACCOUNT", vm.getState().getValue().errorCode);
        assertEquals(TransferViewModel.Phase.FAILED, vm.getPhase().getValue());
    }

    @Test public void submitDetails_dailyLimitExceeded_fails() {
        Account src = new Account("9900000001", 10001, AccountType.CURRENT, 10_000_000L);
        Account dst = new Account("9900000002", 10002, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000001")).thenReturn(src);
        when(accounts.findByNumber("9900000002")).thenReturn(dst);
        when(transactions.sumTransfersOut(anyString(), anyLong(), anyLong())).thenReturn(9_999_999L);
        vm.submitDetails(10001, "9900000001", "9900000002", "100", "x", NOW);
        assertEquals("DAILY_LIMIT_EXCEEDED", vm.getState().getValue().errorCode);
    }

    @Test public void submitDetails_success_movesToOtpPhase() {
        Account src = new Account("9900000001", 10001, AccountType.CURRENT, 10_000L);
        Account dst = new Account("9900000002", 10002, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000001")).thenReturn(src);
        when(accounts.findByNumber("9900000002")).thenReturn(dst);
        vm.submitDetails(10001, "9900000001", "9900000002", "1000", "x", NOW);
        assertEquals(TransferViewModel.Phase.OTP_SENT, vm.getPhase().getValue());
        assertEquals("123456", vm.getGeneratedOtp().getValue());
    }

    @Test public void wrongOtp_keepsOtpPhase_andReportsWrong() {
        primeForOtpPhase();
        vm.submitOtp("000000", NOW);
        assertEquals("OTP_WRONG", vm.getState().getValue().errorCode);
        assertEquals(TransferViewModel.Phase.OTP_SENT, vm.getPhase().getValue());
    }

    @Test public void threeWrongOtps_cancelsTransfer() {
        primeForOtpPhase();
        vm.submitOtp("000000", NOW);
        vm.submitOtp("000000", NOW);
        vm.submitOtp("000000", NOW);
        assertEquals("OTP_ATTEMPTS_EXHAUSTED", vm.getState().getValue().errorCode);
        assertEquals(TransferViewModel.Phase.FAILED, vm.getPhase().getValue());
    }

    @Test public void correctOtp_completesTransfer_andSavesTxn() {
        Account src = new Account("9900000001", 10001, AccountType.CURRENT, 10_000L);
        Account dst = new Account("9900000002", 10002, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000001")).thenReturn(src);
        when(accounts.findByNumber("9900000002")).thenReturn(dst);
        vm.submitDetails(10001, "9900000001", "9900000002", "1000", "x", NOW);
        vm.submitOtp("123456", NOW);
        assertEquals(9_000L, src.getBalance());
        assertEquals(1_000L, dst.getBalance());
        verify(transactions).save(any(Transaction.class));
        assertEquals(TransferViewModel.Phase.COMPLETED, vm.getPhase().getValue());
    }

    @Test public void resendOtp_replacesCode() {
        primeForOtpPhase();
        when(otpGenerator.generate()).thenReturn("999999");
        vm.resendOtp(NOW);
        assertEquals("999999", vm.getGeneratedOtp().getValue());
    }

    @Test public void submitOtpWithoutPending_fails() {
        vm.submitOtp("123456", NOW);
        assertEquals("NO_CHALLENGE", vm.getState().getValue().errorCode);
    }

    private void primeForOtpPhase() {
        Account src = new Account("9900000001", 10001, AccountType.CURRENT, 10_000L);
        Account dst = new Account("9900000002", 10002, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000001")).thenReturn(src);
        when(accounts.findByNumber("9900000002")).thenReturn(dst);
        vm.submitDetails(10001, "9900000001", "9900000002", "1000", "x", NOW);
    }
}
