package com.example.bankingapp.viewmodel.teller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.bankingapp.model.Teller;
import com.example.bankingapp.model.User;
import com.example.bankingapp.repository.AuditLogRepository;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.AuditLogger;
import com.example.bankingapp.utils.CustomerIdGenerator;
import com.example.bankingapp.utils.TempPasswordGenerator;
import com.example.bankingapp.viewmodel.UiState;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class NewCustomerViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    @Mock TellerRepository tellers;
    @Mock AuditLogRepository auditRepo;

    private NewCustomerViewModel vm;
    private static final int TELLER_USER_ID = 1;

    @Before public void setUp() {
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        AuditLogger audit = new AuditLogger(auditRepo, sync);
        when(tellers.findByUserId(TELLER_USER_ID))
                .thenReturn(new Teller(1, TELLER_USER_ID, "Pham Quang Huy", "CN001"));
        vm = new NewCustomerViewModel(
                users, tellers,
                new CustomerIdGenerator(11000),
                new TempPasswordGenerator(new Random(42L)),
                audit, sync);
    }

    @Test public void invalidName_failsFast() {
        vm.create(TELLER_USER_ID, "John1", "123456789", "01/01/2000",
                "1 Main St", "Hanoi", "North", "123456", "0901234567",
                "x@y.co", 1_000_000L);
        assertEquals("T4", vm.getState().getValue().errorCode);
    }

    @Test public void duplicateEmail_F21() {
        when(users.findByEmail("dup@example.com")).thenReturn(stub(99999));
        vm.create(TELLER_USER_ID, "John", "123456789", "01/01/2000",
                "1 Main St", "Hanoi", "North", "123456", "0901234567",
                "dup@example.com", 1_000_000L);
        assertEquals("F21", vm.getState().getValue().errorCode);
    }

    @Test public void duplicateId_F22() {
        when(users.findByEmail(any())).thenReturn(null);
        when(users.findByIdNumber("123456789")).thenReturn(stub(99999));
        vm.create(TELLER_USER_ID, "John", "123456789", "01/01/2000",
                "1 Main St", "Hanoi", "North", "123456", "0901234567",
                "x@y.co", 1_000_000L);
        assertEquals("F22", vm.getState().getValue().errorCode);
    }

    @Test public void dailyLimitOutOfRange_T35() {
        vm.create(TELLER_USER_ID, "John", "123456789", "01/01/2000",
                "1 Main St", "Hanoi", "North", "123456", "0901234567",
                "x@y.co", -5L);
        assertEquals("T35", vm.getState().getValue().errorCode);
    }

    @Test public void success_savesUser_andLogsAudit() {
        vm.create(TELLER_USER_ID, "John", "123456789", "01/01/2000",
                "1 Main St", "Hanoi", "North", "123456", "0901234567",
                "ok@example.com", 1_000_000L);
        UiState s = vm.getState().getValue();
        assertEquals(UiState.Kind.SUCCESS, s.kind);
        verify(users, times(1)).save(any(User.class));
        verify(auditRepo, times(1)).save(any());
    }

    private static User stub(int id) {
        return new User(id, "C" + id, "h");
    }
}
