package com.example.bankingapp.viewmodel.teller;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AuditLogRepository;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.viewmodel.UiState;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TellerTransferViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    @Mock AccountRepository accounts;
    @Mock TransactionRepository transactions;
    @Mock TellerRepository tellers;
    @Mock AuditLogRepository auditRepo;

    private TellerTransferViewModel vm;
    private static final int TELLER_USER_ID = 1;
    private static final long NOW = 1_700_000_000_000L;

    @Before public void setUp() {
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        AuditLogger audit = new AuditLogger(auditRepo, sync);
        when(tellers.findByUserId(TELLER_USER_ID))
                .thenReturn(new Teller(1, TELLER_USER_ID, "Huy", "CN001"));
        lenient().when(transactions.save(any(Transaction.class))).thenAnswer(returnsFirstArg());
        vm = new TellerTransferViewModel(users, accounts, transactions, tellers, audit, sync);
    }

    @Test public void srcInOtherBranch_returnsF8() {
        Account src = new Account("9900000003", 10002, AccountType.CURRENT, 1_000_000L);
        Account dst = new Account("9900000001", 10001, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000003")).thenReturn(src);
        when(accounts.findByNumber("9900000001")).thenReturn(dst);
        when(users.findById(10002)).thenReturn(customerInBranch(10002, "CN002"));
        // Note: source owner's branch check fires first, so we don't stub dst owner.
        vm.transfer(TELLER_USER_ID, "9900000003", "9900000001", "100000", "x", NOW);
        assertEquals("F8", vm.getState().getValue().errorCode);
    }

    @Test public void sameAccount_returnsF6() {
        Account same = new Account("9900000001", 10001, AccountType.CURRENT, 1_000_000L);
        when(accounts.findByNumber("9900000001")).thenReturn(same);
        when(users.findById(10001)).thenReturn(customerInBranch(10001, "CN001"));
        vm.transfer(TELLER_USER_ID, "9900000001", "9900000001", "100000", "x", NOW);
        assertEquals("F6", vm.getState().getValue().errorCode);
    }

    @Test public void insufficient_returnsF7() {
        Account src = new Account("9900000001", 10001, AccountType.CURRENT, 100L);
        Account dst = new Account("9900000003", 10002, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000001")).thenReturn(src);
        when(accounts.findByNumber("9900000003")).thenReturn(dst);
        when(users.findById(10001)).thenReturn(customerInBranch(10001, "CN001"));
        vm.transfer(TELLER_USER_ID, "9900000001", "9900000003", "100000", "x", NOW);
        assertEquals("F7", vm.getState().getValue().errorCode);
    }

    @Test public void success_moves_andRecords() {
        Account src = new Account("9900000001", 10001, AccountType.CURRENT, 1_000_000L);
        Account dst = new Account("9900000003", 10002, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000001")).thenReturn(src);
        when(accounts.findByNumber("9900000003")).thenReturn(dst);
        when(users.findById(10001)).thenReturn(customerInBranch(10001, "CN001"));
        // Only source owner branch is consulted; dst owner stub omitted.
        vm.transfer(TELLER_USER_ID, "9900000001", "9900000003", "100000", "x", NOW);
        assertEquals(UiState.Kind.SUCCESS, vm.getState().getValue().kind);
        assertEquals(900_000L, src.getBalance());
        assertEquals(100_000L, dst.getBalance());
        verify(transactions).save(any(Transaction.class));
        verify(auditRepo).save(any());
    }

    private User customerInBranch(int id, String branchId) {
        User u = new User(id, "C" + id, "h");
        u.setBranchId(branchId);
        return u;
    }
}
