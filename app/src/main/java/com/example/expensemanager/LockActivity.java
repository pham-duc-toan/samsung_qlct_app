package com.example.expensemanager;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensemanager.util.Prefs;

/** Full-screen PIN gate shown before the app can be used when a lock is set. */
public class LockActivity extends AppCompatActivity {

    private Prefs prefs;
    private EditText etPin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        prefs = new Prefs(this);
        etPin = findViewById(R.id.et_pin);

        findViewById(R.id.btn_unlock).setOnClickListener(v -> tryUnlock());
    }

    private void tryUnlock() {
        String pin = etPin.getText().toString();
        if (prefs.checkPin(pin)) {
            ExpenseApp.unlocked = true;
            finish();
        } else {
            etPin.setText("");
            Toast.makeText(this, R.string.pin_wrong, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Don't let Back bypass the lock — send the app to the background instead.
        moveTaskToBack(true);
    }
}
