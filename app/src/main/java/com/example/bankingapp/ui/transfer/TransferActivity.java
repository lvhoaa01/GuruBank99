package com.example.bankingapp.ui.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.BankingApp;
import com.example.bankingapp.R;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.viewmodel.TransferViewModel;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;

public class TransferActivity extends AppCompatActivity {

    private TransferViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        int customerId = getIntent().getIntExtra(DashboardActivity.EXTRA_CUSTOMER_ID, 0);

        EditText etSource      = findViewById(R.id.etSource);
        EditText etDest        = findViewById(R.id.etDest);
        EditText etAmount      = findViewById(R.id.etAmount);
        EditText etDescription = findViewById(R.id.etDescription);
        Button btnSubmit       = findViewById(R.id.btnSubmit);
        TextView tvStatus      = findViewById(R.id.tvStatus);

        // App-scoped so TransferActivity and OtpActivity share the same VM instance.
        vm = new ViewModelProvider((BankingApp) getApplicationContext(), new ViewModelFactory())
                .get(TransferViewModel.class);

        vm.getPhase().observe(this, phase -> {
            if (phase == TransferViewModel.Phase.OTP_SENT) {
                startActivity(new Intent(this, OtpActivity.class));
            }
        });
        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
        });

        btnSubmit.setOnClickListener(v -> vm.submitDetails(
                customerId,
                etSource.getText().toString(),
                etDest.getText().toString(),
                etAmount.getText().toString(),
                etDescription.getText().toString()));
    }
}
