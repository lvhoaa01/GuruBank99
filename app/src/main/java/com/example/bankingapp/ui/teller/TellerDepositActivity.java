package com.example.bankingapp.ui.teller;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;
import com.example.bankingapp.viewmodel.teller.TellerDepositViewModel;

public class TellerDepositActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_deposit);

        int tellerUserId = getIntent().getIntExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, 0);

        EditText etAccount     = findViewById(R.id.etAccount);
        EditText etAmount      = findViewById(R.id.etAmount);
        EditText etDescription = findViewById(R.id.etDescription);
        Button btnSubmit       = findViewById(R.id.btnSubmit);
        TextView tvStatus      = findViewById(R.id.tvStatus);

        TellerDepositViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(TellerDepositViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
        });

        btnSubmit.setOnClickListener(v -> vm.deposit(
                tellerUserId,
                etAccount.getText().toString(),
                etAmount.getText().toString(),
                etDescription.getText().toString()));
    }
}
