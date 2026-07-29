package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.adapter.CategoryManageAdapter;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Category;

/** Lists all categories and opens the editor to add, edit or delete them. */
public class CategoryManageActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private CategoryManageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manage);

        db = DatabaseHelper.getInstance(this);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddCategoryActivity.class)));

        RecyclerView rv = findViewById(R.id.rv_categories);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryManageAdapter(this::openEdit);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.submit(db.getCategories(this));
    }

    private void openEdit(Category c) {
        startActivity(AddCategoryActivity.editIntent(this, c.key));
    }
}
