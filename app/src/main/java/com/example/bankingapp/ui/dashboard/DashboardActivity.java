package com.example.bankingapp.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bankingapp.R;
import com.example.bankingapp.adapter.AccountAdapter;
import com.example.bankingapp.ui.deposit.DepositActivity;
import com.example.bankingapp.ui.login.LoginActivity;
import com.example.bankingapp.ui.profile.ProfileActivity;
import com.example.bankingapp.ui.transaction.TransactionHistoryActivity;
import com.example.bankingapp.ui.transfer.TransferActivity;
import com.example.bankingapp.ui.withdraw.WithdrawActivity;
import com.example.bankingapp.utils.SessionManager;
import com.example.bankingapp.viewmodel.DashboardViewModel;
import com.example.bankingapp.viewmodel.ViewModelFactory;

public class DashboardActivity extends AppCompatActivity {

    public static final String EXTRA_CUSTOMER_ID = "customerId";

    private int customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        customerId = getIntent().getIntExtra(EXTRA_CUSTOMER_ID, 0);

        TextView tvGreeting       = findViewById(R.id.tvGreeting);
        RecyclerView rvAccounts   = findViewById(R.id.rvAccounts);
        Button btnDeposit         = findViewById(R.id.btnDeposit);
        Button btnWithdraw        = findViewById(R.id.btnWithdraw);
        Button btnTransfer        = findViewById(R.id.btnTransfer);
        Button btnHistory         = findViewById(R.id.btnHistory);
        Button btnProfile         = findViewById(R.id.btnProfile);
        Button btnLogout          = findViewById(R.id.btnLogout);

        AccountAdapter adapter = new AccountAdapter(null);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        rvAccounts.setAdapter(adapter);

        DashboardViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(DashboardViewModel.class);

        vm.getUser().observe(this, u -> {
            if (u != null) tvGreeting.setText(getString(R.string.dashboard_greeting, u.getFullName()));
        });
        vm.getAccounts().observe(this, adapter::setItems);

        btnDeposit.setOnClickListener(v -> open(DepositActivity.class));
        btnWithdraw.setOnClickListener(v -> open(WithdrawActivity.class));
        btnTransfer.setOnClickListener(v -> open(TransferActivity.class));
        btnHistory.setOnClickListener(v -> open(TransactionHistoryActivity.class));
        btnProfile.setOnClickListener(v -> open(ProfileActivity.class));
        btnLogout.setOnClickListener(v -> {
            SessionManager.get().logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        vm.load(customerId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SessionManager.get().touchOrExpire()) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        DashboardViewModel vm = new ViewModelProvider(this, new ViewModelFactory()).get(DashboardViewModel.class);
        vm.load(customerId);
    }

    private void open(Class<?> activity) {
        Intent i = new Intent(this, activity);
        i.putExtra(EXTRA_CUSTOMER_ID, customerId);
        startActivity(i);
    }
}
