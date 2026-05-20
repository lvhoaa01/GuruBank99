package com.example.bankingapp.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TellerIdGeneratorTest {

    @Test public void next_isMonotonic() {
        TellerIdGenerator g = new TellerIdGenerator(1);
        assertEquals(1, g.next());
        assertEquals(2, g.next());
        assertEquals(3, g.next());
    }

    @Test public void advanceTo_skipsForwardOnly() {
        TellerIdGenerator g = new TellerIdGenerator(5);
        g.advanceTo(3);            // stays at 5 (lower ignored)
        assertEquals(5, g.next());
        g.advanceTo(10);
        assertEquals(10, g.next());
    }

    @Test public void formatUsername_padsToThreeDigits() {
        assertEquals("T001", TellerIdGenerator.formatUsername(1));
        assertEquals("T042", TellerIdGenerator.formatUsername(42));
        assertEquals("T123", TellerIdGenerator.formatUsername(123));
    }
}
