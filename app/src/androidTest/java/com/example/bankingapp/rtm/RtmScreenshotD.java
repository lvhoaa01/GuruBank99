package com.example.bankingapp.rtm;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bankingapp.screenshot.ScreenshotHelper;
import com.example.bankingapp.ui.deposit.DepositActivity;
import com.example.bankingapp.ui.transaction.TransactionHistoryActivity;
import com.example.bankingapp.ui.withdraw.WithdrawActivity;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * RTM group D — Deposit / Withdraw / Statement UI screenshots (UiAutomator).
 *
 * Ordering (NAME_ASCENDING): tc_dep_* → tc_stmt_* → tc_wd_*.
 * DB notes (deterministic): deposit-success mutate 9900000002; withdraw rejected
 * cases (insufficient/floor) không đổi balance; withdraw-success chạy cuối.
 * Reseed DB trước khi chạy class này.
 *
 * Các T-rule "no special/no letters" cho field số (T55/T56/T58/T59) KHÔNG trigger
 * được qua UI (inputType=number lọc ký tự) → đánh N/A trong SCREENSHOT_MAP, link ValidatorsTest.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RtmScreenshotD extends RtmBaseTest {

    private static final String G = "D";
    private static final long DB_WAIT = 2500L;
    private static final long UI_WAIT = 900L;

    /* ===================== Deposit ===================== */

    @Test public void tc_dep_001_emptyAccount() {
        openSession(CUSTOMER_1);
        launchCustomer(DepositActivity.class, CUSTOMER_1);
        type("etAmount", "1000");
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_DEP_001", UI_WAIT);
    }

    @Test public void tc_dep_004_emptyAmount() {
        openSession(CUSTOMER_1);
        launchCustomer(DepositActivity.class, CUSTOMER_1);
        type("etAccount", ACC_SAVING_C1);
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_DEP_004", UI_WAIT);
    }

    @Test public void tc_dep_007_emptyDesc() {
        openSession(CUSTOMER_1);
        launchCustomer(DepositActivity.class, CUSTOMER_1);
        type("etAccount", ACC_SAVING_C1);
        type("etAmount", "1000");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_DEP_007", UI_WAIT);
    }

    @Test public void tc_dep_008_accountNotFound() {
        openSession(CUSTOMER_1);
        launchCustomer(DepositActivity.class, CUSTOMER_1);
        type("etAccount", "9900009999");
        type("etAmount", "1000");
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_DEP_008", DB_WAIT);
    }

    @Test public void tc_dep_011_zeroAmount() {
        openSession(CUSTOMER_1);
        launchCustomer(DepositActivity.class, CUSTOMER_1);
        type("etAccount", ACC_SAVING_C1);
        type("etAmount", "0");
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_DEP_011", DB_WAIT);
    }

    @Test public void tc_dep_012_success() {
        openSession(CUSTOMER_1);
        launchCustomer(DepositActivity.class, CUSTOMER_1);
        type("etAccount", ACC_CURRENT_C1);          // 9900000002 — tránh đụng SAVING dùng cho withdraw
        type("etAmount", "500000");
        type("etDescription", "Salary");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_DEP_012", DB_WAIT);
    }

    /* ===================== Statement ===================== */

    @Test public void tc_stmt_mini() {
        openSession(CUSTOMER_1);
        launchCustomer(TransactionHistoryActivity.class, CUSTOMER_1);
        type("etAccount", ACC_SAVING_C1);
        tap("btnMini");
        ScreenshotHelper.settleAndCapture(G, "TC_MINI_004", DB_WAIT);
    }

    @Test public void tc_stmt_filter() {
        openSession(CUSTOMER_1);
        launchCustomer(TransactionHistoryActivity.class, CUSTOMER_1);
        type("etAccount", ACC_SAVING_C1);
        type("etMinAmount", "0");
        type("etMaxCount", "10");
        tap("btnFilter");
        ScreenshotHelper.settleAndCapture(G, "TC_CUST_015", DB_WAIT);
    }

    /* ===================== Withdraw ===================== */

    @Test public void tc_wd_insufficient() {
        openSession(CUSTOMER_1);
        launchCustomer(WithdrawActivity.class, CUSTOMER_1);
        type("etAccount", ACC_SAVING_C1);
        type("etAmount", "99999999");
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_WD_009", DB_WAIT);
    }

    @Test public void tc_wd_savingfloor() {
        openSession(CUSTOMER_1);
        launchCustomer(WithdrawActivity.class, CUSTOMER_1);
        type("etAccount", ACC_SAVING_C1);           // SAVING 5,000,000
        type("etAmount", "4999600");                // còn 400 < 500 → SAVING_FLOOR
        type("etDescription", "test");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_WD_015", DB_WAIT);
    }

    @Test public void tc_wd_success() {
        openSession(CUSTOMER_1);
        launchCustomer(WithdrawActivity.class, CUSTOMER_1);
        type("etAccount", ACC_SAVING_C1);
        type("etAmount", "100000");
        type("etDescription", "ATM");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_WD_013", DB_WAIT);
    }
}
