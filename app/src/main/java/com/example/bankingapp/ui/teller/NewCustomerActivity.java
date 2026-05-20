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
import com.example.bankingapp.viewmodel.teller.NewCustomerViewModel;

public class NewCustomerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_new_customer);

        int tellerUserId = getIntent().getIntExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, 0);

        EditText etFullName     = findViewById(R.id.etFullName);
        EditText etIdNumber     = findViewById(R.id.etIdNumber);
        EditText etDob          = findViewById(R.id.etDob);
        EditText etAddress      = findViewById(R.id.etAddress);
        EditText etCity         = findViewById(R.id.etCity);
        EditText etState        = findViewById(R.id.etState);
        EditText etPin          = findViewById(R.id.etPin);
        EditText etPhone        = findViewById(R.id.etPhone);
        EditText etEmail        = findViewById(R.id.etEmail);
        EditText etDailyLimit   = findViewById(R.id.etDailyLimit);
        Button btnSubmit        = findViewById(R.id.btnSubmit);
        TextView tvStatus       = findViewById(R.id.tvStatus);

        NewCustomerViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(NewCustomerViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
            if (s.kind == UiState.Kind.SUCCESS && s.payload instanceof NewCustomerViewModel.NewCustomerResult) {
                NewCustomerViewModel.NewCustomerResult r =
                        (NewCustomerViewModel.NewCustomerResult) s.payload;
                tvStatus.setText("Created. Username=" + r.username
                        + "  TempPwd=" + r.tempPassword
                        + "  (show this once)");
            }
        });

        btnSubmit.setOnClickListener(v -> {
            long limit;
            try { limit = Long.parseLong(etDailyLimit.getText().toString()); }
            catch (NumberFormatException nfe) { limit = -1L; }
            vm.create(tellerUserId,
                    etFullName.getText().toString(),
                    etIdNumber.getText().toString(),
                    etDob.getText().toString(),
                    etAddress.getText().toString(),
                    etCity.getText().toString(),
                    etState.getText().toString(),
                    etPin.getText().toString(),
                    etPhone.getText().toString(),
                    etEmail.getText().toString(),
                    limit);
        });
    }
}
