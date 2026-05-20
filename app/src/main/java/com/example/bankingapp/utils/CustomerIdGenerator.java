package com.example.bankingapp.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Issues Customer IDs starting at 10001 (SRS 3.4).
 */
public class CustomerIdGenerator {

    private final AtomicInteger next;

    public CustomerIdGenerator() {
        this(10001);
    }

    public CustomerIdGenerator(int startInclusive) {
        this.next = new AtomicInteger(startInclusive);
    }

    public int next() {
        return next.getAndIncrement();
    }

    /** Sync after seeding existing users so the next issued id doesn't collide. */
    public void advanceTo(int firstFreeId) {
        if (firstFreeId > next.get()) {
            next.set(firstFreeId);
        }
    }
}
