package com.example.spendsmart.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spendsmart.R;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Holder> {
    public interface Listener {
        void onClick(Transaction transaction);
    }

    private final List<Transaction> items = new ArrayList<>();
    private final Listener listener;

    public TransactionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Transaction> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    public Transaction getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Transaction tx = items.get(position);
        holder.category.setText(tx.category);
        String initial = tx.category == null || tx.category.isEmpty()
                ? "?"
                : tx.category.substring(0, 1).toUpperCase();
        holder.categoryInitial.setText(initial);
        String note = tx.note == null || tx.note.isEmpty() ? tx.type.name() : tx.note;
        if (tx.paymentMethod != null) {
            note = note + " · " + tx.paymentMethod.name();
        }
        if (tx.receiptUri != null && !tx.receiptUri.isEmpty()) {
            note = note + " · receipt";
        }
        holder.note.setText(note);
        holder.date.setText(DateUtils.formatDisplayDate(tx.date));
        boolean income = tx.type == TransactionType.INCOME;
        holder.amount.setText(CurrencyFormatter.formatSigned(tx.amount, income));
        int color = ContextCompat.getColor(holder.itemView.getContext(),
                income ? R.color.income_green : R.color.expense_red);
        holder.amount.setTextColor(color);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(tx);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView category;
        final TextView categoryInitial;
        final TextView note;
        final TextView date;
        final TextView amount;

        Holder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.textCategory);
            categoryInitial = itemView.findViewById(R.id.textCategoryInitial);
            note = itemView.findViewById(R.id.textNote);
            date = itemView.findViewById(R.id.textDate);
            amount = itemView.findViewById(R.id.textAmount);
        }
    }
}

