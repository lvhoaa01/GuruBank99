package com.example.bankingapp.ui.teller;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.utils.CurrencyFormatter;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;
import com.example.bankingapp.viewmodel.teller.EditAccountViewModel;

public class EditAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_edit_account);

        int tellerUserId = getIntent().getIntExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, 0);

        EditText etAccountNumber = findViewById(R.id.etAccountNumber);
        EditText etAccountType   = findViewById(R.id.etAccountType);
        Button btnLoad           = findViewById(R.id.btnLoad);
        Button btnSave           = findViewById(R.id.btnSave);
        TextView tvAccountInfo   = findViewById(R.id.tvAccountInfo);
        TextView tvStatus        = findViewById(R.id.tvStatus);

        EditAccountViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(EditAccountViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
        });
        vm.getLoadedAccount().observe(this, a -> {
            if (a == null) return;
            tvAccountInfo.setText("Owner #" + a.getOwnerCustomerId()
                    + " | " + a.getType() + " | " + CurrencyFormatter.formatVnd(a.getBalance()));
            etAccountType.setText(a.getType().name());
        });

        btnLoad.setOnClickListener(v -> vm.load(tellerUserId, etAccountNumber.getText().toString()));
        btnSave.setOnClickListener(v -> {
            AccountType t = null;
            try { t = AccountType.valueOf(etAccountType.getText().toString().trim().toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
            vm.changeType(tellerUserId, etAccountNumber.getText().toString(), t);
        });
    }
}
