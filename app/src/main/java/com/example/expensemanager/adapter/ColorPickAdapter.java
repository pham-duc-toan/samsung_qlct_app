package com.example.expensemanager.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;

import java.util.Arrays;
import java.util.List;

/** Grid of selectable color swatches (color resource entry names). */
public class ColorPickAdapter extends RecyclerView.Adapter<ColorPickAdapter.VH> {

    private final List<String> colors;
    private int selectedPos = 0;

    public ColorPickAdapter(String[] colorEntries) {
        this.colors = Arrays.asList(colorEntries);
    }

    public void setSelected(String entry) {
        int i = colors.indexOf(entry);
        selectedPos = i >= 0 ? i : 0;
        notifyDataSetChanged();
    }

    public String getSelected() {
        return colors.get(selectedPos);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color_pick, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(colors.get(position), position == selectedPos);
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    class VH extends RecyclerView.ViewHolder {
        final View swatch;

        VH(@NonNull View itemView) {
            super(itemView);
            swatch = itemView.findViewById(R.id.swatch);
            itemView.setOnClickListener(v -> select(getBindingAdapterPosition()));
        }

        void bind(String entry, boolean selected) {
            Context ctx = itemView.getContext();
            int id = ctx.getResources().getIdentifier(entry, "color", ctx.getPackageName());
            if (id != 0) {
                swatch.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, id)));
            }
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
