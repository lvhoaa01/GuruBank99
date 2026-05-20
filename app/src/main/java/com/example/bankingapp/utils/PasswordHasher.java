package com.example.bankingapp.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 with a fixed app salt. NOT production-safe — used here because
 * the project is a software-testing course exercise and we want to keep
 * dependencies minimal (no bcrypt library). The function is pure so
 * unit tests can rely on a stable mapping from password → hash.
 */
public final class PasswordHasher {

    private static final String SALT = "bank99::";

    private PasswordHasher() { }

    public static String hash(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((SALT + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
