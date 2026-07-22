package com.example.expensemanager.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Category;

import java.util.ArrayList;
import java.util.List;

/** Grid of selectable category bubbles used on the add/edit screen. */
public class CategoryPickAdapter extends RecyclerView.Adapter<CategoryPickAdapter.VH> {

    private final List<Category> data = new ArrayList<>();
    private int selectedPos = 0;

    /** Replace the list and preselect a category by key (falls back to the first). */
    public void submit(List<Category> categories, @Nullable String selectedKey) {
        data.clear();
        data.addAll(categories);
        selectedPos = 0;
        if (selectedKey != null) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).key.equals(selectedKey)) {
                    selectedPos = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @Nullable
    public Category getSelected() {
        if (data.isEmpty() || selectedPos < 0 || selectedPos >= data.size()) {
            return null;
        }
        return data.get(selectedPos);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_pick, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(data.get(position), position == selectedPos);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class VH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView name;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(v -> select(getBindingAdapterPosition()));
        }

        void bind(Category c, boolean selected) {
            int color = ContextCompat.getColor(itemView.getContext(), c.colorRes);
            icon.setImageResource(c.iconRes);
            icon.setBackgroundTintList(ColorStateList.valueOf(color));
            name.setText(c.nameRes);
            itemView.setSelected(selected);
        }
    }

    private void select(int position) {
        if (position == RecyclerView.NO_POSITION || position == selectedPos) {
            return;
        }
        int previous = selectedPos;
        selectedPos = position;
        notifyItemChanged(previous);
        notifyItemChanged(selectedPos);
    }
}
