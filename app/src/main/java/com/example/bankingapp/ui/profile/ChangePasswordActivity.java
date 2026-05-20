package com.example.bankingapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.ui.login.LoginActivity;
import com.example.bankingapp.utils.SessionManager;
import com.example.bankingapp.viewmodel.ChangePasswordViewModel;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        int customerId = getIntent().getIntExtra(DashboardActivity.EXTRA_CUSTOMER_ID, 0);

        EditText etOld   = findViewById(R.id.etOldPassword);
        EditText etNew   = findViewById(R.id.etNewPassword);
        EditText etConf  = findViewById(R.id.etConfirmPassword);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        TextView tvStatus = findViewById(R.id.tvStatus);

        ChangePasswordViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(ChangePasswordViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
            if (s.kind == UiState.Kind.SUCCESS) {
                SessionManager.get().logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        btnSubmit.setOnClickListener(v -> vm.change(
                customerId,
                etOld.getText().toString(),
                etNew.getText().toString(),
                etConf.getText().toString()));
    }
}
