package com.example.bankingapp.teller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.bankingapp.R;
import com.example.bankingapp.repository.RepositoryProvider;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.ui.teller.TellerDashboardActivity;
import com.example.bankingapp.utils.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TellerDashboardEspressoTest {

    @Before public void setUp() {
        RepositoryProvider.init();
        SessionManager.get().login(1);   // T001
    }

    @Test public void dashboard_showsAllTellerButtons() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TellerDashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(DashboardActivity.EXTRA_CUSTOMER_ID, 1);
        i.putExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, 1);
        InstrumentationRegistry.getInstrumentation().getTargetContext().startActivity(i);

        onView(withId(R.id.btnNewCustomer)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNewAccount)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTellerTransfer)).check(matches(isDisplayed()));
    }
}
