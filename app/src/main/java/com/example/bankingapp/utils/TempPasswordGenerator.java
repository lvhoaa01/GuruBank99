package com.example.bankingapp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generate a 10-character temporary password per SRS 3.4: must contain
 * at least one uppercase letter, one lowercase letter, one digit, and one
 * special character. The {@link Random} is injectable so unit tests can
 * seed it for deterministic output.
 */
public class TempPasswordGenerator {

    public static final int LENGTH = 10;
    private static final String UPPER   = "ABCDEFGHJKLMNPQRSTUVWXYZ";   // exclude I, O for legibility
    private static final String LOWER   = "abcdefghijkmnpqrstuvwxyz";   // exclude l, o
    private static final String DIGITS  = "23456789";                    // exclude 0, 1
    private static final String SPECIAL = "!@#$%^&*?+-";

    private final Random random;

    public TempPasswordGenerator() {
        this(new Random());
    }

    public TempPasswordGenerator(Random random) {
        this.random = random;
    }

    public String generate() {
        List<Character> chars = new ArrayList<>(LENGTH);
        chars.add(pick(UPPER));
        chars.add(pick(LOWER));
        chars.add(pick(DIGITS));
        chars.add(pick(SPECIAL));

        String all = UPPER + LOWER + DIGITS + SPECIAL;
        while (chars.size() < LENGTH) {
            chars.add(pick(all));
        }

        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder(LENGTH);
        for (Character c : chars) sb.append(c);
        return sb.toString();
    }

    private char pick(String alphabet) {
        return alphabet.charAt(random.nextInt(alphabet.length()));
    }
}
