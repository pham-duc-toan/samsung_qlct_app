package com.example.expensemanager.model;

/**
 * A single income or expense entry.
 * Amount is always stored as a positive number; {@link #type} tells the direction.
 */
public class Transaction {

    public static final String TYPE_INCOME = "income";
    public static final String TYPE_EXPENSE = "expense";

    public long id;
    public double amount;        // always positive
    public String type;          // TYPE_INCOME or TYPE_EXPENSE
    public String categoryKey;   // key into Category
    public String note;
    public long date;            // epoch millis

    public Transaction() {
    }

    public Transaction(long id, double amount, String type, String categoryKey, String note, long date) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.categoryKey = categoryKey;
        this.note = note;
        this.date = date;
    }

    public boolean isIncome() {
        return TYPE_INCOME.equals(type);
    }

    public Category category() {
        return Category.byKey(categoryKey);
    }

    /** Positive for income, negative for expense. */
    public double signedAmount() {
        return isIncome() ? amount : -amount;
    }
}
