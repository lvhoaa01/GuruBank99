package com.example.bankingapp.utils;

import java.util.Random;

/**
 * Generates a 6-digit OTP. The {@link Random} is injected so unit tests can
 * use a seeded instance for deterministic output.
 */
public class OtpGenerator {

    private final Random random;

    public OtpGenerator() {
        this(new Random());
    }

    public OtpGenerator(Random random) {
        this.random = random;
    }

    public String generate() {
        int n = random.nextInt(1_000_000);  // 0..999999
        return String.format("%06d", n);
    }
}
