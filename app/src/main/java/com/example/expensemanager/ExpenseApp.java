package com.example.expensemanager;

import android.app.Application;

import com.example.expensemanager.model.Category;
import com.example.expensemanager.util.Prefs;
import com.example.expensemanager.util.ReminderScheduler;

/** Applies the saved theme, loads categories and creates the notification channel on startup. */
public class ExpenseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new Prefs(this).applyNightMode();
        Category.loadFromDatabase(this);
        ReminderScheduler.createChannel(this);
    }
}
