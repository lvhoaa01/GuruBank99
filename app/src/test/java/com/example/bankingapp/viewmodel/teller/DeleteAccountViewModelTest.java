package com.example.bankingapp.viewmodel.teller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AuditLogRepository;
import com.example.bankingapp.repository.TellerRepository;
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
public class DeleteAccountViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    @Mock AccountRepository accounts;
    @Mock TellerRepository tellers;
    @Mock AuditLogRepository auditRepo;

    private DeleteAccountViewModel vm;
    private static final int TELLER_USER_ID = 1;

    @Before public void setUp() {
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        AuditLogger audit = new AuditLogger(auditRepo, sync);
        when(tellers.findByUserId(TELLER_USER_ID))
                .thenReturn(new Teller(1, TELLER_USER_ID, "Huy", "CN001"));
        vm = new DeleteAccountViewModel(users, accounts, tellers, audit, sync);
    }

    @Test public void balanceNonZero_F36() {
        Account a = new Account("9900000001", 10001, AccountType.CURRENT, 100L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        when(users.findById(10001)).thenReturn(customerInBranch(10001, "CN001"));
        vm.delete(TELLER_USER_ID, "9900000001");
        assertEquals("F36", vm.getState().getValue().errorCode);
    }

    @Test public void wrongBranch_F37() {
        Account a = new Account("9900000003", 10002, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000003")).thenReturn(a);
        when(users.findById(10002)).thenReturn(customerInBranch(10002, "CN002"));
        vm.delete(TELLER_USER_ID, "9900000003");
        assertEquals("F37", vm.getState().getValue().errorCode);
    }

    @Test public void zeroBalanceSameBranch_closes() {
        Account a = new Account("9900000001", 10001, AccountType.CURRENT, 0L);
        when(accounts.findByNumber("9900000001")).thenReturn(a);
        when(users.findById(10001)).thenReturn(customerInBranch(10001, "CN001"));
        vm.delete(TELLER_USER_ID, "9900000001");
        assertEquals(UiState.Kind.SUCCESS, vm.getState().getValue().kind);
        assertFalse(a.isActive());
        verify(accounts).save(a);
    }

    private User customerInBranch(int id, String branchId) {
        User u = new User(id, "C" + id, "h");
        u.setBranchId(branchId);
        return u;
    }
}
