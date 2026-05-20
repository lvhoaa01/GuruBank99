package com.example.bankingapp.utils;

import com.example.bankingapp.model.Session;

/**
 * Tracks the currently-logged-in user and enforces SRS F51:
 * "Session timeout 5 phút không hoạt động → auto logout".
 *
 * The clock is abstracted via {@link Clock} so unit tests can advance
 * time without sleeping. Process-wide singleton, but the singleton is
 * resettable for tests via {@link #resetForTests(Clock)}.
 */
public class SessionManager {

    public static final long IDLE_TIMEOUT_MILLIS = 5L * 60L * 1000L;

    public interface Clock {
        long now();
    }

    private static SessionManager instance;

    public static synchronized SessionManager get() {
        if (instance == null) {
            instance = new SessionManager(System::currentTimeMillis);
        }
        return instance;
    }

    public static synchronized void resetForTests(Clock clock) {
        instance = new SessionManager(clock);
    }

    private final Clock clock;
    private Session session;

    public SessionManager(Clock clock) {
        this.clock = clock;
    }

    public void login(int userId) {
        this.session = new Session(userId, clock.now());
    }

    public void logout() {
        this.session = null;
    }

    public boolean isLoggedIn() {
        return session != null && !isExpired();
    }

    public Integer currentUserId() {
        return session == null ? null : session.getUserId();
    }

    public void recordActivity() {
        if (session != null) {
            session.setLastActivityMillis(clock.now());
        }
    }

    public boolean isExpired() {
        if (session == null) {
            return false;
        }
        return clock.now() - session.getLastActivityMillis() >= IDLE_TIMEOUT_MILLIS;
    }

    /**
     * Convenience: returns true iff the session is currently active (not expired);
     * if it has expired, the session is cleared as a side-effect.
     */
    public boolean touchOrExpire() {
        if (session == null) {
            return false;
        }
        if (isExpired()) {
            session = null;
            return false;
        }
        recordActivity();
        return true;
    }
}
