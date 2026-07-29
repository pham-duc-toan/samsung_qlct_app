package com.example.expensemanager.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.example.expensemanager.AddTransactionActivity;
import com.example.expensemanager.MainActivity;
import com.example.expensemanager.R;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.util.CurrencyUtil;
import com.example.expensemanager.util.DateUtil;

import java.util.Calendar;

/** Home-screen widget: shows the balance and this month's expense, with an add shortcut. */
public class BalanceWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, manager, id);
        }
    }

    /** Re-renders every placed widget. Call after data changes. */
    public static void refresh(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, BalanceWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(provider)) {
            updateWidget(context, manager, id);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        double income = db.totalOf(Transaction.TYPE_INCOME);
        double expense = db.totalOf(Transaction.TYPE_EXPENSE);

        Calendar now = Calendar.getInstance();
        long start = DateUtil.startOfMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH));
        long end = DateUtil.startOfNextMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH));
        double monthExpense = db.totalOf(Transaction.TYPE_EXPENSE, start, end);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_balance);
        views.setTextViewText(R.id.tv_balance, CurrencyUtil.format(income - expense));
        views.setTextViewText(R.id.tv_month_expense, CurrencyUtil.format(monthExpense));
        views.setOnClickPendingIntent(R.id.widget_root, activityIntent(context, MainActivity.class, 0));
        views.setOnClickPendingIntent(R.id.btn_add, activityIntent(context, AddTransactionActivity.class, 1));

        manager.updateAppWidget(widgetId, views);
    }

    private static PendingIntent activityIntent(Context context, Class<?> target, int requestCode) {
        Intent intent = new Intent(context, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }
}
