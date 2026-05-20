package com.example.bankingapp.ui.register;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.viewmodel.RegisterViewModel;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etFullName       = findViewById(R.id.etFullName);
        EditText etIdNumber       = findViewById(R.id.etIdNumber);
        EditText etDob            = findViewById(R.id.etDob);
        EditText etAddress        = findViewById(R.id.etAddress);
        EditText etCity           = findViewById(R.id.etCity);
        EditText etState          = findViewById(R.id.etState);
        EditText etPin            = findViewById(R.id.etPin);
        EditText etPhone          = findViewById(R.id.etPhone);
        EditText etEmail          = findViewById(R.id.etEmail);
        EditText etPassword       = findViewById(R.id.etPassword);
        EditText etInitialDeposit = findViewById(R.id.etInitialDeposit);
        Button btnRegister        = findViewById(R.id.btnRegister);
        TextView tvStatus         = findViewById(R.id.tvStatus);

        RegisterViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(RegisterViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            if (s.kind == UiState.Kind.SUCCESS) {
                tvStatus.setTextColor(getResources().getColor(R.color.success));
            } else if (s.kind == UiState.Kind.ERROR) {
                tvStatus.setTextColor(getResources().getColor(R.color.error));
            }
        });

        btnRegister.setOnClickListener(v -> vm.register(
                etFullName.getText().toString(),
                etIdNumber.getText().toString(),
                etDob.getText().toString(),
                etAddress.getText().toString(),
                etCity.getText().toString(),
                etState.getText().toString(),
                etPin.getText().toString(),
                etPhone.getText().toString(),
                etEmail.getText().toString(),
                etPassword.getText().toString(),
                etInitialDeposit.getText().toString()));
    }
}
