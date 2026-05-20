package com.example.bankingapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class OtpGeneratorTest {

    @Test public void otp_isSixDigits() {
        String otp = new OtpGenerator(new Random(0)).generate();
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test public void otp_isDeterministicWithSeed() {
        String a = new OtpGenerator(new Random(123L)).generate();
        String b = new OtpGenerator(new Random(123L)).generate();
        assertEquals(a, b);
    }

    @Test public void otp_distinctOnDifferentSeeds() {
        Set<String> codes = new HashSet<>();
        for (int seed = 0; seed < 20; seed++) {
            codes.add(new OtpGenerator(new Random(seed)).generate());
        }
        assertTrue("Expected several distinct OTPs, got: " + codes, codes.size() > 10);
    }
}
