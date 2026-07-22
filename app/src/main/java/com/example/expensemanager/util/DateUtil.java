package com.example.expensemanager.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Date helpers: month ranges, formatting and "today / yesterday" grouping. */
public class DateUtil {

    private static final Locale VN = new Locale("vi", "VN");
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    /** First millisecond of the given month (month0 is 0-based: Jan = 0). */
    public static long startOfMonth(int year, int month0) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month0, 1, 0, 0, 0);
        return c.getTimeInMillis();
    }

    /** First millisecond of the following month — the exclusive upper bound. */
    public static long startOfNextMonth(int year, int month0) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month0, 1, 0, 0, 0);
        c.add(Calendar.MONTH, 1);
        return c.getTimeInMillis();
    }

    /** e.g. "Tháng 7, 2026". */
    public static String monthLabel(int year, int month0) {
        return "Tháng " + (month0 + 1) + ", " + year;
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy", VN).format(new Date(millis));
    }

    public static String formatDateLong(long millis) {
        return new SimpleDateFormat("EEEE, dd 'tháng' MM, yyyy", VN).format(new Date(millis));
    }

    /** Section header for a transaction list: "Hôm nay", "Hôm qua" or the date. */
    public static String relativeDay(long millis) {
        long target = dayKey(millis);
        long today = dayKey(System.currentTimeMillis());
        long diffDays = (today - target) / DAY_MS;
        if (diffDays == 0) return "Hôm nay";
        if (diffDays == 1) return "Hôm qua";
        return formatDate(millis);
    }

    /** Midnight of the given day, used as a grouping key. */
    public static long dayKey(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
