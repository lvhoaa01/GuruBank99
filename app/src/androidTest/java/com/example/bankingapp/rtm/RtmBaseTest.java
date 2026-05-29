package com.example.bankingapp.rtm;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.example.bankingapp.repository.RepositoryProvider;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.ui.teller.TellerDashboardActivity;
import com.example.bankingapp.utils.SessionManager;

/**
 * Shared constants + UiAutomator helpers for RTM screenshot capture.
 *
 * Dùng UiAutomator (không Espresso) vì emulator chạy Android API 37 —
 * Espresso event injection (InputManager.getInstance) đã bị xoá ở API mới.
 * UiAutomator dùng UiAutomation accessibility injection, chạy tốt trên API 37,
 * và setText() không cần soft keyboard (tránh keyboard che nút).
 *
 * Strategy: launch mỗi Activity trực tiếp qua Intent (mang theo customer/teller id),
 * không điều hướng từ Login từng bước → deterministic.
 */
public abstract class RtmBaseTest {

    public static final String PKG = "com.example.bankingapp";

    public static final int CUSTOMER_1 = 10001;
    public static final int CUSTOMER_2 = 10002;
    public static final int TELLER_1   = 1;
    public static final int TELLER_2   = 2;

    public static final String ACC_SAVING_C1  = "9900000001";
    public static final String ACC_CURRENT_C1 = "9900000002";
    public static final String ACC_CURRENT_C2 = "9900000003";

    protected UiDevice device() {
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    protected void openSession(int userId) {
        RepositoryProvider.init();
        SessionManager.get().login(userId);
    }

    protected ActivityScenario<?> launchCustomer(Class<?> activity, int customerId) {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent i = new Intent(ctx, activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(DashboardActivity.EXTRA_CUSTOMER_ID, customerId);
        return ActivityScenario.launch(i);
    }

    protected ActivityScenario<?> launchTeller(Class<?> activity, int tellerUserId) {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent i = new Intent(ctx, activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(TellerDashboardActivity.EXTRA_TELLER_USER_ID, tellerUserId);
        i.putExtra(DashboardActivity.EXTRA_CUSTOMER_ID, tellerUserId);
        return ActivityScenario.launch(i);
    }

    protected ActivityScenario<?> launchPlain(Class<?> activity) {
        Context ctx = ApplicationProvider.getApplicationContext();
        return ActivityScenario.launch(
                new Intent(ctx, activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /* ---------- UiAutomator helpers (find by resource-id) ---------- */

    /** Đợi field xuất hiện rồi setText (UiAutomator tự thay nội dung, không cần keyboard). */
    protected void type(String resId, String text) {
        UiObject2 field = device().wait(Until.findObject(By.res(PKG, resId)), 5000);
        if (field == null) throw new AssertionError("View not found: " + resId);
        field.setText(text);
    }

    /** Click view theo resource-id. */
    protected void tap(String resId) {
        UiObject2 v = device().wait(Until.findObject(By.res(PKG, resId)), 5000);
        if (v == null) throw new AssertionError("Clickable not found: " + resId);
        v.click();
    }

    /** Click view theo text hiển thị (vd. nút "LOGIN"). */
    protected void tapText(String text) {
        UiObject2 v = device().wait(Until.findObject(By.text(text)), 5000);
        if (v == null) v = device().wait(Until.findObject(By.textContains(text)), 3000);
        if (v == null) throw new AssertionError("Text not found: " + text);
        v.click();
    }

    protected void waitIdle(long ms) {
        device().waitForIdle(ms);
    }
}
