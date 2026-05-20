package com.example.bankingapp.model;

/**
 * Represents a one-time OTP challenge bound to a pending transfer.
 * SRS 3.5: 6 digits, 120s expiry, max 3 wrong attempts, max 3 resends.
 */
public class OtpChallenge {

    public static final int MAX_WRONG_ATTEMPTS = 3;
    public static final int MAX_RESENDS = 3;
    public static final long VALIDITY_MILLIS = 120_000L;

    private final String otpCode;
    private final long expiresAtMillis;
    private int wrongAttempts;
    private int resends;

    public OtpChallenge(String otpCode, long expiresAtMillis) {
        this.otpCode = otpCode;
        this.expiresAtMillis = expiresAtMillis;
        this.wrongAttempts = 0;
        this.resends = 0;
    }

    public String getOtpCode() { return otpCode; }
    public long getExpiresAtMillis() { return expiresAtMillis; }
    public int getWrongAttempts() { return wrongAttempts; }
    public int getResends() { return resends; }

    public void incrementWrongAttempts() { this.wrongAttempts++; }
    public void incrementResends() { this.resends++; }

    public boolean isExpired(long nowMillis) { return nowMillis >= expiresAtMillis; }
}
