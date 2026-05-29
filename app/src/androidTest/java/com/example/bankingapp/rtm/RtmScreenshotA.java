package com.example.bankingapp.rtm;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bankingapp.screenshot.ScreenshotHelper;
import com.example.bankingapp.ui.dashboard.DashboardActivity;
import com.example.bankingapp.ui.login.LoginActivity;
import com.example.bankingapp.ui.profile.ChangePasswordActivity;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * RTM group A — Login / Logout / Change Password UI screenshots (UiAutomator).
 *
 * Ordering (NAME_ASCENDING): tc_chgpw_* → tc_login_* → tc_logout_*.
 *  - tc_chgpw_011 đổi password CUSTOMER_2 (không phá C10001 cho login tests).
 *  - tc_login_004 (success) chạy trước chuỗi sai 005→006→007 (lock C10001).
 *  - Reseed DB trước khi chạy class này.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RtmScreenshotA extends RtmBaseTest {

    private static final String G = "A";
    private static final long DB_WAIT = 2500L;
    private static final long UI_WAIT = 900L;

    /* ===================== Change Password ===================== */

    @Test public void tc_chgpw_001_oldEmpty() {
        openSession(CUSTOMER_1);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_1);
        type("etNewPassword", "Aa1!aaaa");
        type("etConfirmPassword", "Aa1!aaaa");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_001", UI_WAIT);
    }

    @Test public void tc_chgpw_002_newEmpty() {
        openSession(CUSTOMER_1);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_1);
        type("etOldPassword", "Password1!");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_002", UI_WAIT);
    }

    @Test public void tc_chgpw_003_noDigit() {
        openSession(CUSTOMER_1);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_1);
        type("etOldPassword", "Password1!");
        type("etNewPassword", "Abcdef!@");
        type("etConfirmPassword", "Abcdef!@");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_003", UI_WAIT);
    }

    @Test public void tc_chgpw_004_noSpecial() {
        openSession(CUSTOMER_1);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_1);
        type("etOldPassword", "Password1!");
        type("etNewPassword", "Abcdef12");
        type("etConfirmPassword", "Abcdef12");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_004", UI_WAIT);
    }

    @Test public void tc_chgpw_005_tooShort() {
        openSession(CUSTOMER_1);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_1);
        type("etOldPassword", "Password1!");
        type("etNewPassword", "Ab1!abc");
        type("etConfirmPassword", "Ab1!abc");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_005", UI_WAIT);
    }

    @Test public void tc_chgpw_007_confirmEmpty() {
        openSession(CUSTOMER_1);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_1);
        type("etOldPassword", "Password1!");
        type("etNewPassword", "Aa1!aaaa");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_007", UI_WAIT);
    }

    @Test public void tc_chgpw_008_mismatch() {
        openSession(CUSTOMER_1);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_1);
        type("etOldPassword", "Password1!");
        type("etNewPassword", "Aa1!aaaa");
        type("etConfirmPassword", "Different9!");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_008", UI_WAIT);
    }

    @Test public void tc_chgpw_010_wrongOld() {
        openSession(CUSTOMER_1);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_1);
        type("etOldPassword", "WrongOld9!");
        type("etNewPassword", "Aa1!aaaa");
        type("etConfirmPassword", "Aa1!aaaa");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_010", DB_WAIT);
    }

    @Test public void tc_chgpw_011_success() {
        openSession(CUSTOMER_2);
        launchCustomer(ChangePasswordActivity.class, CUSTOMER_2);
        type("etOldPassword", "Password1!");
        type("etNewPassword", "NewPass9!");
        type("etConfirmPassword", "NewPass9!");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_CHGPW_011", DB_WAIT);
    }

    /* ===================== Login ===================== */

    @Test public void tc_login_001_emptyUsername() {
        launchPlain(LoginActivity.class);
        type("etPassword", "Password1!");
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_001", UI_WAIT);
    }

    @Test public void tc_login_002_emptyPassword() {
        launchPlain(LoginActivity.class);
        type("etUsername", "C10001");
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_002", UI_WAIT);
    }

    @Test public void tc_login_003_bothEmpty() {
        launchPlain(LoginActivity.class);
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_003", UI_WAIT);
    }

    @Test public void tc_login_004_success() {
        launchPlain(LoginActivity.class);
        type("etUsername", "C10001");
        type("etPassword", "Password1!");
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_004", DB_WAIT);
    }

    @Test public void tc_login_005_wrong1() {
        launchPlain(LoginActivity.class);
        type("etUsername", "C10001");
        type("etPassword", "WrongPwd9!");
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_005", DB_WAIT);
    }

    @Test public void tc_login_006_wrong2() {
        launchPlain(LoginActivity.class);
        type("etUsername", "C10001");
        type("etPassword", "WrongPwd9!");
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_006", DB_WAIT);
    }

    @Test public void tc_login_007_lockout() {
        launchPlain(LoginActivity.class);
        type("etUsername", "C10001");
        type("etPassword", "WrongPwd9!");
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_007", DB_WAIT);
    }

    @Test public void tc_login_008_lockedAccount() {
        launchPlain(LoginActivity.class);
        type("etUsername", "C10003");
        type("etPassword", "Password1!");
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_008", DB_WAIT);
    }

    @Test public void tc_login_010_unknownUser() {
        launchPlain(LoginActivity.class);
        type("etUsername", "C99999");
        type("etPassword", "Password1!");
        tap("btnLogin");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGIN_010", DB_WAIT);
    }

    /* ===================== Logout ===================== */

    @Test public void tc_logout_001() {
        openSession(CUSTOMER_1);
        launchCustomer(DashboardActivity.class, CUSTOMER_1);
        ScreenshotHelper.settleAndCapture(G, "TC_LOGOUT_001_before", DB_WAIT);
        tap("btnLogout");
        ScreenshotHelper.settleAndCapture(G, "TC_LOGOUT_001", UI_WAIT);
    }
}
