package com.example.expensemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;

import java.util.Arrays;
import java.util.List;

/** Grid of selectable icons (drawable resource entry names) for the category editor. */
public class IconPickAdapter extends RecyclerView.Adapter<IconPickAdapter.VH> {

    private final List<String> icons;
    private int selectedPos = 0;

    public IconPickAdapter(String[] iconEntries) {
        this.icons = Arrays.asList(iconEntries);
    }

    /** Preselect the given entry name (falls back to the first). */
    public void setSelected(String entry) {
        int i = icons.indexOf(entry);
        selectedPos = i >= 0 ? i : 0;
        notifyDataSetChanged();
    }

    public String getSelected() {
        return icons.get(selectedPos);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icon_pick, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(icons.get(position), position == selectedPos);
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    class VH extends RecyclerView.ViewHolder {
        final ImageView icon;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            itemView.setOnClickListener(v -> select(getBindingAdapterPosition()));
        }

        void bind(String entry, boolean selected) {
            Context ctx = itemView.getContext();
            int id = ctx.getResources().getIdentifier(entry, "drawable", ctx.getPackageName());
            if (id != 0) {
                icon.setImageResource(id);
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
