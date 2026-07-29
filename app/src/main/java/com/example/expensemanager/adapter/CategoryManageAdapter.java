package com.example.expensemanager.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Category;

import java.util.ArrayList;
import java.util.List;

/** Lists every category (icon, name, income/expense kind) for the management screen. */
public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.VH> {

    public interface OnCategoryClick {
        void onClick(Category category);
    }

    private final List<Category> items = new ArrayList<>();
    private final OnCategoryClick listener;

    public CategoryManageAdapter(OnCategoryClick listener) {
        this.listener = listener;
    }

    public void submit(List<Category> data) {
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
                .inflate(R.layout.item_category_manage, parent, false);
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
        final TextView kind;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            kind = itemView.findViewById(R.id.kind);
        }

        void bind(Category c) {
            int color = ContextCompat.getColor(itemView.getContext(), c.colorRes);
            icon.setImageResource(c.iconRes);
            icon.setBackgroundTintList(ColorStateList.valueOf(color));
            name.setText(c.name);
            kind.setText(c.income ? R.string.filter_income : R.string.filter_expense);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(c);
                }
            });
        }
    }
}
