package com.example.bankingapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.UserRole;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.ui.register.RegisterActivity;
import com.example.bankingapp.ui.teller.TellerDashboardActivity;
import com.example.bankingapp.viewmodel.LoginViewModel;
import com.example.bankingapp.viewmodel.UiState;
import com.example.bankingapp.viewmodel.ViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_CUSTOMER_ID = "customerId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin     = findViewById(R.id.btnLogin);
        Button btnGoReg     = findViewById(R.id.btnGoRegister);
        TextView tvError    = findViewById(R.id.tvError);

        LoginViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(LoginViewModel.class);

        vm.getState().observe(this, state -> {
            if (state == null) return;
            if (state.kind == UiState.Kind.SUCCESS) {
                User user = (User) state.payload;
                Class<?> target = user.getRole() == UserRole.TELLER
                        ? TellerDashboardActivity.class
                        : DashboardActivity.class;
                Intent i = new Intent(this, target);
                i.putExtra(EXTRA_CUSTOMER_ID, user.getCustomerId());
                startActivity(i);
                finish();
            } else if (state.kind == UiState.Kind.ERROR) {
                tvError.setText(state.message);
            } else {
                tvError.setText("");
            }
        });

        btnLogin.setOnClickListener(v ->
                vm.login(etUsername.getText().toString(), etPassword.getText().toString()));
        btnGoReg.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}
