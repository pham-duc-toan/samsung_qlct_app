package com.example.expensemanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.expensemanager.util.Prefs;
import com.example.expensemanager.util.ReminderScheduler;

/** Re-arms the daily reminder after a device reboot (alarms are cleared on boot). */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Prefs prefs = new Prefs(context);
            if (prefs.isReminderEnabled()) {
                ReminderScheduler.schedule(context, prefs.getReminderHour(), prefs.getReminderMinute());
            }
        }
    }
}
