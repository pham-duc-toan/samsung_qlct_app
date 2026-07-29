package com.example.expensemanager.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.adapter.StatAdapter;
import com.example.expensemanager.db.DatabaseHelper;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.util.CurrencyUtil;
import com.example.expensemanager.util.DateUtil;
import com.example.expensemanager.view.BarChartView;
import com.example.expensemanager.view.PieChartView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Monthly spending breakdown: a donut chart plus a per-category legend. */
public class StatsFragment extends Fragment {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private DatabaseHelper db;
    private StatAdapter adapter;
    private final Calendar month = Calendar.getInstance();

    private TextView tvMonth;
    private TextView tvTotal;
    private TextView tvNoData;
    private PieChartView pieChart;
    private BarChartView barChart;
    private View chartContainer;
    private RecyclerView rv;

    // Custom date-range statistics.
    private StatAdapter rangeAdapter;
    private final Calendar rangeFrom = Calendar.getInstance();
    private final Calendar rangeTo = Calendar.getInstance();
    private TextView tvRangeFrom;
    private TextView tvRangeTo;
    private TextView tvRangeTotal;
    private TextView tvRangeNoData;
    private RecyclerView rvRange;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(requireContext());

        tvMonth = view.findViewById(R.id.tv_month);
        tvTotal = view.findViewById(R.id.tv_total_spent);
        tvNoData = view.findViewById(R.id.tv_no_data);
        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);
        chartContainer = view.findViewById(R.id.chart_container);
        rv = view.findViewById(R.id.rv_stats);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StatAdapter();
        rv.setAdapter(adapter);

        barChart.setColors(
                ContextCompat.getColor(requireContext(), R.color.income),
                ContextCompat.getColor(requireContext(), R.color.expense),
                ContextCompat.getColor(requireContext(), R.color.text_secondary));

        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> {
            month.add(Calendar.MONTH, -1);
            loadData();
        });
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> {
            month.add(Calendar.MONTH, 1);
            loadData();
        });

        // Date-range section: default from the 1st of this month to today.
        rangeFrom.set(Calendar.DAY_OF_MONTH, 1);
        tvRangeFrom = view.findViewById(R.id.btn_range_from);
        tvRangeTo = view.findViewById(R.id.btn_range_to);
        tvRangeTotal = view.findViewById(R.id.tv_range_total);
        tvRangeNoData = view.findViewById(R.id.tv_range_no_data);
        rvRange = view.findViewById(R.id.rv_range);
        rvRange.setLayoutManager(new LinearLayoutManager(requireContext()));
        rangeAdapter = new StatAdapter();
        rvRange.setAdapter(rangeAdapter);

        tvRangeFrom.setOnClickListener(v -> pickRangeDate(true));
        tvRangeTo.setOnClickListener(v -> pickRangeDate(false));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        loadTrend();
        loadRange();
    }

    /** Opens a date picker for either end of the custom range. */
    private void pickRangeDate(boolean isFrom) {
        Calendar target = isFrom ? rangeFrom : rangeTo;
        new DatePickerDialog(requireContext(), (view, year, monthOfYear, dayOfMonth) -> {
            target.set(Calendar.YEAR, year);
            target.set(Calendar.MONTH, monthOfYear);
            target.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            loadRange();
        }, target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Expense breakdown for the freely chosen [from, to] range (inclusive). */
    private void loadRange() {
        long fromDay = DateUtil.dayKey(rangeFrom.getTimeInMillis());
        long toDay = DateUtil.dayKey(rangeTo.getTimeInMillis());
        long start = Math.min(fromDay, toDay);
        long endInclusive = Math.max(fromDay, toDay);
        long end = endInclusive + DAY_MS; // include the whole 'to' day

        tvRangeFrom.setText(getString(R.string.range_from_label, DateUtil.formatDate(start)));
        tvRangeTo.setText(getString(R.string.range_to_label, DateUtil.formatDate(endInclusive)));

        LinkedHashMap<String, Double> byCategory = db.expenseByCategory(start, end);
        double total = 0;
        for (double value : byCategory.values()) {
            total += value;
        }

        if (byCategory.isEmpty() || total <= 0) {
            tvRangeTotal.setText(CurrencyUtil.format(0));
            tvRangeNoData.setVisibility(View.VISIBLE);
            rangeAdapter.submit(new ArrayList<>());
            return;
        }

        tvRangeNoData.setVisibility(View.GONE);
        tvRangeTotal.setText(CurrencyUtil.format(total));

        List<StatAdapter.Item> items = new ArrayList<>();
        for (Map.Entry<String, Double> entry : byCategory.entrySet()) {
            Category category = Category.byKey(entry.getKey());
            double amount = entry.getValue();
            int percent = (int) Math.round(amount / total * 100.0);
            items.add(new StatAdapter.Item(category, amount, percent));
        }
        rangeAdapter.submit(items);
    }

    /** Last 6 months of income vs expense totals, ending at the current month. */
    private void loadTrend() {
        Calendar cursor = Calendar.getInstance();
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        cursor.add(Calendar.MONTH, -5);

        List<BarChartView.Group> groups = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int year = cursor.get(Calendar.YEAR);
            int m = cursor.get(Calendar.MONTH);
            long start = DateUtil.startOfMonth(year, m);
            long end = DateUtil.startOfNextMonth(year, m);
            double income = db.totalOf(Transaction.TYPE_INCOME, start, end);
            double expense = db.totalOf(Transaction.TYPE_EXPENSE, start, end);
            groups.add(new BarChartView.Group("T" + (m + 1), (float) income, (float) expense));
            cursor.add(Calendar.MONTH, 1);
        }
        barChart.setData(groups);
    }

    private void loadData() {
        int year = month.get(Calendar.YEAR);
        int m = month.get(Calendar.MONTH);
        long start = DateUtil.startOfMonth(year, m);
        long end = DateUtil.startOfNextMonth(year, m);
        tvMonth.setText(DateUtil.monthLabel(year, m));

        LinkedHashMap<String, Double> byCategory = db.expenseByCategory(start, end);
        double total = 0;
        for (double value : byCategory.values()) {
            total += value;
        }

        if (byCategory.isEmpty() || total <= 0) {
            chartContainer.setVisibility(View.INVISIBLE);
            tvNoData.setVisibility(View.VISIBLE);
            adapter.submit(new ArrayList<>());
            return;
        }

        chartContainer.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        tvTotal.setText(CurrencyUtil.format(total));

        List<PieChartView.Slice> slices = new ArrayList<>();
        List<StatAdapter.Item> items = new ArrayList<>();
        for (Map.Entry<String, Double> entry : byCategory.entrySet()) {
            Category category = Category.byKey(entry.getKey());
            double amount = entry.getValue();
            int color = ContextCompat.getColor(requireContext(), category.colorRes);
            int percent = (int) Math.round(amount / total * 100.0);
            slices.add(new PieChartView.Slice((float) amount, color));
            items.add(new StatAdapter.Item(category, amount, percent));
        }

        pieChart.setData(slices);
        adapter.submit(items);
    }
}
