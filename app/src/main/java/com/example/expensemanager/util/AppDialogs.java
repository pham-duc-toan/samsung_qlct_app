package com.example.expensemanager.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.expensemanager.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Shared dialogs used from more than one screen. */
public class AppDialogs {

    public interface OnAmount {
        /** value is the entered amount, or 0 when the field is cleared/emptied. */
        void onAmount(long value);
    }

    public interface OnBudgetChanged {
        void onChanged();
    }

    /** Generic "enter an amount" dialog. Shows a "clear" button when current > 0. */
    public static void showAmount(Context context, CharSequence title, long current, OnAmount callback) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_budget, null, false);
        EditText input = view.findViewById(R.id.et_budget);
        if (current > 0) {
            input.setText(String.valueOf(current));
            input.setSelection(input.getText().length());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    long value;
                    try {
                        value = Long.parseLong(input.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        value = 0;
                    }
                    if (callback != null) {
                        callback.onAmount(Math.max(0L, value));
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        if (current > 0) {
            builder.setNeutralButton(R.string.clear, (dialog, which) -> {
                if (callback != null) {
                    callback.onAmount(0);
                }
            });
        }

        builder.show();
    }

    /** Set or clear the overall monthly budget. */
    public static void showBudget(Context context, Prefs prefs, OnBudgetChanged callback) {
        showAmount(context, context.getString(R.string.set_budget), prefs.getBudget(), value -> {
            prefs.setBudget(value);
            if (callback != null) {
                callback.onChanged();
            }
        });
    }
}
