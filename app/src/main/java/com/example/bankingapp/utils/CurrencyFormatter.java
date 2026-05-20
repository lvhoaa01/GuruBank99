package com.example.bankingapp.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats VND amounts as "1,234,567 VND" (thousands separator with comma).
 */
public final class CurrencyFormatter {

    private CurrencyFormatter() { }

    private static final DecimalFormat FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        FORMAT = new DecimalFormat("#,###", symbols);
    }

    public static String formatVnd(long amount) {
        return FORMAT.format(amount) + " VND";
    }
}
