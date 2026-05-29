package com.example.bankingapp.rtm;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bankingapp.screenshot.ScreenshotHelper;
import com.example.bankingapp.ui.teller.DeleteAccountActivity;
import com.example.bankingapp.ui.teller.EditAccountActivity;
import com.example.bankingapp.ui.teller.NewAccountActivity;
import com.example.bankingapp.ui.teller.TellerStatementActivity;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * RTM group C — Teller: New/Edit/Delete Account + Balance/Statement (UiAutomator).
 *
 * Teller T001 (CN001). 9900000003 thuộc C10002 (CN002) → dùng cho branch-isolation.
 * Ordering: tc_delacc_* → tc_editacc_* → tc_newacc_* → tc_tellerstmt_*.
 * Reseed DB trước khi chạy class này.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RtmScreenshotC extends RtmBaseTest {

    private static final String G = "C";
    private static final long DB_WAIT = 2500L;

    /* ===================== Delete Account ===================== */

    @Test public void tc_delacc_balanceNonZero() {
        openSession(TELLER_1);
        launchTeller(DeleteAccountActivity.class, TELLER_1);
        type("etAccountNumber", ACC_SAVING_C1);    // balance 5M != 0 → F36
        tap("btnDelete");
        ScreenshotHelper.settleAndCapture(G, "TC_DELACC_005", DB_WAIT);
    }

    @Test public void tc_delacc_branchIsolation() {
        openSession(TELLER_1);
        launchTeller(DeleteAccountActivity.class, TELLER_1);
        type("etAccountNumber", ACC_CURRENT_C2);   // CN002 → F37
        tap("btnDelete");
        ScreenshotHelper.settleAndCapture(G, "TC_DELACC_006", DB_WAIT);
    }

    /* ===================== Edit Account ===================== */

    @Test public void tc_editacc_loadOk() {
        openSession(TELLER_1);
        launchTeller(EditAccountActivity.class, TELLER_1);
        type("etAccountNumber", ACC_SAVING_C1);    // cùng chi nhánh → load
        tap("btnLoad");
        ScreenshotHelper.settleAndCapture(G, "TC_EDITACC_LOAD", DB_WAIT);
    }

    @Test public void tc_editacc_branchIsolation() {
        openSession(TELLER_1);
        launchTeller(EditAccountActivity.class, TELLER_1);
        type("etAccountNumber", ACC_CURRENT_C2);   // CN002 → F34
        tap("btnLoad");
        ScreenshotHelper.settleAndCapture(G, "TC_EDITACC_005", DB_WAIT);
    }

    /* ===================== New Account ===================== */

    @Test public void tc_newacc_below500() {
        openSession(TELLER_1);
        launchTeller(NewAccountActivity.class, TELLER_1);
        type("etCustomerId", "10001");
        type("etAccountType", "SAVING");
        type("etInitialDeposit", "499");           // < 500 → F31
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_NEWACC_006", DB_WAIT);
    }

    @Test public void tc_newacc_branchIsolation() {
        openSession(TELLER_1);
        launchTeller(NewAccountActivity.class, TELLER_1);
        type("etCustomerId", "10002");             // CN002 → F32
        type("etAccountType", "CURRENT");
        type("etInitialDeposit", "1000");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_NEWACC_009", DB_WAIT);
    }

    @Test public void tc_newacc_success() {
        openSession(TELLER_1);
        launchTeller(NewAccountActivity.class, TELLER_1);
        type("etCustomerId", "10001");
        type("etAccountType", "CURRENT");
        type("etInitialDeposit", "1000000");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_NEWACC_012", DB_WAIT);
    }

    /* ===================== Teller Balance / Statement ===================== */

    @Test public void tc_tellerstmt_balanceOk() {
        openSession(TELLER_1);
        launchTeller(TellerStatementActivity.class, TELLER_1);
        type("etAccount", ACC_SAVING_C1);
        tap("btnBalance");
        ScreenshotHelper.settleAndCapture(G, "TC_BAL_004", DB_WAIT);
    }

    @Test public void tc_tellerstmt_branchIsolation() {
        openSession(TELLER_1);
        launchTeller(TellerStatementActivity.class, TELLER_1);
        type("etAccount", ACC_CURRENT_C2);         // CN002 → F1
        tap("btnBalance");
        ScreenshotHelper.settleAndCapture(G, "TC_BAL_branch", DB_WAIT);
    }
}
