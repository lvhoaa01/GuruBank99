package com.example.bankingapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateFormatter {

    private DateFormatter() { }

    private static final SimpleDateFormat DISPLAY =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

    public static String format(long millis) {
        return DISPLAY.format(new Date(millis));
    }
}
