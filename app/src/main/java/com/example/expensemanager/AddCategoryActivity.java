package com.example.expensemanager;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.adapter.ColorPickAdapter;
import com.example.expensemanager.adapter.IconPickAdapter;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Category;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Create a new category, or edit/delete an existing one. */
public class AddCategoryActivity extends AppCompatActivity {

    private static final String EXTRA_KEY = "extra_category_key";
    // The universal fallback category (used by Category.byKey); deleting it would crash the app.
    private static final String KEY_OTHER = "other";

    // Icons and colors offered by the pickers (resource entry names present in res/).
    private static final String[] ICONS = {
            "ic_food", "ic_coffee", "ic_transport", "ic_shopping", "ic_bills", "ic_entertainment",
            "ic_health", "ic_education", "ic_home_cat", "ic_other", "ic_salary", "ic_bonus",
            "ic_invest", "ic_gift", "ic_wallet", "ic_category"
    };
    private static final String[] COLORS = {
            "cat_food", "cat_coffee", "cat_transport", "cat_shopping", "cat_bills", "cat_entertainment",
            "cat_health", "cat_education", "cat_home", "cat_other", "cat_salary", "cat_bonus",
            "cat_invest", "cat_gift"
    };

    public static Intent editIntent(Context context, String key) {
        Intent intent = new Intent(context, AddCategoryActivity.class);
        intent.putExtra(EXTRA_KEY, key);
        return intent;
    }

    private DatabaseHelper db;
    private IconPickAdapter iconAdapter;
    private ColorPickAdapter colorAdapter;

    private boolean income = false;
    @Nullable private String editingKey; // non-null when editing an existing category

    private EditText etName;
    private TextView tvTitle;
    private TextView btnTypeExpense;
    private TextView btnTypeIncome;
    private View btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        db = DatabaseHelper.getInstance(this);

        etName = findViewById(R.id.et_name);
        tvTitle = findViewById(R.id.tv_title);
        btnTypeExpense = findViewById(R.id.btn_type_expense);
        btnTypeIncome = findViewById(R.id.btn_type_income);
        btnDelete = findViewById(R.id.btn_delete);

        RecyclerView rvIcons = findViewById(R.id.rv_icons);
        rvIcons.setLayoutManager(new GridLayoutManager(this, 6));
        rvIcons.setNestedScrollingEnabled(false);
        iconAdapter = new IconPickAdapter(ICONS);
        rvIcons.setAdapter(iconAdapter);

        RecyclerView rvColors = findViewById(R.id.rv_colors);
        rvColors.setLayoutManager(new GridLayoutManager(this, 7));
        rvColors.setNestedScrollingEnabled(false);
        colorAdapter = new ColorPickAdapter(COLORS);
        rvColors.setAdapter(colorAdapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnTypeExpense.setOnClickListener(v -> applyType(false));
        btnTypeIncome.setOnClickListener(v -> applyType(true));
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());

        loadIfEditing();
        applyType(income);
    }

    private void loadIfEditing() {
        editingKey = getIntent().getStringExtra(EXTRA_KEY);
        if (editingKey == null) {
            return;
        }
        DatabaseHelper.CategoryDef def = db.getCategoryDef(this, editingKey);
        if (def == null) {
            editingKey = null;
            return;
        }
        tvTitle.setText(R.string.edit_category);
        // "Khác" is the fallback for deleted categories, so it must never be removed.
        if (!KEY_OTHER.equals(editingKey)) {
            btnDelete.setVisibility(View.VISIBLE);
        }
        etName.setText(def.displayName);
        etName.setSelection(etName.getText().length());
        income = def.income;
        iconAdapter.setSelected(def.icon);
        colorAdapter.setSelected(def.color);
    }

    private void applyType(boolean incomeType) {
        income = incomeType;
        styleToggle(btnTypeExpense, !income, R.color.expense);
        styleToggle(btnTypeIncome, income, R.color.income);
    }

    private void styleToggle(TextView tab, boolean selected, @ColorRes int colorRes) {
        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_toggle_selected);
            tab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorRes)));
            tab.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            tab.setBackground(null);
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.err_cat_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isDuplicateName(name)) {
            Toast.makeText(this, R.string.err_cat_dup, Toast.LENGTH_SHORT).show();
            return;
        }
        String icon = iconAdapter.getSelected();
        String color = colorAdapter.getSelected();

        if (editingKey != null) {
            db.updateCategory(editingKey, name, icon, color, income);
        } else {
            String key = "custom_" + System.currentTimeMillis();
            db.insertCategory(key, name, icon, color, income);
        }
        Category.loadFromDatabase(this);
        Toast.makeText(this, R.string.cat_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    /** True if another category already uses this name (case-insensitive). */
    private boolean isDuplicateName(String name) {
        for (DatabaseHelper.CategoryDef def : db.getCategoryDefs(this)) {
            if (def.key.equals(editingKey)) {
                continue; // don't compare a category against itself when editing
            }
            if (def.displayName.trim().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private void confirmDelete() {
        if (editingKey == null || KEY_OTHER.equals(editingKey)) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_cat_title)
                .setMessage(R.string.delete_cat_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deleteCategory(editingKey);
                    Category.loadFromDatabase(this);
                    Toast.makeText(this, R.string.cat_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }
}
