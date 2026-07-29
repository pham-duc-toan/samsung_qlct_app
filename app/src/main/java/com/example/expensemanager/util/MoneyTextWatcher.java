package com.example.expensemanager.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats an amount field with '.' thousands separators while the user types,
 * e.g. "1500000" is shown as "1.500.000". Parse the real value with {@link #parse}.
 */
public class MoneyTextWatcher implements TextWatcher {

    private final EditText editText;
    private final DecimalFormat format;
    private boolean editing;

    public MoneyTextWatcher(EditText editText) {
        this.editText = editText;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        this.format = new DecimalFormat("#,###", symbols);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        if (editing) {
            return; // our own setText re-entered this callback
        }
        editing = true;
        String digits = s.toString().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            editText.setText("");
        } else {
            if (digits.length() > 15) {
                digits = digits.substring(0, 15);
            }
            String formatted = format.format(Long.parseLong(digits));
            editText.setText(formatted);
            editText.setSelection(formatted.length());
        }
        editing = false;
    }

    /** Strips separators and parses the value; returns 0 when empty or invalid. */
    public static long parse(CharSequence text) {
        if (text == null) {
            return 0;
        }
        String digits = text.toString().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
