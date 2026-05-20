package com.example.bankingapp;

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

import com.example.bankingapp.repository.RepositoryProvider;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.ui.transfer.TransferActivity;
import com.example.bankingapp.utils.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TransferEspressoTest {

    @Before public void setUp() {
        RepositoryProvider.init();
        SessionManager.get().login(10001);
    }

    @Test public void transfer_invalidAccount_showsError() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), TransferActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(DashboardActivity.EXTRA_CUSTOMER_ID, 10001);
        InstrumentationRegistry.getInstrumentation().getTargetContext().startActivity(i);

        onView(withId(R.id.etSource)).perform(typeText("9900000001"), closeSoftKeyboard());
        onView(withId(R.id.etDest)).perform(typeText("9900000001"), closeSoftKeyboard()); // same as src
        onView(withId(R.id.etAmount)).perform(typeText("1000"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(typeText("Test"), closeSoftKeyboard());
        onView(withId(R.id.btnSubmit)).perform(click());
        onView(withId(R.id.tvStatus)).check(matches(isDisplayed()));
    }
}
