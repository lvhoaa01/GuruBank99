package com.example.bankingapp.teller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.bankingapp.R;
import com.example.bankingapp.repository.RepositoryProvider;
import com.example.bankingapp.ui.teller.NewCustomerActivity;
import com.example.bankingapp.ui.teller.TellerDashboardActivity;
import com.example.bankingapp.utils.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NewCustomerEspressoTest {

    @Before public void setUp() {
        RepositoryProvider.init();
        SessionManager.get().login(1);
    }

    @Test public void newCustomer_invalidName_showsError() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), NewCustomerActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, 1);
        InstrumentationRegistry.getInstrumentation().getTargetContext().startActivity(i);

        onView(withId(R.id.etFullName)).perform(typeText("John1"), closeSoftKeyboard());
        onView(withId(R.id.etIdNumber)).perform(typeText("987654321"), closeSoftKeyboard());
        onView(withId(R.id.etDob)).perform(typeText("01/01/2000"), closeSoftKeyboard());
        onView(withId(R.id.btnSubmit)).perform(click());
        onView(withId(R.id.tvStatus)).check(matches(isDisplayed()));
    }
}
