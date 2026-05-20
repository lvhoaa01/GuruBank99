package com.example.bankingapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.UserStatus;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.PasswordHasher;
import com.example.bankingapp.utils.SessionManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoginViewModelTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock UserRepository users;
    private SessionManager session;
    private LoginViewModel vm;

    private static final long NOW = 1_000_000L;

    @Before public void setUp() {
        session = new SessionManager(() -> NOW);
        AppExecutors sync = new AppExecutors(Runnable::run, Runnable::run);
        vm = new LoginViewModel(users, session, sync);
    }

    @Test public void emptyUsername_reportsT99() {
        vm.login("", "Password1!", NOW);
        UiState s = vm.getState().getValue();
        assertEquals("T99", s.errorCode);
    }

    @Test public void emptyPassword_reportsT100() {
        vm.login("C10001", "", NOW);
        assertEquals("T100", vm.getState().getValue().errorCode);
    }

    @Test public void unknownUser_reportsUserNotFound() {
        when(users.findByUsername("nope")).thenReturn(null);
        vm.login("nope", "Password1!", NOW);
        assertEquals("USER_NOT_FOUND", vm.getState().getValue().errorCode);
    }

    @Test public void wrongPassword_firstAttempt_invalidCredentials() {
        User u = active("C10001", "Password1!");
        when(users.findByUsername("C10001")).thenReturn(u);
        vm.login("C10001", "Wrong!", NOW);
        assertEquals("INVALID_CREDENTIALS", vm.getState().getValue().errorCode);
    }

    @Test public void threeWrongAttempts_locksAccount() {
        User u = active("C10001", "Password1!");
        when(users.findByUsername("C10001")).thenReturn(u);
        vm.login("C10001", "Wrong!", NOW);
        vm.login("C10001", "Wrong!", NOW);
        vm.login("C10001", "Wrong!", NOW);
        assertEquals("ACCOUNT_LOCKED", vm.getState().getValue().errorCode);
        assertEquals(UserStatus.LOCKED, u.getStatus());
    }

    @Test public void lockedAccountInsideWindow_reportsLocked() {
        User u = active("C10001", "Password1!");
        u.setStatus(UserStatus.LOCKED);
        u.setLockedUntilMillis(NOW + 1000L);
        when(users.findByUsername("C10001")).thenReturn(u);
        vm.login("C10001", "Password1!", NOW);
        assertEquals("ACCOUNT_LOCKED", vm.getState().getValue().errorCode);
    }

    @Test public void success_opensSession() {
        User u = active("C10001", "Password1!");
        when(users.findByUsername("C10001")).thenReturn(u);
        vm.login("C10001", "Password1!", NOW);
        UiState s = vm.getState().getValue();
        assertEquals(UiState.Kind.SUCCESS, s.kind);
        // Payload is now the full User so the Activity can route by role.
        assertEquals(u, s.payload);
    }

    private static User active(String username, String password) {
        User u = new User(parseId(username), username, PasswordHasher.hash(password));
        u.setStatus(UserStatus.ACTIVE);
        return u;
    }
    private static int parseId(String username) {
        return Integer.parseInt(username.substring(1));
    }
}
