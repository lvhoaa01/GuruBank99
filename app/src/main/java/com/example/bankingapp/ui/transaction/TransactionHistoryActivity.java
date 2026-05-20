package com.example.bankingapp.ui.transaction;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bankingapp.R;
import com.example.bankingapp.adapter.TransactionAdapter;
import com.example.bankingapp.viewmodel.TransactionHistoryViewModel;
import com.example.bankingapp.viewmodel.ViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    private static final SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        EditText etAccount   = findViewById(R.id.etAccount);
        EditText etFromDate  = findViewById(R.id.etFromDate);
        EditText etToDate    = findViewById(R.id.etToDate);
        EditText etMinAmount = findViewById(R.id.etMinAmount);
        EditText etMaxCount  = findViewById(R.id.etMaxCount);
        Button btnMini       = findViewById(R.id.btnMini);
        Button btnFilter     = findViewById(R.id.btnFilter);
        RecyclerView rv      = findViewById(R.id.rvTransactions);

        TransactionAdapter adapter = new TransactionAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        TransactionHistoryViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(TransactionHistoryViewModel.class);

        vm.getResult().observe(this, adapter::setItems);

        btnMini.setOnClickListener(v -> vm.loadMini(etAccount.getText().toString()));
        btnFilter.setOnClickListener(v -> {
            long from = parseDateOr(etFromDate.getText().toString(), Long.MIN_VALUE);
            long to   = parseDateOr(etToDate.getText().toString(), Long.MAX_VALUE);
            long min  = parseLongOr(etMinAmount.getText().toString(), 0L);
            int max   = (int) parseLongOr(etMaxCount.getText().toString(), 0L);
            vm.loadCustomized(etAccount.getText().toString(), from, to, min, max);
        });
    }

    private static long parseDateOr(String s, long fallback) {
        try {
            if (s == null || s.isEmpty()) return fallback;
            Date d = YYYY_MM_DD.parse(s);
            return d == null ? fallback : d.getTime();
        } catch (Exception e) {
            return fallback;
        }
    }

    private static long parseLongOr(String s, long fallback) {
        try { return (s == null || s.isEmpty()) ? fallback : Long.parseLong(s); }
        catch (NumberFormatException e) { return fallback; }
    }
}
