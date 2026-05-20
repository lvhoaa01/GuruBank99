package com.example.bankingapp.ui.withdraw;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;
import com.example.bankingapp.viewmodel.WithdrawViewModel;

public class WithdrawActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        int customerId = getIntent().getIntExtra(DashboardActivity.EXTRA_CUSTOMER_ID, 0);

        EditText etAccount     = findViewById(R.id.etAccount);
        EditText etAmount      = findViewById(R.id.etAmount);
        EditText etDescription = findViewById(R.id.etDescription);
        Button btnSubmit       = findViewById(R.id.btnSubmit);
        TextView tvStatus      = findViewById(R.id.tvStatus);

        WithdrawViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(WithdrawViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
        });

        btnSubmit.setOnClickListener(v -> vm.withdraw(
                customerId,
                etAccount.getText().toString(),
                etAmount.getText().toString(),
                etDescription.getText().toString()));
    }
}
