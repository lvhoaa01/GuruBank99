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
import com.example.bankingapp.viewmodel.teller.EditCustomerViewModel;

public class EditCustomerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_edit_customer);

        int tellerUserId = getIntent().getIntExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, 0);

        EditText etCustomerId = findViewById(R.id.etCustomerId);
        EditText etAddress    = findViewById(R.id.etAddress);
        EditText etCity       = findViewById(R.id.etCity);
        EditText etState      = findViewById(R.id.etState);
        EditText etPin        = findViewById(R.id.etPin);
        EditText etPhone      = findViewById(R.id.etPhone);
        EditText etEmail      = findViewById(R.id.etEmail);
        EditText etDailyLimit = findViewById(R.id.etDailyLimit);
        Button btnLoad        = findViewById(R.id.btnLoad);
        Button btnSave        = findViewById(R.id.btnSave);
        TextView tvStatus     = findViewById(R.id.tvStatus);

        EditCustomerViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(EditCustomerViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
        });
        vm.getLoadedCustomer().observe(this, u -> {
            if (u == null) return;
            etAddress.setText(u.getAddress());
            etCity.setText(u.getCity());
            etState.setText(u.getState());
            etPin.setText(u.getPin());
            etPhone.setText(u.getPhone());
            etEmail.setText(u.getEmail());
            etDailyLimit.setText(String.valueOf(u.getDailyLimit()));
        });

        btnLoad.setOnClickListener(v -> vm.load(tellerUserId, etCustomerId.getText().toString()));
        btnSave.setOnClickListener(v -> {
            int cid;
            try { cid = Integer.parseInt(etCustomerId.getText().toString()); }
            catch (NumberFormatException nfe) { return; }
            long limit;
            try { limit = Long.parseLong(etDailyLimit.getText().toString()); }
            catch (NumberFormatException nfe) { limit = -1L; }
            vm.save(tellerUserId, cid,
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
