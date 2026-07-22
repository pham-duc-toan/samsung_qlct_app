package com.example.expensemanager.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.expensemanager.R;
import com.example.expensemanager.receiver.ReminderReceiver;

import java.util.Calendar;

/** Schedules (and cancels) the once-a-day reminder alarm and owns the channel id. */
public class ReminderScheduler {

    public static final String CHANNEL_ID = "daily_reminder";
    public static final int NOTIFICATION_ID = 1001;
    private static final int REQUEST_CODE = 2001;

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    /** Schedule a repeating daily reminder at the given time (next occurrence). */
    public static void schedule(Context context, int hour, int minute) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            return;
        }
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        if (time.getTimeInMillis() <= System.currentTimeMillis()) {
            time.add(Calendar.DAY_OF_MONTH, 1);
        }
        // Inexact repeating avoids needing the SCHEDULE_EXACT_ALARM permission.
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent(context));
    }

    public static void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(pendingIntent(context));
        }
    }

    private static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags);
    }
}
