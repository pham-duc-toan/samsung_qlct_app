package com.example.expensemanager.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.AddTransactionActivity;
import com.example.expensemanager.R;
import com.example.expensemanager.adapter.TransactionAdapter;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.util.CurrencyUtil;
import com.example.expensemanager.util.DateUtil;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Full transaction history for one month, with month navigation, search and type filter. */
public class TransactionsFragment extends Fragment {

    private DatabaseHelper db;
    private TransactionAdapter adapter;
    private final Calendar month = Calendar.getInstance();

    private TextView tvMonth;
    private TextView tvIncome;
    private TextView tvExpense;
    private EditText etSearch;
    private ChipGroup chipGroup;
    private RecyclerView rv;
    private View layoutEmpty;
    private TextView tvEmpty;

    /** The current month's transactions before search/type filtering. */
    private List<Transaction> monthList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(requireContext());

        tvMonth = view.findViewById(R.id.tv_month);
        tvIncome = view.findViewById(R.id.tv_month_income);
        tvExpense = view.findViewById(R.id.tv_month_expense);
        etSearch = view.findViewById(R.id.et_search);
        chipGroup = view.findViewById(R.id.chip_group);
        rv = view.findViewById(R.id.rv_transactions);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        tvEmpty = view.findViewById(R.id.tv_empty);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(this::openEdit);
        rv.setAdapter(adapter);

        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> {
            month.add(Calendar.MONTH, -1);
            loadData();
        });
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> {
            month.add(Calendar.MONTH, 1);
            loadData();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { applyFilter(); }
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        int year = month.get(Calendar.YEAR);
        int m = month.get(Calendar.MONTH);
        long start = DateUtil.startOfMonth(year, m);
        long end = DateUtil.startOfNextMonth(year, m);

        tvMonth.setText(DateUtil.monthLabel(year, m));
        tvIncome.setText(CurrencyUtil.format(db.totalOf(Transaction.TYPE_INCOME, start, end)));
        tvExpense.setText(CurrencyUtil.format(db.totalOf(Transaction.TYPE_EXPENSE, start, end)));

        monthList = db.getBetween(start, end);
        applyFilter();
    }

    private void applyFilter() {
        int checkedId = chipGroup.getCheckedChipId();
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.getDefault());

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : monthList) {
            if (checkedId == R.id.chip_income && !t.isIncome()) {
                continue;
            }
            if (checkedId == R.id.chip_expense && t.isIncome()) {
                continue;
            }
            if (!query.isEmpty()) {
                String note = t.note == null ? "" : t.note.toLowerCase(Locale.getDefault());
                String category = t.category().name.toLowerCase(Locale.getDefault());
                if (!note.contains(query) && !category.contains(query)) {
                    continue;
                }
            }
            filtered.add(t);
        }

        adapter.submit(filtered);

        if (monthList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(R.string.empty_transactions);
        } else if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(R.string.no_results);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void openEdit(Transaction t) {
        startActivity(AddTransactionActivity.editIntent(requireContext(), t.id));
    }
}
