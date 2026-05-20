package com.example.bankingapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bankingapp.R;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.viewmodel.ProfileViewModel;
import com.example.bankingapp.viewmodel.ViewModelFactory;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        int customerId = getIntent().getIntExtra(DashboardActivity.EXTRA_CUSTOMER_ID, 0);

        TextView tvProfile         = findViewById(R.id.tvProfile);
        Button btnChangePassword   = findViewById(R.id.btnChangePassword);

        ProfileViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(ProfileViewModel.class);

        vm.getUser().observe(this, u -> {
            if (u == null) return;
            String text = "ID: " + u.getCustomerId()
                    + "\nUsername: " + u.getUsername()
                    + "\nName: " + safe(u.getFullName())
                    + "\nEmail: " + safe(u.getEmail())
                    + "\nPhone: " + safe(u.getPhone())
                    + "\nDaily limit: " + u.getDailyLimit();
            tvProfile.setText(text);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent i = new Intent(this, ChangePasswordActivity.class);
            i.putExtra(DashboardActivity.EXTRA_CUSTOMER_ID, customerId);
            startActivity(i);
        });

        vm.load(customerId);
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
