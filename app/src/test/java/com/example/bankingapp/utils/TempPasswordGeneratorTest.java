package com.example.bankingapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TempPasswordGeneratorTest {

    @Test public void generate_isTenCharacters() {
        String pwd = new TempPasswordGenerator(new Random(0)).generate();
        assertNotNull(pwd);
        assertEquals(TempPasswordGenerator.LENGTH, pwd.length());
    }

    @Test public void generate_containsRequiredClasses() {
        String pwd = new TempPasswordGenerator(new Random(0)).generate();
        assertTrue("Missing uppercase: " + pwd, pwd.matches(".*[A-Z].*"));
        assertTrue("Missing lowercase: " + pwd, pwd.matches(".*[a-z].*"));
        assertTrue("Missing digit: "    + pwd, pwd.matches(".*\\d.*"));
        assertTrue("Missing special: "  + pwd, pwd.matches(".*[^A-Za-z0-9].*"));
    }

    @Test public void generate_deterministicWithSameSeed() {
        String a = new TempPasswordGenerator(new Random(123L)).generate();
        String b = new TempPasswordGenerator(new Random(123L)).generate();
        assertEquals(a, b);
    }

    @Test public void generate_distinctAcrossSeeds() {
        Set<String> pwds = new HashSet<>();
        for (int seed = 0; seed < 30; seed++) {
            pwds.add(new TempPasswordGenerator(new Random(seed)).generate());
        }
        assertTrue("Expected many distinct passwords: " + pwds.size(), pwds.size() > 20);
    }
}
