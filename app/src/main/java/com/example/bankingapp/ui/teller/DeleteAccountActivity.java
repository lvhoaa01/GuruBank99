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
import com.example.bankingapp.viewmodel.teller.DeleteAccountViewModel;

public class DeleteAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_delete_account);

        int tellerUserId = getIntent().getIntExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, 0);

        EditText etAccountNumber = findViewById(R.id.etAccountNumber);
        Button btnDelete         = findViewById(R.id.btnDelete);
        TextView tvStatus        = findViewById(R.id.tvStatus);

        DeleteAccountViewModel vm = new ViewModelProvider(this, new ViewModelFactory())
                .get(DeleteAccountViewModel.class);

        vm.getState().observe(this, s -> {
            if (s == null) return;
            tvStatus.setText(s.message == null ? "" : s.message);
            tvStatus.setTextColor(getResources().getColor(
                    s.kind == UiState.Kind.SUCCESS ? R.color.success : R.color.error));
        });

        btnDelete.setOnClickListener(v ->
                vm.delete(tellerUserId, etAccountNumber.getText().toString()));
    }
}
