package com.example.expensemanager.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.expensemanager.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Categories are now stored in SQLite (see {@link DatabaseHelper}) rather than
 * hardcoded here. {@link #loadFromDatabase(Context)} reads them once at startup
 * and fills this in-memory registry; each category keeps a stable key plus the
 * resolved name/icon/color resource ids so the rest of the UI is unchanged.
 */
public class Category {

    public final String key;
    public final int nameRes;
    public final int iconRes;
    public final int colorRes;
    public final boolean income;

    public Category(String key, int nameRes, int iconRes, int colorRes, boolean income) {
        this.key = key;
        this.nameRes = nameRes;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.income = income;
    }

    private static final LinkedHashMap<String, Category> ALL = new LinkedHashMap<>();
    private static final List<Category> EXPENSE = new ArrayList<>();
    private static final List<Category> INCOME = new ArrayList<>();

    private static void register(Category c) {
        ALL.put(c.key, c);
        if (c.income) {
            INCOME.add(c);
        } else {
            EXPENSE.add(c);
        }
    }

    /** Load category definitions from the database. Call once at app startup. */
    public static synchronized void loadFromDatabase(Context context) {
        List<Category> loaded = DatabaseHelper.getInstance(context).getCategories(context);
        ALL.clear();
        EXPENSE.clear();
        INCOME.clear();
        for (Category c : loaded) {
            register(c);
        }
    }

    public static List<Category> expenseCategories() {
        return EXPENSE;
    }

    public static List<Category> incomeCategories() {
        return INCOME;
    }

    public static List<Category> forType(String type) {
        return Transaction.TYPE_INCOME.equals(type) ? INCOME : EXPENSE;
    }

    /** Never returns null: unknown keys fall back to "Other" so the UI can't crash. */
    @NonNull
    public static Category byKey(String key) {
        Category c = key == null ? null : ALL.get(key);
        return c != null ? c : ALL.get("other");
    }
}
