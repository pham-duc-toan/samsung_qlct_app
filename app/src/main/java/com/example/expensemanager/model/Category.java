package com.example.expensemanager.model;

import androidx.annotation.NonNull;

import com.example.expensemanager.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Predefined categories. Kept in code (not in the DB) to keep the app simple:
 * each category has a stable key, a localized name, an icon and a color.
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

    static {
        // Expense categories
        register(new Category("food", R.string.cat_food, R.drawable.ic_food, R.color.cat_food, false));
        register(new Category("coffee", R.string.cat_coffee, R.drawable.ic_coffee, R.color.cat_coffee, false));
        register(new Category("transport", R.string.cat_transport, R.drawable.ic_transport, R.color.cat_transport, false));
        register(new Category("shopping", R.string.cat_shopping, R.drawable.ic_shopping, R.color.cat_shopping, false));
        register(new Category("bills", R.string.cat_bills, R.drawable.ic_bills, R.color.cat_bills, false));
        register(new Category("entertainment", R.string.cat_entertainment, R.drawable.ic_entertainment, R.color.cat_entertainment, false));
        register(new Category("health", R.string.cat_health, R.drawable.ic_health, R.color.cat_health, false));
        register(new Category("education", R.string.cat_education, R.drawable.ic_education, R.color.cat_education, false));
        register(new Category("home", R.string.cat_home, R.drawable.ic_home_cat, R.color.cat_home, false));
        register(new Category("other", R.string.cat_other, R.drawable.ic_other, R.color.cat_other, false));

        // Income categories
        register(new Category("salary", R.string.cat_salary, R.drawable.ic_salary, R.color.cat_salary, true));
        register(new Category("bonus", R.string.cat_bonus, R.drawable.ic_bonus, R.color.cat_bonus, true));
        register(new Category("invest", R.string.cat_invest, R.drawable.ic_invest, R.color.cat_invest, true));
        register(new Category("gift", R.string.cat_gift, R.drawable.ic_gift, R.color.cat_gift, true));
        register(new Category("other_income", R.string.cat_other_income, R.drawable.ic_other, R.color.cat_other, true));
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
