package com.example.expensemanager.fragment;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.expensemanager.CategoryBudgetActivity;
import com.example.expensemanager.R;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.util.AppDialogs;
import com.example.expensemanager.util.CsvExporter;
import com.example.expensemanager.util.CurrencyUtil;
import com.example.expensemanager.util.Prefs;
import com.example.expensemanager.util.ReminderScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.Locale;

/** Settings: budgets, light/dark theme, daily reminder, CSV export, clear data. */
public class SettingsFragment extends Fragment {

    private static final int[] THEME_MODES = {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
    };

    private Prefs prefs;
    private DatabaseHelper db;
    private TextView tvBudgetValue;
    private TextView tvThemeValue;
    private TextView tvReminderValue;
    private SwitchMaterial switchReminder;

    // Runtime POST_NOTIFICATIONS request (Android 13+).
    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    doEnableReminder();
                } else {
                    switchReminder.setChecked(false);
                    Toast.makeText(requireContext(), R.string.notif_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefs = new Prefs(requireContext());
        db = DatabaseHelper.getInstance(requireContext());

        tvBudgetValue = view.findViewById(R.id.tv_budget_value);
        tvThemeValue = view.findViewById(R.id.tv_theme_value);
        tvReminderValue = view.findViewById(R.id.tv_reminder_value);
        switchReminder = view.findViewById(R.id.switch_reminder);

        view.findViewById(R.id.row_budget).setOnClickListener(v ->
                AppDialogs.showBudget(requireContext(), prefs, this::refresh));
        view.findViewById(R.id.row_category_budget).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CategoryBudgetActivity.class)));
        view.findViewById(R.id.row_theme).setOnClickListener(v -> showThemeDialog());
        view.findViewById(R.id.row_reminder).setOnClickListener(v -> showTimePicker());
        switchReminder.setOnClickListener(v -> onReminderToggled());
        view.findViewById(R.id.row_export).setOnClickListener(v -> exportCsv());
        view.findViewById(R.id.row_delete_all).setOnClickListener(v -> confirmDeleteAll());

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        tvBudgetValue.setText(prefs.hasBudget()
                ? CurrencyUtil.format(prefs.getBudget())
                : getString(R.string.budget_not_set));
        tvThemeValue.setText(themeLabel(prefs.getNightMode()));

        switchReminder.setChecked(prefs.isReminderEnabled());
        tvReminderValue.setText(prefs.isReminderEnabled()
                ? getString(R.string.reminder_at, formatTime(prefs.getReminderHour(), prefs.getReminderMinute()))
                : getString(R.string.reminder_off));
    }

    // ---------------------------------------------------------------- Theme

    private String themeLabel(int mode) {
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            return getString(R.string.theme_light);
        }
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            return getString(R.string.theme_dark);
        }
        return getString(R.string.theme_system);
    }

    private void showThemeDialog() {
        String[] labels = {
                getString(R.string.theme_system),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };
        int current = 0;
        for (int i = 0; i < THEME_MODES.length; i++) {
            if (THEME_MODES[i] == prefs.getNightMode()) {
                current = i;
                break;
            }
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.appearance)
                .setSingleChoiceItems(labels, current, (dialog, which) -> {
                    dialog.dismiss();
                    prefs.setNightMode(THEME_MODES[which]); // recreates the activity
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ------------------------------------------------------------- Reminder

    private void onReminderToggled() {
        if (!switchReminder.isChecked()) {
            prefs.setReminderEnabled(false);
            ReminderScheduler.cancel(requireContext());
            refresh();
            return;
        }
        // Turning on: ask for notification permission on Android 13+ first.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            doEnableReminder();
        }
    }

    private void doEnableReminder() {
        prefs.setReminderEnabled(true);
        ReminderScheduler.schedule(requireContext(), prefs.getReminderHour(), prefs.getReminderMinute());
        refresh();
    }

    private void showTimePicker() {
        new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            prefs.setReminderTime(hour, minute);
            if (prefs.isReminderEnabled()) {
                ReminderScheduler.schedule(requireContext(), hour, minute);
            }
            refresh();
        }, prefs.getReminderHour(), prefs.getReminderMinute(), true).show();
    }

    private String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    // --------------------------------------------------------------- Data

    private void exportCsv() {
        List<Transaction> all = db.getAll();
        boolean ok = CsvExporter.export(requireContext(), all);
        if (!ok) {
            int msg = all.isEmpty() ? R.string.export_empty : R.string.export_fail;
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteAll() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_all)
                .setMessage(R.string.delete_all_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deleteAll();
                    Toast.makeText(requireContext(), R.string.delete_all_done, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .show();
    }
}
