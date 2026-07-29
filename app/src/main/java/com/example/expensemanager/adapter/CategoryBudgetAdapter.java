package com.example.expensemanager.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.util.CurrencyUtil;

import java.util.ArrayList;
import java.util.List;

/** One row per expense category with its monthly spend against its budget. */
public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.VH> {

    public static class Item {
        public final Category category;
        public final double spent;
        public final long budget;

        public Item(Category category, double spent, long budget) {
            this.category = category;
            this.spent = spent;
            this.budget = budget;
        }
    }

    public interface OnPick {
        void onPick(Item item);
    }

    private final List<Item> items = new ArrayList<>();
    private final OnPick listener;

    public CategoryBudgetAdapter(OnPick listener) {
        this.listener = listener;
    }

    public void submit(List<Item> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_budget, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView name;
        final TextView percent;
        final TextView status;
        final ProgressBar progress;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            percent = itemView.findViewById(R.id.percent);
            status = itemView.findViewById(R.id.status);
            progress = itemView.findViewById(R.id.progress);
        }

        void bind(Item item) {
            int catColor = ContextCompat.getColor(itemView.getContext(), item.category.colorRes);
            icon.setImageResource(item.category.iconRes);
            icon.setBackgroundTintList(ColorStateList.valueOf(catColor));
            name.setText(item.category.name);

            if (item.budget <= 0) {
                percent.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                status.setText(R.string.cat_budget_hint);
            } else {
                int pct = (int) Math.round(item.spent / item.budget * 100.0);
                int colorRes = pct >= 100
                        ? R.color.expense
                        : (pct >= 80 ? R.color.warning : R.color.income);
                int color = ContextCompat.getColor(itemView.getContext(), colorRes);

                percent.setVisibility(View.VISIBLE);
                percent.setText(pct + "%");
                percent.setTextColor(color);

                progress.setVisibility(View.VISIBLE);
                progress.setProgress(Math.min(100, pct));
                progress.setProgressTintList(ColorStateList.valueOf(color));

                status.setText(itemView.getContext().getString(R.string.budget_spent_format,
                        CurrencyUtil.format(item.spent), CurrencyUtil.format(item.budget)));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPick(item);
                }
            });
        }
    }
}
