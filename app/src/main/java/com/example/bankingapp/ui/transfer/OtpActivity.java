package com.example.bankingapp.ui.transfer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.BankingApp;
import com.example.bankingapp.R;
import com.example.bankingapp.viewmodel.TransferViewModel;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;

public class OtpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        TextView tvDisplayedOtp = findViewById(R.id.tvDisplayedOtp);
        EditText etOtp          = findViewById(R.id.etOtp);
        Button btnVerify        = findViewById(R.id.btnVerify);
        Button btnResend        = findViewById(R.id.btnResend);
        TextView tvStatus       = findViewById(R.id.tvStatus);

        TransferViewModel vm = new ViewModelProvider(
                (BankingApp) getApplicationContext(), new ViewModelFactory())
                .get(TransferViewModel.class);

        vm.getGeneratedOtp().observe(this, code ->
                tvDisplayedOtp.setText(getString(R.string.otp_displayed, code == null ? "" : code)));

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
        });

        vm.getPhase().observe(this, phase -> {
            if (phase == TransferViewModel.Phase.COMPLETED || phase == TransferViewModel.Phase.FAILED) {
                // We do not finish() automatically — let the user read the final state.
            }
        });

        btnVerify.setOnClickListener(v -> vm.submitOtp(etOtp.getText().toString()));
        btnResend.setOnClickListener(v -> vm.resendOtp());
    }
}
