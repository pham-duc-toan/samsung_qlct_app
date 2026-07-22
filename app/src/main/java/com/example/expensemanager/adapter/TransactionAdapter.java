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
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.util.CurrencyUtil;
import com.example.expensemanager.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a newest-first list of transactions grouped under per-day headers
 * ("Hôm nay", "Hôm qua", or a date). Two view types: header and item.
 */
public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnTransactionClick {
        void onClick(Transaction transaction);
    }

    private final List<Object> rows = new ArrayList<>();
    private final OnTransactionClick listener;

    public TransactionAdapter(OnTransactionClick listener) {
        this.listener = listener;
    }

    public void submit(List<Transaction> transactions) {
        rows.clear();
        long currentDay = Long.MIN_VALUE;
        for (Transaction t : transactions) {
            long day = DateUtil.dayKey(t.date);
            if (day != currentDay) {
                currentDay = day;
                rows.add(DateUtil.relativeDay(t.date)); // String header
            }
            rows.add(t);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof Transaction ? TYPE_ITEM : TYPE_HEADER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new TxVH(inflater.inflate(R.layout.item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).header.setText((String) row);
        } else {
            ((TxVH) holder).bind((Transaction) row);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView header;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
        }
    }

    class TxVH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final TextView note;
        final TextView amount;

        TxVH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            note = itemView.findViewById(R.id.note);
            amount = itemView.findViewById(R.id.amount);
        }

        void bind(Transaction t) {
            Category cat = t.category();
            int color = ContextCompat.getColor(itemView.getContext(), cat.colorRes);
            icon.setImageResource(cat.iconRes);
            icon.setBackgroundTintList(ColorStateList.valueOf(color));

            title.setText(cat.nameRes);
            if (t.note == null || t.note.trim().isEmpty()) {
                note.setVisibility(View.GONE);
            } else {
                note.setVisibility(View.VISIBLE);
                note.setText(t.note);
            }

            int amountColor = ContextCompat.getColor(itemView.getContext(),
                    t.isIncome() ? R.color.income : R.color.expense);
            amount.setTextColor(amountColor);
            amount.setText(CurrencyUtil.formatSigned(t.signedAmount()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(t);
                }
            });
        }
    }
}
