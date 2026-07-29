package com.example.expensemanager.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Thin wrapper over SharedPreferences for the monthly budget and theme mode. */
public class Prefs {

    private static final String FILE = "settings";
    private static final String KEY_BUDGET = "monthly_budget";
    private static final String KEY_NIGHT = "night_mode";

    private final SharedPreferences sp;

    public Prefs(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    // ---- Monthly budget (0 = not set) ----

    public long getBudget() {
        return sp.getLong(KEY_BUDGET, 0L);
    }

    public void setBudget(long value) {
        sp.edit().putLong(KEY_BUDGET, Math.max(0L, value)).apply();
    }

    public boolean hasBudget() {
        return getBudget() > 0L;
    }

    // ---- Theme (values are AppCompatDelegate.MODE_NIGHT_* constants) ----

    public int getNightMode() {
        return sp.getInt(KEY_NIGHT, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setNightMode(int mode) {
        sp.edit().putInt(KEY_NIGHT, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public void applyNightMode() {
        AppCompatDelegate.setDefaultNightMode(getNightMode());
    }

    // ---- Per-category budget (0 = not set) ----

    public long getCategoryBudget(String categoryKey) {
        return sp.getLong("budget_cat_" + categoryKey, 0L);
    }

    public void setCategoryBudget(String categoryKey, long value) {
        sp.edit().putLong("budget_cat_" + categoryKey, Math.max(0L, value)).apply();
    }

    // ---- Daily reminder ----

    private static final String KEY_REMINDER_ON = "reminder_on";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";

    public boolean isReminderEnabled() {
        return sp.getBoolean(KEY_REMINDER_ON, false);
    }

    public void setReminderEnabled(boolean enabled) {
        sp.edit().putBoolean(KEY_REMINDER_ON, enabled).apply();
    }

    public int getReminderHour() {
        return sp.getInt(KEY_REMINDER_HOUR, 20);
    }

    public int getReminderMinute() {
        return sp.getInt(KEY_REMINDER_MINUTE, 0);
    }

    public void setReminderTime(int hour, int minute) {
        sp.edit().putInt(KEY_REMINDER_HOUR, hour).putInt(KEY_REMINDER_MINUTE, minute).apply();
    }

    // ---- App lock (PIN stored as a SHA-256 hash, never in plain text) ----

    private static final String KEY_PIN = "pin_hash";

    public boolean isLockEnabled() {
        return sp.getString(KEY_PIN, null) != null;
    }

    public void setPin(String pin) {
        sp.edit().putString(KEY_PIN, sha256(pin)).apply();
    }

    public void clearPin() {
        sp.edit().remove(KEY_PIN).apply();
    }

    public boolean checkPin(String pin) {
        String stored = sp.getString(KEY_PIN, null);
        return stored != null && stored.equals(sha256(pin));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return value; // extremely unlikely; SHA-256 is always available
        }
    }
}
