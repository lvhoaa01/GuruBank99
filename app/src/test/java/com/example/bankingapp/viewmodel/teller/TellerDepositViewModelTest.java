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
public class TellerDepositViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    @Mock AccountRepository accounts;
    @Mock TransactionRepository transactions;
    @Mock TellerRepository tellers;
    @Mock AuditLogRepository auditRepo;

    private TellerDepositViewModel vm;
    private static final int TELLER_USER_ID = 1;

    @Before public void setUp() {
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        AuditLogger audit = new AuditLogger(auditRepo, sync);
        when(tellers.findByUserId(TELLER_USER_ID))
                .thenReturn(new Teller(1, TELLER_USER_ID, "Huy", "CN001"));
        // save() returns the input unchanged so the VM's `saved.getTransactionId()`
        // sees a non-null Transaction (real JDBC impl uses RETURNING transaction_id).
        lenient().when(transactions.save(any(Transaction.class))).thenAnswer(returnsFirstArg());
        vm = new TellerDepositViewModel(users, accounts, transactions, tellers, audit, sync);
    }

    @Test public void accountInOtherBranch_returnsF19() {
        Account a = new Account("9900000003", 10002, AccountType.CURRENT, 500_000L);
        when(accounts.findByNumber("9900000003")).thenReturn(a);
        when(users.findById(10002)).thenReturn(customerInBranch(10002, "CN002"));
        vm.deposit(TELLER_USER_ID, "9900000003", "100000", "test", 0L);
        assertEquals("F19", vm.getState().getValue().errorCode);
    }

    @Test public void missingAccount_returnsF18() {
        when(accounts.findByNumber("9900000099")).thenReturn(null);
        vm.deposit(TELLER_USER_ID, "9900000099", "100000", "test", 0L);
        assertEquals("F18", vm.getState().getValue().errorCode);
    }

    @Test public void success_credits_andRecordsTeller() {
        Account a = new Account("9900000001", 10001, AccountType.SAVING, 1_000_000L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        when(users.findById(10001)).thenReturn(customerInBranch(10001, "CN001"));
        vm.deposit(TELLER_USER_ID, "9900000001", "500000", "salary", 0L);
        assertEquals(UiState.Kind.SUCCESS, vm.getState().getValue().kind);
        assertEquals(1_500_000L, a.getBalance());
        verify(accounts).save(a);
        verify(transactions).save(any(Transaction.class));
        verify(auditRepo).save(any());
    }

    private User customerInBranch(int id, String branchId) {
        User u = new User(id, "C" + id, "h");
        u.setBranchId(branchId);
        return u;
    }
}
