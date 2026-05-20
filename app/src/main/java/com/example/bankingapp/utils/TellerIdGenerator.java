package com.example.bankingapp.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Issues sequential MaNV values starting at 1 (SRS 3.4 example shows
 * "T001, T002..."). For runtime, the DB sequence (or
 * {@code TellerRepository.nextTellerId}) is authoritative — this class
 * is mainly used by unit tests to fake the sequence deterministically.
 */
public class TellerIdGenerator {

    private final AtomicInteger next;

    public TellerIdGenerator() {
        this(1);
    }

    public TellerIdGenerator(int startInclusive) {
        this.next = new AtomicInteger(startInclusive);
    }

    public int next() {
        return next.getAndIncrement();
    }

    public void advanceTo(int firstFreeId) {
        if (firstFreeId > next.get()) {
            next.set(firstFreeId);
        }
    }

    /** "T" + 3-digit zero-padded employee id, e.g. T001, T042, T123. */
    public static String formatUsername(int tellerId) {
        return String.format("T%03d", tellerId);
    }
}
