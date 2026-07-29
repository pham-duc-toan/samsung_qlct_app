package com.example.expensemanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.expensemanager.model.Category;
import com.example.expensemanager.model.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Local SQLite storage. Two tables — transactions and categories — no backend,
 * no network. Exposed as a singleton so every screen shares one connection.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "expense_manager.db";
    private static final int DB_VERSION = 2;

    public static final String TABLE = "transactions";
    public static final String COL_ID = "id";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_TYPE = "type";
    public static final String COL_CATEGORY = "category";
    public static final String COL_NOTE = "note";
    public static final String COL_DATE = "date";

    // Category table: definitions now live in the DB instead of being hardcoded.
    // name/icon/color hold resource entry names (e.g. "cat_food", "ic_food") that
    // are resolved back to resource ids at load time, so the visuals stay intact.
    public static final String TABLE_CAT = "categories";
    public static final String CAT_KEY = "cat_key";
    public static final String CAT_NAME = "name";
    public static final String CAT_ICON = "icon";
    public static final String CAT_COLOR = "color";
    public static final String CAT_INCOME = "income";
    public static final String CAT_SORT = "sort_order";

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

        db.execSQL("CREATE TABLE " + TABLE_CAT + " (" +
                CAT_KEY + " TEXT PRIMARY KEY, " +
                CAT_NAME + " TEXT NOT NULL, " +
                CAT_ICON + " TEXT NOT NULL, " +
                CAT_COLOR + " TEXT NOT NULL, " +
                CAT_INCOME + " INTEGER NOT NULL, " +
                CAT_SORT + " INTEGER NOT NULL)");

        seedCategories(db);
        seedSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAT);
        onCreate(db);
    }

    // ----------------------------------------------------------- Category seed

    /** The predefined categories, now stored in the DB instead of in code. */
    private void seedCategories(SQLiteDatabase db) {
        int order = 0;
        // Expense categories
        insertCat(db, "food", "cat_food", "ic_food", "cat_food", 0, order++);
        insertCat(db, "coffee", "cat_coffee", "ic_coffee", "cat_coffee", 0, order++);
        insertCat(db, "transport", "cat_transport", "ic_transport", "cat_transport", 0, order++);
        insertCat(db, "shopping", "cat_shopping", "ic_shopping", "cat_shopping", 0, order++);
        insertCat(db, "bills", "cat_bills", "ic_bills", "cat_bills", 0, order++);
        insertCat(db, "entertainment", "cat_entertainment", "ic_entertainment", "cat_entertainment", 0, order++);
        insertCat(db, "health", "cat_health", "ic_health", "cat_health", 0, order++);
        insertCat(db, "education", "cat_education", "ic_education", "cat_education", 0, order++);
        insertCat(db, "home", "cat_home", "ic_home_cat", "cat_home", 0, order++);
        insertCat(db, "other", "cat_other", "ic_other", "cat_other", 0, order++);
        // Income categories
        insertCat(db, "salary", "cat_salary", "ic_salary", "cat_salary", 1, order++);
        insertCat(db, "bonus", "cat_bonus", "ic_bonus", "cat_bonus", 1, order++);
        insertCat(db, "invest", "cat_invest", "ic_invest", "cat_invest", 1, order++);
        insertCat(db, "gift", "cat_gift", "ic_gift", "cat_gift", 1, order++);
        insertCat(db, "other_income", "cat_other_income", "ic_other", "cat_other", 1, order++);
    }

    private void insertCat(SQLiteDatabase db, String key, String name, String icon,
                           String color, int income, int sort) {
        ContentValues v = new ContentValues();
        v.put(CAT_KEY, key);
        v.put(CAT_NAME, name);
        v.put(CAT_ICON, icon);
        v.put(CAT_COLOR, color);
        v.put(CAT_INCOME, income);
        v.put(CAT_SORT, sort);
        db.insert(TABLE_CAT, null, v);
    }

    /**
     * Raw category row. Holds the resolved display name (from a string resource,
     * or the literal text for user-added categories) plus the icon/color resource
     * entry names, which the category editor needs to preselect its pickers.
     */
    public static class CategoryDef {
        public final String key;
        public final String displayName;
        public final String icon;
        public final String color;
        public final boolean income;

        public CategoryDef(String key, String displayName, String icon, String color, boolean income) {
            this.key = key;
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
            this.income = income;
        }
    }

    /** All category rows, ordered, with names resolved to display text. */
    public List<CategoryDef> getCategoryDefs(Context context) {
        List<CategoryDef> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE_CAT, null, null, null, null, null, CAT_SORT + " ASC");
        while (c.moveToNext()) {
            list.add(defFromCursor(context, c));
        }
        c.close();
        return list;
    }

    /** A single category row by key, or null if it no longer exists. */
    public CategoryDef getCategoryDef(Context context, String key) {
        Cursor c = getReadableDatabase().query(TABLE_CAT, null,
                CAT_KEY + "=?", new String[]{key}, null, null, null);
        CategoryDef def = c.moveToFirst() ? defFromCursor(context, c) : null;
        c.close();
        return def;
    }

    private CategoryDef defFromCursor(Context context, Cursor c) {
        String key = c.getString(c.getColumnIndexOrThrow(CAT_KEY));
        String name = c.getString(c.getColumnIndexOrThrow(CAT_NAME));
        String icon = c.getString(c.getColumnIndexOrThrow(CAT_ICON));
        String color = c.getString(c.getColumnIndexOrThrow(CAT_COLOR));
        boolean income = c.getInt(c.getColumnIndexOrThrow(CAT_INCOME)) == 1;
        // Seeded rows store a string-resource entry name; custom rows store literal text.
        int nameResId = context.getResources().getIdentifier(name, "string", context.getPackageName());
        String display = nameResId != 0 ? context.getString(nameResId) : name;
        return new CategoryDef(key, display, icon, color, income);
    }

    /** Resolves category rows into ready-to-render {@link Category} objects. */
    public List<Category> getCategories(Context context) {
        List<Category> list = new ArrayList<>();
        String pkg = context.getPackageName();
        Resources res = context.getResources();
        for (CategoryDef d : getCategoryDefs(context)) {
            int iconRes = res.getIdentifier(d.icon, "drawable", pkg);
            int colorRes = res.getIdentifier(d.color, "color", pkg);
            list.add(new Category(d.key, d.displayName, iconRes, colorRes, d.income));
        }
        return list;
    }

    public void insertCategory(String key, String name, String icon, String color, boolean income) {
        ContentValues v = new ContentValues();
        v.put(CAT_KEY, key);
        v.put(CAT_NAME, name);
        v.put(CAT_ICON, icon);
        v.put(CAT_COLOR, color);
        v.put(CAT_INCOME, income ? 1 : 0);
        v.put(CAT_SORT, nextSort());
        getWritableDatabase().insert(TABLE_CAT, null, v);
    }

    public int updateCategory(String key, String name, String icon, String color, boolean income) {
        ContentValues v = new ContentValues();
        v.put(CAT_NAME, name);
        v.put(CAT_ICON, icon);
        v.put(CAT_COLOR, color);
        v.put(CAT_INCOME, income ? 1 : 0);
        return getWritableDatabase().update(TABLE_CAT, v, CAT_KEY + "=?", new String[]{key});
    }

    public int deleteCategory(String key) {
        return getWritableDatabase().delete(TABLE_CAT, CAT_KEY + "=?", new String[]{key});
    }

    /** Persists a new ordering: sort_order becomes each key's index in the list. */
    public void setCategoryOrder(List<String> keysInOrder) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < keysInOrder.size(); i++) {
                ContentValues v = new ContentValues();
                v.put(CAT_SORT, i);
                db.update(TABLE_CAT, v, CAT_KEY + "=?", new String[]{keysInOrder.get(i)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private int nextSort() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(MAX(" + CAT_SORT + "),-1)+1 FROM " + TABLE_CAT, null);
        int next = 0;
        if (c.moveToFirst()) {
            next = c.getInt(0);
        }
        c.close();
        return next;
    }

    // -------------------------------------------------------- Transaction seed

    /** Generates a few months of demo transactions so charts and stats look alive. */
    private void seedSampleData(SQLiteDatabase db) {
        Random rnd = new Random(20240729L); // fixed seed → same demo data every install
        String[] cats = {"food", "coffee", "transport", "shopping", "bills",
                "entertainment", "health", "education", "home", "other"};

        for (int back = 7; back >= 0; back--) {
            Calendar m = Calendar.getInstance();
            m.add(Calendar.MONTH, -back);
            int year = m.get(Calendar.YEAR);
            int month0 = m.get(Calendar.MONTH);
            int lastDay = m.getActualMaximum(Calendar.DAY_OF_MONTH);
            int dayLimit = (back == 0) ? m.get(Calendar.DAY_OF_MONTH) : lastDay;

            // Monthly salary near the start of the month.
            insertRaw(db, 8000000 + rnd.nextInt(6) * 250000L, Transaction.TYPE_INCOME, "salary",
                    "Lương tháng " + (month0 + 1), dateAt(year, month0, Math.min(5, dayLimit)));
            // Occasional bonus and investment income.
            if (rnd.nextInt(2) == 0) {
                insertRaw(db, 300000 + rnd.nextInt(8) * 100000L, Transaction.TYPE_INCOME, "bonus",
                        "Thưởng", dateAt(year, month0, 1 + rnd.nextInt(dayLimit)));
            }
            if (rnd.nextInt(3) == 0) {
                insertRaw(db, 500000 + rnd.nextInt(10) * 100000L, Transaction.TYPE_INCOME, "invest",
                        "Lãi đầu tư", dateAt(year, month0, 1 + rnd.nextInt(dayLimit)));
            }

            int count = 18 + rnd.nextInt(12); // 18-29 expenses per month
            for (int i = 0; i < count; i++) {
                String cat = cats[rnd.nextInt(cats.length)];
                int day = 1 + rnd.nextInt(dayLimit);
                insertRaw(db, expenseAmount(cat, rnd), Transaction.TYPE_EXPENSE, cat,
                        expenseNote(cat), dateAt(year, month0, day));
            }
        }
    }

    private static long dateAt(int year, int month0, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month0, day, 12, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static double expenseAmount(String cat, Random rnd) {
        switch (cat) {
            case "food": return 30000 + rnd.nextInt(12) * 10000L;
            case "coffee": return 25000 + rnd.nextInt(6) * 5000L;
            case "transport": return 20000 + rnd.nextInt(10) * 15000L;
            case "shopping": return 150000 + rnd.nextInt(10) * 80000L;
            case "bills": return 100000 + rnd.nextInt(9) * 50000L;
            case "entertainment": return 60000 + rnd.nextInt(8) * 30000L;
            case "health": return 50000 + rnd.nextInt(10) * 40000L;
            case "education": return 200000 + rnd.nextInt(8) * 100000L;
            case "home": return 100000 + rnd.nextInt(10) * 60000L;
            default: return 20000 + rnd.nextInt(10) * 20000L;
        }
    }

    private static String expenseNote(String cat) {
        switch (cat) {
            case "food": return "Ăn uống";
            case "coffee": return "Cà phê";
            case "transport": return "Xăng xe / gửi xe";
            case "shopping": return "Mua sắm";
            case "bills": return "Hóa đơn";
            case "entertainment": return "Giải trí";
            case "health": return "Sức khỏe";
            case "education": return "Học tập";
            case "home": return "Nhà cửa";
            default: return "Chi khác";
        }
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
