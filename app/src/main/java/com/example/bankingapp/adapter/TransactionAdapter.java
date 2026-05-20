package com.example.bankingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bankingapp.R;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.utils.CurrencyFormatter;
import com.example.bankingapp.utils.DateFormatter;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    private final List<Transaction> items = new ArrayList<>();

    public void setItems(List<Transaction> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Transaction t = items.get(position);
        h.tvHeader.setText("#" + t.getTransactionId() + "  " + t.getType().name());
        String details = DateFormatter.format(t.getTimestampMillis())
                + "  •  " + (t.getDescription() == null ? "" : t.getDescription());
        if (t.getSourceAccount() != null) details += "  •  src " + t.getSourceAccount();
        if (t.getDestinationAccount() != null) details += "  •  dst " + t.getDestinationAccount();
        h.tvDetails.setText(details);
        h.tvAmount.setText(CurrencyFormatter.formatVnd(t.getAmount())
                + "  (balance after: " + CurrencyFormatter.formatVnd(t.getBalanceAfter()) + ")");
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvHeader, tvDetails, tvAmount;
        VH(@NonNull View v) {
            super(v);
            tvHeader  = v.findViewById(R.id.tvHeader);
            tvDetails = v.findViewById(R.id.tvDetails);
            tvAmount  = v.findViewById(R.id.tvAmount);
        }
    }
}
