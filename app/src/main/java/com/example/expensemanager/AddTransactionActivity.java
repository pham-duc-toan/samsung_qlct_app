package com.example.expensemanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.adapter.CategoryPickAdapter;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.util.DateUtil;
import com.example.expensemanager.util.MoneyTextWatcher;
import com.example.expensemanager.widget.BalanceWidgetProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

/** Create a new transaction, or edit/delete an existing one. */
public class AddTransactionActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "extra_transaction_id";

    public static Intent editIntent(Context context, long id) {
        Intent intent = new Intent(context, AddTransactionActivity.class);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    private DatabaseHelper db;
    private CategoryPickAdapter categoryAdapter;

    private String type = Transaction.TYPE_EXPENSE;
    private final Calendar date = Calendar.getInstance();
    private Transaction editing; // non-null when editing an existing row

    private EditText etAmount;
    private TextInputEditText etNote;
    private TextView tvTitle;
    private TextView tvDate;
    private TextView btnTypeExpense;
    private TextView btnTypeIncome;
    private ImageView btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        db = DatabaseHelper.getInstance(this);

        etAmount = findViewById(R.id.et_amount);
        etAmount.addTextChangedListener(new MoneyTextWatcher(etAmount));
        etNote = findViewById(R.id.et_note);
        tvTitle = findViewById(R.id.tv_title);
        tvDate = findViewById(R.id.tv_date);
        btnTypeExpense = findViewById(R.id.btn_type_expense);
        btnTypeIncome = findViewById(R.id.btn_type_income);
        btnDelete = findViewById(R.id.btn_delete);

        RecyclerView rvCategories = findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 4));
        rvCategories.setNestedScrollingEnabled(false);
        categoryAdapter = new CategoryPickAdapter();
        rvCategories.setAdapter(categoryAdapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnTypeExpense.setOnClickListener(v -> applyType(Transaction.TYPE_EXPENSE, null));
        btnTypeIncome.setOnClickListener(v -> applyType(Transaction.TYPE_INCOME, null));
        findViewById(R.id.btn_date).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());

        loadIfEditing();

        applyType(type, editing != null ? editing.categoryKey : null);
        updateDateLabel();
    }

    private void loadIfEditing() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            return;
        }
        editing = db.getById(id);
        if (editing == null) {
            return;
        }
        tvTitle.setText(R.string.edit_transaction);
        btnDelete.setVisibility(View.VISIBLE);
        type = editing.type;
        etAmount.setText(String.valueOf((long) editing.amount));
        etAmount.setSelection(etAmount.getText().length());
        etNote.setText(editing.note);
        date.setTimeInMillis(editing.date);
    }

    /** Switch income/expense, restyle the toggle and reload the category grid. */
    private void applyType(String newType, @Nullable String preselectKey) {
        type = newType;
        boolean expense = Transaction.TYPE_EXPENSE.equals(type);
        styleToggle(btnTypeExpense, expense, R.color.expense);
        styleToggle(btnTypeIncome, !expense, R.color.income);
        categoryAdapter.submit(Category.forType(type), preselectKey);
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

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, monthOfYear);
            date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel() {
        String label = DateUtil.formatDateLong(date.getTimeInMillis());
        if (!label.isEmpty()) {
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
        }
        tvDate.setText(label);
    }

    private void save() {
        long amount = MoneyTextWatcher.parse(etAmount.getText());
        if (amount <= 0) {
            Toast.makeText(this, R.string.err_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        Category category = categoryAdapter.getSelected();
        if (category == null) {
            Toast.makeText(this, R.string.err_category, Toast.LENGTH_SHORT).show();
            return;
        }

        Transaction t = editing != null ? editing : new Transaction();
        t.amount = amount;
        t.type = type;
        t.categoryKey = category.key;
        t.note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
        t.date = date.getTimeInMillis();

        if (editing != null) {
            db.update(t);
        } else {
            db.insert(t);
        }

        BalanceWidgetProvider.refresh(this);
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        if (editing == null) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.delete(editing.id);
                    BalanceWidgetProvider.refresh(this);
                    Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }
}
