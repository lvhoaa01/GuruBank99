package com.example.bankingapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bankingapp.ui.register.RegisterActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RegisterEspressoTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> rule = new ActivityScenarioRule<>(RegisterActivity.class);

    @Test public void invalidPin_showsError() {
        onView(withId(R.id.etFullName)).perform(typeText("New Customer"), closeSoftKeyboard());
        onView(withId(R.id.etIdNumber)).perform(typeText("987654321"), closeSoftKeyboard());
        onView(withId(R.id.etDob)).perform(typeText("01/01/2000"), closeSoftKeyboard());
        onView(withId(R.id.etAddress)).perform(typeText("123 Main"), closeSoftKeyboard());
        onView(withId(R.id.etCity)).perform(typeText("Hanoi"), closeSoftKeyboard());
        onView(withId(R.id.etState)).perform(typeText("North"), closeSoftKeyboard());
        onView(withId(R.id.etPin)).perform(typeText("123"), closeSoftKeyboard());      // too short
        onView(withId(R.id.etPhone)).perform(typeText("0901234500"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(typeText("x@y.co"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("Aa1!aaaa"), closeSoftKeyboard());
        onView(withId(R.id.etInitialDeposit)).perform(typeText("1000"), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(click());
        onView(withId(R.id.tvStatus)).check(matches(isDisplayed()));
    }
}
