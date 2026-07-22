package com.example.expensemanager.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.AddTransactionActivity;
import com.example.expensemanager.R;
import com.example.expensemanager.adapter.TransactionAdapter;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.util.AppDialogs;
import com.example.expensemanager.util.CurrencyUtil;
import com.example.expensemanager.util.DateUtil;
import com.example.expensemanager.util.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.List;

/** Dashboard: balance, income/expense totals, monthly budget and recent transactions. */
public class HomeFragment extends Fragment {

    private static final int RECENT_LIMIT = 10;

    private DatabaseHelper db;
    private Prefs prefs;
    private TransactionAdapter adapter;

    private TextView tvToday;
    private TextView tvBalance;
    private TextView tvIncome;
    private TextView tvExpense;

    private TextView tvBudgetStatus;
    private TextView tvBudgetPercent;
    private TextView tvBudgetLeft;
    private ProgressBar budgetProgress;

    private RecyclerView rvRecent;
    private View layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(requireContext());
        prefs = new Prefs(requireContext());

        tvToday = view.findViewById(R.id.tv_today);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        tvBudgetStatus = view.findViewById(R.id.tv_budget_status);
        tvBudgetPercent = view.findViewById(R.id.tv_budget_percent);
        tvBudgetLeft = view.findViewById(R.id.tv_budget_left);
        budgetProgress = view.findViewById(R.id.budget_progress);
        rvRecent = view.findViewById(R.id.rv_recent);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(this::openEdit);
        rvRecent.setAdapter(adapter);

        tvToday.setText(capitalize(DateUtil.formatDateLong(System.currentTimeMillis())));

        view.findViewById(R.id.card_budget).setOnClickListener(v ->
                AppDialogs.showBudget(requireContext(), prefs, this::loadData));

        view.findViewById(R.id.btn_see_all).setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottom_nav);
                if (nav != null) {
                    nav.setSelectedItemId(R.id.nav_transactions);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        double income = db.totalOf(Transaction.TYPE_INCOME);
        double expense = db.totalOf(Transaction.TYPE_EXPENSE);

        tvBalance.setText(CurrencyUtil.format(income - expense));
        tvIncome.setText(CurrencyUtil.format(income));
        tvExpense.setText(CurrencyUtil.format(expense));

        updateBudget();

        List<Transaction> recent = db.getRecent(RECENT_LIMIT);
        adapter.submit(recent);
        boolean empty = recent.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvRecent.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updateBudget() {
        long budget = prefs.getBudget();
        if (budget <= 0) {
            tvBudgetStatus.setText(R.string.budget_not_set);
            budgetProgress.setVisibility(View.GONE);
            tvBudgetPercent.setVisibility(View.GONE);
            tvBudgetLeft.setVisibility(View.GONE);
            return;
        }

        Calendar now = Calendar.getInstance();
        long start = DateUtil.startOfMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH));
        long end = DateUtil.startOfNextMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH));
        double spent = db.totalOf(Transaction.TYPE_EXPENSE, start, end);

        int percent = (int) Math.round(spent / budget * 100.0);
        @ColorRes int colorRes = percent >= 100
                ? R.color.expense
                : (percent >= 80 ? R.color.warning : R.color.income);
        int color = ContextCompat.getColor(requireContext(), colorRes);

        budgetProgress.setVisibility(View.VISIBLE);
        budgetProgress.setProgress(Math.min(100, percent));
        budgetProgress.setProgressTintList(ColorStateList.valueOf(color));

        tvBudgetPercent.setVisibility(View.VISIBLE);
        tvBudgetPercent.setText(percent + "%");
        tvBudgetPercent.setTextColor(color);

        tvBudgetStatus.setText(getString(R.string.budget_spent_format,
                CurrencyUtil.format(spent), CurrencyUtil.format(budget)));

        tvBudgetLeft.setVisibility(View.VISIBLE);
        double remaining = budget - spent;
        if (remaining >= 0) {
            tvBudgetLeft.setText(getString(R.string.budget_remaining_format, CurrencyUtil.format(remaining)));
        } else {
            tvBudgetLeft.setText(getString(R.string.budget_over_format, CurrencyUtil.format(-remaining)));
        }
    }

    private void openEdit(Transaction t) {
        startActivity(AddTransactionActivity.editIntent(requireContext(), t.id));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
