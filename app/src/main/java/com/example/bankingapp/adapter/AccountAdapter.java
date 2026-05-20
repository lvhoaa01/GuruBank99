package com.example.bankingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bankingapp.R;
import com.example.bankingapp.model.Account;
import com.example.bankingapp.utils.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.VH> {

    public interface OnSelect { void onSelect(Account account); }

    private final List<Account> items = new ArrayList<>();
    private final OnSelect listener;

    public AccountAdapter(OnSelect listener) {
        this.listener = listener;
    }

    public void setItems(List<Account> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Account a = items.get(position);
        h.tvAcctNum.setText(a.getAccountNumber());
        h.tvAcctType.setText(a.getType().name());
        h.tvBalance.setText(CurrencyFormatter.formatVnd(a.getBalance()));
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(a);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvAcctNum, tvAcctType, tvBalance;
        VH(@NonNull View v) {
            super(v);
            tvAcctNum  = v.findViewById(R.id.tvAcctNum);
            tvAcctType = v.findViewById(R.id.tvAcctType);
            tvBalance  = v.findViewById(R.id.tvBalance);
        }
    }
}
