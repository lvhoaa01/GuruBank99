package com.example.bankingapp.viewmodel;

/**
 * Generic UI state envelope used by all ViewModels.
 * Tests assert on {@link #kind} + {@link #errorCode} rather than UI strings.
 */
public final class UiState {

    public enum Kind { IDLE, LOADING, SUCCESS, ERROR }

    public final Kind kind;
    public final String message;
    public final String errorCode;
    public final Object payload;

    private UiState(Kind kind, String message, String errorCode, Object payload) {
        this.kind = kind;
        this.message = message;
        this.errorCode = errorCode;
        this.payload = payload;
    }

    public static UiState idle() {
        return new UiState(Kind.IDLE, null, null, null);
    }

    public static UiState loading() {
        return new UiState(Kind.LOADING, null, null, null);
    }

    public static UiState success(String message) {
        return new UiState(Kind.SUCCESS, message, null, null);
    }

    public static UiState success(String message, Object payload) {
        return new UiState(Kind.SUCCESS, message, null, payload);
    }

    public static UiState error(String errorCode, String message) {
        return new UiState(Kind.ERROR, message, errorCode, null);
    }
}
