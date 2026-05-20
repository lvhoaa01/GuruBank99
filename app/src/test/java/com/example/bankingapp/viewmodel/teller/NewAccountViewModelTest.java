package com.example.bankingapp.viewmodel.teller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import com.example.bankingapp.utils.AccountNumberGenerator;
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
public class NewAccountViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    @Mock AccountRepository accounts;
    @Mock TellerRepository tellers;
    @Mock AuditLogRepository auditRepo;

    private NewAccountViewModel vm;
    private static final int TELLER_USER_ID = 1;

    @Before public void setUp() {
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        AuditLogger audit = new AuditLogger(auditRepo, sync);
        when(tellers.findByUserId(TELLER_USER_ID))
                .thenReturn(new Teller(1, TELLER_USER_ID, "Huy", "CN001"));
        vm = new NewAccountViewModel(users, accounts, tellers,
                new AccountNumberGenerator(), audit, sync);
    }

    @Test public void initialBelow500_returnsF31() {
        vm.create(TELLER_USER_ID, "10001", AccountType.SAVING, "499");
        assertEquals("F31", vm.getState().getValue().errorCode);
    }

    @Test public void exact500_passes() {
        when(users.findById(10001)).thenReturn(customerInBranch(10001, "CN001"));
        vm.create(TELLER_USER_ID, "10001", AccountType.SAVING, "500");
        assertEquals(UiState.Kind.SUCCESS, vm.getState().getValue().kind);
        verify(accounts).save(any(Account.class));
    }

    @Test public void customerInOtherBranch_returnsF32() {
        when(users.findById(10002)).thenReturn(customerInBranch(10002, "CN002"));
        vm.create(TELLER_USER_ID, "10002", AccountType.CURRENT, "1000");
        assertEquals("F32", vm.getState().getValue().errorCode);
    }

    @Test public void unknownCustomer_returnsF30() {
        when(users.findById(99999)).thenReturn(null);
        vm.create(TELLER_USER_ID, "99999", AccountType.CURRENT, "1000");
        assertEquals("F30", vm.getState().getValue().errorCode);
    }

    private User customerInBranch(int id, String branchId) {
        User u = new User(id, "C" + id, "h");
        u.setBranchId(branchId);
        return u;
    }
}
