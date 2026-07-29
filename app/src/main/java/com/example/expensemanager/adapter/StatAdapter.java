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

/** Legend rows for the statistics screen: icon, name, %, amount and a bar. */
public class StatAdapter extends RecyclerView.Adapter<StatAdapter.VH> {

    public static class Item {
        public final Category category;
        public final double amount;
        public final int percent;

        public Item(Category category, double amount, int percent) {
            this.category = category;
            this.amount = amount;
            this.percent = percent;
        }
    }

    private final List<Item> items = new ArrayList<>();

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
                .inflate(R.layout.item_stat_category, parent, false);
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

    static class VH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView name;
        final TextView percent;
        final TextView amount;
        final ProgressBar progress;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            percent = itemView.findViewById(R.id.percent);
            amount = itemView.findViewById(R.id.amount);
            progress = itemView.findViewById(R.id.progress);
        }

        void bind(Item item) {
            int color = ContextCompat.getColor(itemView.getContext(), item.category.colorRes);
            icon.setImageResource(item.category.iconRes);
            icon.setBackgroundTintList(ColorStateList.valueOf(color));
            name.setText(item.category.name);
            percent.setText(item.percent + "%");
            amount.setText(CurrencyUtil.format(item.amount));
            progress.setProgress(item.percent);
            progress.setProgressTintList(ColorStateList.valueOf(color));
        }
    }
}
