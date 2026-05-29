package com.example.bankingapp.rtm;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.example.bankingapp.screenshot.ScreenshotHelper;
import com.example.bankingapp.ui.transfer.TransferActivity;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * RTM group E — Fund Transfer + OTP UI screenshots (UiAutomator).
 *
 * TransferViewModel dùng app-scoped ViewModelStore (chia sẻ giữa TransferActivity
 * và OtpActivity). Vì state phase tồn tại xuyên test trong cùng process, các test
 * error (kết thúc ở phase FAILED, không auto-navigate) chạy TRƯỚC; luồng OTP đầy đủ
 * gộp vào 1 test chạy CUỐI để tránh stale OTP_SENT làm test sau auto nhảy màn OTP.
 *
 * Ordering: tc_tf_01_* → tc_tf_02_* → tc_tf_03_* → tc_tf_04_otpflow.
 * Reseed DB trước khi chạy class này.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RtmScreenshotE extends RtmBaseTest {

    private static final String G = "E";
    private static final long DB_WAIT = 2800L;
    private static final long UI_WAIT = 1000L;

    @Test public void tc_tf_01_emptySource() {
        openSession(CUSTOMER_1);
        launchCustomer(TransferActivity.class, CUSTOMER_1);
        type("etDest", ACC_CURRENT_C2);
        type("etAmount", "100000");
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_TF_001", UI_WAIT);
    }

    @Test public void tc_tf_02_sameAccount() {
        openSession(CUSTOMER_1);
        launchCustomer(TransferActivity.class, CUSTOMER_1);
        type("etSource", ACC_SAVING_C1);
        type("etDest", ACC_SAVING_C1);
        type("etAmount", "100000");
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_TF_016", DB_WAIT);   // F10 same account
    }

    @Test public void tc_tf_03_insufficient() {
        openSession(CUSTOMER_1);
        launchCustomer(TransferActivity.class, CUSTOMER_1);
        type("etSource", ACC_SAVING_C1);
        type("etDest", ACC_CURRENT_C2);
        type("etAmount", "99999999");
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_TF_017", DB_WAIT);   // F11 insufficient
    }

    /** Luồng OTP đầy đủ: gửi OTP → nhập sai → nhập đúng → hoàn tất. 3 screenshot. */
    @Test public void tc_tf_04_otpflow() {
        openSession(CUSTOMER_1);
        launchCustomer(TransferActivity.class, CUSTOMER_1);
        type("etSource", ACC_SAVING_C1);
        type("etDest", ACC_CURRENT_C2);
        type("etAmount", "100000");
        type("etDescription", "Lunch");
        tap("btnSubmit");
        // → OtpActivity hiện (OTP_SENT)
        ScreenshotHelper.settleAndCapture(G, "TC_TF_022", DB_WAIT);   // F14 OTP required

        // Nhập sai OTP
        type("etOtp", "000000");
        tap("btnVerify");
        ScreenshotHelper.settleAndCapture(G, "TC_OTP_005", UI_WAIT);  // "Wrong OTP. Attempts left: 2"

        // Đọc OTP đang hiển thị, nhập đúng → hoàn tất
        String otp = readOtp();
        type("etOtp", otp);
        tap("btnVerify");
        ScreenshotHelper.settleAndCapture(G, "TC_TF_025", DB_WAIT);   // "Transfer completed"
    }

    /** Đọc 6 chữ số OTP từ tvDisplayedOtp ("OTP for testing: 123456"). */
    private String readOtp() {
        UiObject2 v = device().wait(Until.findObject(By.res(PKG, "tvDisplayedOtp")), 5000);
        if (v == null || v.getText() == null) throw new AssertionError("OTP text not found");
        return v.getText().replaceAll("\\D", "");
    }
}
