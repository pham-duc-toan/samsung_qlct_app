package com.example.expensemanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.expensemanager.model.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Local SQLite storage. Single table of transactions, no backend, no network.
 * Exposed as a singleton so every screen shares one connection.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "expense_manager.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "transactions";
    public static final String COL_ID = "id";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_TYPE = "type";
    public static final String COL_CATEGORY = "category";
    public static final String COL_NOTE = "note";
    public static final String COL_DATE = "date";

    private static final String ORDER_NEWEST = COL_DATE + " DESC, " + COL_ID + " DESC";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_TYPE + " TEXT NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_NOTE + " TEXT, " +
                COL_DATE + " INTEGER NOT NULL)");
        seedSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    /** A handful of demo rows so the first launch isn't an empty screen. */
    private void seedSampleData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;
        insertRaw(db, 8500000, Transaction.TYPE_INCOME, "salary", "Lương tháng", now - day);
        insertRaw(db, 65000, Transaction.TYPE_EXPENSE, "food", "Cơm trưa văn phòng", now - day);
        insertRaw(db, 35000, Transaction.TYPE_EXPENSE, "coffee", "Cà phê sáng", now - 2 * day);
        insertRaw(db, 250000, Transaction.TYPE_EXPENSE, "transport", "Đổ xăng xe", now - 3 * day);
        insertRaw(db, 450000, Transaction.TYPE_EXPENSE, "shopping", "Mua áo", now - 4 * day);
        insertRaw(db, 200000, Transaction.TYPE_EXPENSE, "bills", "Tiền điện", now - 5 * day);
        insertRaw(db, 120000, Transaction.TYPE_EXPENSE, "entertainment", "Xem phim", now - 6 * day);
        insertRaw(db, 500000, Transaction.TYPE_INCOME, "bonus", "Thưởng dự án", now - 7 * day);
    }

    private void insertRaw(SQLiteDatabase db, double amount, String type, String cat, String note, long date) {
        ContentValues v = new ContentValues();
        v.put(COL_AMOUNT, amount);
        v.put(COL_TYPE, type);
        v.put(COL_CATEGORY, cat);
        v.put(COL_NOTE, note);
        v.put(COL_DATE, date);
        db.insert(TABLE, null, v);
    }

    // ------------------------------------------------------------------ CRUD

    public long insert(Transaction t) {
        long id = getWritableDatabase().insert(TABLE, null, toValues(t));
        t.id = id;
        return id;
    }

    public int update(Transaction t) {
        return getWritableDatabase().update(TABLE, toValues(t),
                COL_ID + "=?", new String[]{String.valueOf(t.id)});
    }

    public int delete(long id) {
        return getWritableDatabase().delete(TABLE,
                COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    /** Remove every transaction. */
    public int deleteAll() {
        return getWritableDatabase().delete(TABLE, null, null);
    }

    private ContentValues toValues(Transaction t) {
        ContentValues v = new ContentValues();
        v.put(COL_AMOUNT, t.amount);
        v.put(COL_TYPE, t.type);
        v.put(COL_CATEGORY, t.categoryKey);
        v.put(COL_NOTE, t.note);
        v.put(COL_DATE, t.date);
        return v;
    }

    // --------------------------------------------------------------- Queries

    /** All transactions, newest first. */
    public List<Transaction> getAll() {
        return query(null, null);
    }

    /** Transactions in the half-open range [startMillis, endMillis), newest first. */
    public List<Transaction> getBetween(long startMillis, long endMillis) {
        return query(COL_DATE + " >= ? AND " + COL_DATE + " < ?",
                new String[]{String.valueOf(startMillis), String.valueOf(endMillis)});
    }

    /** The most recent N transactions. */
    public List<Transaction> getRecent(int limit) {
        List<Transaction> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE, null, null, null, null, null,
                ORDER_NEWEST, String.valueOf(limit));
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();
        return list;
    }

    private List<Transaction> query(String selection, String[] args) {
        List<Transaction> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE, null, selection, args, null, null, ORDER_NEWEST);
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();
        return list;
    }

    /** Single transaction by id, or null if it no longer exists. */
    public Transaction getById(long id) {
        Cursor c = getReadableDatabase().query(TABLE, null,
                COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Transaction t = c.moveToFirst() ? fromCursor(c) : null;
        c.close();
        return t;
    }

    private Transaction fromCursor(Cursor c) {
        Transaction t = new Transaction();
        t.id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
        t.amount = c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT));
        t.type = c.getString(c.getColumnIndexOrThrow(COL_TYPE));
        t.categoryKey = c.getString(c.getColumnIndexOrThrow(COL_CATEGORY));
        t.note = c.getString(c.getColumnIndexOrThrow(COL_NOTE));
        t.date = c.getLong(c.getColumnIndexOrThrow(COL_DATE));
        return t;
    }

    // ------------------------------------------------------------ Aggregates

    /** Sum of a type across all time. */
    public double totalOf(String type) {
        return sum(COL_TYPE + "=?", new String[]{type});
    }

    /** Sum of a type within [startMillis, endMillis). */
    public double totalOf(String type, long startMillis, long endMillis) {
        return sum(COL_TYPE + "=? AND " + COL_DATE + " >= ? AND " + COL_DATE + " < ?",
                new String[]{type, String.valueOf(startMillis), String.valueOf(endMillis)});
    }

    private double sum(String selection, String[] args) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(" + COL_AMOUNT + "),0) FROM " + TABLE +
                        (selection != null ? " WHERE " + selection : ""), args);
        double total = 0;
        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        return total;
    }

    /** Expense totals grouped by category within a range, largest first. */
    public LinkedHashMap<String, Double> expenseByCategory(long startMillis, long endMillis) {
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") AS total FROM " + TABLE +
                        " WHERE " + COL_TYPE + "=? AND " + COL_DATE + " >= ? AND " + COL_DATE + " < ?" +
                        " GROUP BY " + COL_CATEGORY + " ORDER BY total DESC",
                new String[]{Transaction.TYPE_EXPENSE, String.valueOf(startMillis), String.valueOf(endMillis)});
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getDouble(1));
        }
        c.close();
        return map;
    }
}
