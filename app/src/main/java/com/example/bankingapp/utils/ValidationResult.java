package com.example.bankingapp.utils;

/**
 * Outcome of a single validation rule.
 * Carries a stable error code so tests can assert on cause without
 * relying on user-facing messages.
 */
public final class ValidationResult {

    private final boolean valid;
    private final String errorCode;
    private final String message;

    private ValidationResult(boolean valid, String errorCode, String message) {
        this.valid = valid;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult fail(String code, String message) {
        return new ValidationResult(false, code, message);
    }

    public boolean isValid() { return valid; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
}
