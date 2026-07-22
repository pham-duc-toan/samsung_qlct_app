package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.expensemanager.fragment.HomeFragment;
import com.example.expensemanager.fragment.SettingsFragment;
import com.example.expensemanager.fragment.StatsFragment;
import com.example.expensemanager.fragment.TransactionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Single-activity host. Swaps between the three fragments via the bottom nav
 * and launches the add screen from the floating button.
 */
public class MainActivity extends AppCompatActivity {

    private int currentNavId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            showFragment(item.getItemId());
            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class)));

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void showFragment(int navId) {
        if (navId == currentNavId) {
            return;
        }
        currentNavId = navId;

        Fragment fragment;
        if (navId == R.id.nav_transactions) {
            fragment = new TransactionsFragment();
        } else if (navId == R.id.nav_stats) {
            fragment = new StatsFragment();
        } else if (navId == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else {
            fragment = new HomeFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
