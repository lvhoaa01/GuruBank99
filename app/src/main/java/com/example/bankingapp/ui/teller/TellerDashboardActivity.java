package com.example.bankingapp.ui.teller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.ui.login.LoginActivity;
import com.example.bankingapp.ui.profile.ChangePasswordActivity;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.utils.SessionManager;
import com.example.bankingapp.viewmodel.ViewModelFactory;
import com.example.bankingapp.viewmodel.teller.TellerDashboardViewModel;

public class TellerDashboardActivity extends AppCompatActivity {

    public static final String EXTRA_TELLER_USER_ID = "tellerUserId";

    private int tellerUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_dashboard);

        tellerUserId = getIntent().getIntExtra(DashboardActivity.EXTRA_CUSTOMER_ID, 0);

        TextView tvGreeting = findViewById(R.id.tvGreeting);

        TellerDashboardViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(TellerDashboardViewModel.class);
        vm.getTeller().observe(this, t -> {
            if (t != null) tvGreeting.setText(getString(R.string.teller_greeting,
                    t.getFullName(), t.getBranchId()));
        });
        vm.load(tellerUserId);

        wire(R.id.btnNewCustomer,    NewCustomerActivity.class);
        wire(R.id.btnEditCustomer,   EditCustomerActivity.class);
        wire(R.id.btnDeleteCustomer, DeleteCustomerActivity.class);
        wire(R.id.btnNewAccount,     NewAccountActivity.class);
        wire(R.id.btnEditAccount,    EditAccountActivity.class);
        wire(R.id.btnDeleteAccount,  DeleteAccountActivity.class);
        wire(R.id.btnTellerDeposit,  TellerDepositActivity.class);
        wire(R.id.btnTellerWithdraw, TellerWithdrawActivity.class);
        wire(R.id.btnTellerTransfer, TellerTransferActivity.class);
        wire(R.id.btnTellerStatement, TellerStatementActivity.class);
        wire(R.id.btnChangePassword, ChangePasswordActivity.class);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SessionManager.get().logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SessionManager.get().touchOrExpire()) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void wire(int buttonId, Class<?> target) {
        Button b = findViewById(buttonId);
        b.setOnClickListener(v -> {
            Intent i = new Intent(this, target);
            i.putExtra(EXTRA_TELLER_USER_ID, tellerUserId);
            i.putExtra(DashboardActivity.EXTRA_CUSTOMER_ID, tellerUserId);
            startActivity(i);
        });
    }
}
