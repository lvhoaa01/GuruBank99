package com.example.bankingapp.model;

/**
 * Holds the active user's id and last-activity timestamp.
 * Used by {@link com.example.bankingapp.utils.SessionManager} for
 * the SRS-F51 5-minute idle timeout.
 */
public class Session {

    private final int userId;
    private long lastActivityMillis;

    public Session(int userId, long lastActivityMillis) {
        this.userId = userId;
        this.lastActivityMillis = lastActivityMillis;
    }

    public int getUserId() { return userId; }

    public long getLastActivityMillis() { return lastActivityMillis; }
    public void setLastActivityMillis(long lastActivityMillis) { this.lastActivityMillis = lastActivityMillis; }
}
