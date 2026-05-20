package com.example.bankingapp.ui.teller;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;
import com.example.bankingapp.viewmodel.teller.NewAccountViewModel;

public class NewAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_new_account);

        int tellerUserId = getIntent().getIntExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, 0);

        EditText etCustomerId     = findViewById(R.id.etCustomerId);
        EditText etAccountType    = findViewById(R.id.etAccountType);
        EditText etInitialDeposit = findViewById(R.id.etInitialDeposit);
        Button btnSubmit          = findViewById(R.id.btnSubmit);
        TextView tvStatus         = findViewById(R.id.tvStatus);

        NewAccountViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(NewAccountViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
        });

        btnSubmit.setOnClickListener(v -> {
            AccountType type = null;
            String typeStr = etAccountType.getText().toString().trim().toUpperCase();
            try { type = AccountType.valueOf(typeStr); }
            catch (IllegalArgumentException ignored) { }
            vm.create(tellerUserId,
                    etCustomerId.getText().toString(),
                    type,
                    etInitialDeposit.getText().toString());
        });
    }
}
