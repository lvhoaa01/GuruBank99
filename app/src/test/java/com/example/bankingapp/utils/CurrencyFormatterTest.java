package com.example.bankingapp.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CurrencyFormatterTest {

    @Test public void zero() { assertEquals("0 VND", CurrencyFormatter.formatVnd(0L)); }
    @Test public void thousands() { assertEquals("1,000 VND", CurrencyFormatter.formatVnd(1_000L)); }
    @Test public void millions() { assertEquals("1,234,567 VND", CurrencyFormatter.formatVnd(1_234_567L)); }
}
