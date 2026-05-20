package com.example.bankingapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SessionManagerTest {

    private static class FakeClock implements SessionManager.Clock {
        long now;
        public FakeClock(long start) { this.now = start; }
        @Override public long now() { return now; }
    }

    @Test public void notLoggedIn_byDefault() {
        SessionManager sm = new SessionManager(new FakeClock(0));
        assertFalse(sm.isLoggedIn());
        assertFalse(sm.touchOrExpire());
    }

    @Test public void login_isLoggedIn() {
        FakeClock clock = new FakeClock(1000L);
        SessionManager sm = new SessionManager(clock);
        sm.login(42);
        assertTrue(sm.isLoggedIn());
        assertEquals(Integer.valueOf(42), sm.currentUserId());
    }

    @Test public void expires_after5Minutes() {
        FakeClock clock = new FakeClock(0L);
        SessionManager sm = new SessionManager(clock);
        sm.login(1);
        clock.now = SessionManager.IDLE_TIMEOUT_MILLIS - 1;
        assertTrue(sm.isLoggedIn());
        clock.now = SessionManager.IDLE_TIMEOUT_MILLIS;
        assertFalse(sm.isLoggedIn());
    }

    @Test public void recordActivity_extendsSession() {
        FakeClock clock = new FakeClock(0L);
        SessionManager sm = new SessionManager(clock);
        sm.login(1);
        clock.now = 60_000L;     // +1 min — still alive
        sm.recordActivity();
        clock.now = 60_000L + SessionManager.IDLE_TIMEOUT_MILLIS - 1;
        assertTrue(sm.isLoggedIn());
        clock.now = 60_000L + SessionManager.IDLE_TIMEOUT_MILLIS;
        assertFalse(sm.isLoggedIn());
    }

    @Test public void touchOrExpire_clearsSessionWhenExpired() {
        FakeClock clock = new FakeClock(0L);
        SessionManager sm = new SessionManager(clock);
        sm.login(1);
        clock.now = SessionManager.IDLE_TIMEOUT_MILLIS + 1;
        assertFalse(sm.touchOrExpire());
        assertFalse(sm.isLoggedIn());
    }

    @Test public void logout_clears() {
        SessionManager sm = new SessionManager(new FakeClock(0L));
        sm.login(1);
        sm.logout();
        assertFalse(sm.isLoggedIn());
    }
}
