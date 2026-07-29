package com.example.expensemanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.adapter.CategoryBudgetAdapter;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.util.AppDialogs;
import com.example.expensemanager.util.DateUtil;
import com.example.expensemanager.util.Prefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

/** Set a spending limit per expense category, with this month's progress shown. */
public class CategoryBudgetActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private Prefs prefs;
    private CategoryBudgetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_budget);

        db = DatabaseHelper.getInstance(this);
        prefs = new Prefs(this);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_category_budget);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryBudgetAdapter(this::onPick);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        Calendar now = Calendar.getInstance();
        long start = DateUtil.startOfMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH));
        long end = DateUtil.startOfNextMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH));
        LinkedHashMap<String, Double> spentByCategory = db.expenseByCategory(start, end);

        List<CategoryBudgetAdapter.Item> items = new ArrayList<>();
        for (Category category : Category.expenseCategories()) {
            double spent = spentByCategory.containsKey(category.key)
                    ? spentByCategory.get(category.key) : 0.0;
            long budget = prefs.getCategoryBudget(category.key);
            items.add(new CategoryBudgetAdapter.Item(category, spent, budget));
        }
        adapter.submit(items);
    }

    private void onPick(CategoryBudgetAdapter.Item item) {
        String title = getString(R.string.set_amount_for, item.category.name);
        AppDialogs.showAmount(this, title, item.budget, value -> {
            prefs.setCategoryBudget(item.category.key, value);
            load();
        });
    }
}
