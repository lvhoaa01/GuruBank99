package com.example.bankingapp.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates account numbers of the form "99" + 10 digits (SRS 3.4).
 * The seeded mode is used by tests; the no-arg constructor uses a
 * sequential counter so generated numbers are easy to read in logs.
 */
public class AccountNumberGenerator {

    private final AtomicLong sequence = new AtomicLong(10L);
    private final Random random;
    private final boolean sequential;

    public AccountNumberGenerator() {
        this(null, true);
    }

    public AccountNumberGenerator(Random random) {
        this(random, false);
    }

    private AccountNumberGenerator(Random random, boolean sequential) {
        this.random = random;
        this.sequential = sequential;
    }

    public synchronized String next() {
        long n;
        if (sequential) {
            n = sequence.getAndIncrement();
        } else {
            // 10 digits → 0..9_999_999_999
            n = (long) (random.nextDouble() * 10_000_000_000L);
        }
        return "99" + String.format("%010d", n);
    }
}
