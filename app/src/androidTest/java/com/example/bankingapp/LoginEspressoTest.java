package com.example.bankingapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bankingapp.ui.login.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginEspressoTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> rule = new ActivityScenarioRule<>(LoginActivity.class);

    @Test public void emptyForm_showsValidationError() {
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.tvError)).check(matches(isDisplayed()));
    }

    @Test public void invalidCredentials_showError() {
        onView(withId(R.id.etUsername)).perform(typeText("C10001"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("wrong"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.tvError)).check(matches(isDisplayed()));
    }

    @Test public void validCredentials_opensDashboard() {
        onView(withId(R.id.etUsername)).perform(typeText("C10001"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("Password1!"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        // Dashboard shows greeting with user's name
        onView(withText("Bank99")).check(matches(isDisplayed()));
    }
}
