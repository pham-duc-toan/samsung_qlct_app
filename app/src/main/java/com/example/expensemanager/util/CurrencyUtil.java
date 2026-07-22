package com.example.expensemanager.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formats numbers as Vietnamese đồng, e.g. 1500000 -> "1.500.000 đ". */
public class CurrencyUtil {

    // Vietnamese currency suffix. Source files are UTF-8 (AGP default), so this literal is safe.
    private static final String SYMBOL = "đ";
    private static final DecimalFormat GROUPED;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        GROUPED = new DecimalFormat("#,###", symbols);
    }

    public static String format(double amount) {
        return GROUPED.format(Math.round(amount)) + " " + SYMBOL;
    }

    /** Prefixed with + or - depending on sign. */
    public static String formatSigned(double signedAmount) {
        long rounded = Math.round(signedAmount);
        String sign = rounded > 0 ? "+" : (rounded < 0 ? "-" : "");
        return sign + GROUPED.format(Math.abs(rounded)) + " " + SYMBOL;
    }

    /** Compact form for tight chart labels, e.g. 1500000 -> "1,5M", 25000 -> "25K". */
    public static String formatShort(double amount) {
        double abs = Math.abs(amount);
        DecimalFormat oneDecimal = new DecimalFormat("#.#");
        if (abs >= 1_000_000d) {
            return oneDecimal.format(amount / 1_000_000d) + "M";
        }
        if (abs >= 1_000d) {
            return oneDecimal.format(amount / 1_000d) + "K";
        }
        return String.valueOf(Math.round(amount));
    }
}
