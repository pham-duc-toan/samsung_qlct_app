package com.example.expensemanager.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.expensemanager.MainActivity;
import com.example.expensemanager.R;
import com.example.expensemanager.util.ReminderScheduler;

/** Fired by AlarmManager once a day; posts the "log your spending" notification. */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, open, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(context.getString(R.string.reminder_notif_title))
                .setContentText(context.getString(R.string.reminder_notif_text))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(ReminderScheduler.NOTIFICATION_ID, builder.build());
        }
    }
}
