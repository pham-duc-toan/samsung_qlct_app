package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.adapter.CategoryManageAdapter;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Category;

import java.util.ArrayList;
import java.util.List;

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

        // Long-press a row to drag it into a new position; the order is saved on drop.
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                adapter.moveItem(vh.getBindingAdapterPosition(), target.getBindingAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                persistOrder();
            }
        });
        helper.attachToRecyclerView(rv);
    }

    private void persistOrder() {
        List<String> keys = new ArrayList<>();
        for (Category c : adapter.getItems()) {
            keys.add(c.key);
        }
        db.setCategoryOrder(keys);
        Category.loadFromDatabase(this);
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
