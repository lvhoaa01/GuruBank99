package com.example.bankingapp.rtm;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bankingapp.screenshot.ScreenshotHelper;
import com.example.bankingapp.ui.teller.DeleteCustomerActivity;
import com.example.bankingapp.ui.teller.EditCustomerActivity;
import com.example.bankingapp.ui.teller.NewCustomerActivity;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * RTM group B — Teller: New / Edit / Delete Customer UI screenshots (UiAutomator).
 *
 * Teller T001 (CN001). Branch isolation: thao tác lên C10002 (CN002) → bị từ chối.
 * Ordering: tc_delcus_* → tc_editcus_* → tc_newcus_* (newcus_success mutate DB cuối).
 * Reseed DB trước khi chạy class này.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RtmScreenshotB extends RtmBaseTest {

    private static final String G = "B";
    private static final long DB_WAIT = 2500L;

    /* ===================== Delete Customer ===================== */

    @Test public void tc_delcus_activeAccounts() {
        openSession(TELLER_1);
        launchTeller(DeleteCustomerActivity.class, TELLER_1);
        type("etCustomerId", "10001");        // còn TK active → F28
        tap("btnDelete");
        ScreenshotHelper.settleAndCapture(G, "TC_DELCUS_006", DB_WAIT);
    }

    @Test public void tc_delcus_branchIsolation() {
        openSession(TELLER_1);
        launchTeller(DeleteCustomerActivity.class, TELLER_1);
        type("etCustomerId", "10002");        // CN002, T001 ở CN001 → F29
        tap("btnDelete");
        ScreenshotHelper.settleAndCapture(G, "TC_DELCUS_007", DB_WAIT);
    }

    /* ===================== Edit Customer ===================== */

    @Test public void tc_editcus_loadOk() {
        openSession(TELLER_1);
        launchTeller(EditCustomerActivity.class, TELLER_1);
        type("etCustomerId", "10001");        // cùng chi nhánh → load form
        tap("btnLoad");
        ScreenshotHelper.settleAndCapture(G, "TC_EDITCUS_LOAD", DB_WAIT);
    }

    @Test public void tc_editcus_branchIsolation() {
        openSession(TELLER_1);
        launchTeller(EditCustomerActivity.class, TELLER_1);
        type("etCustomerId", "10002");        // CN002 → F25
        tap("btnLoad");
        ScreenshotHelper.settleAndCapture(G, "TC_EDITCUS_014", DB_WAIT);
    }

    /* ===================== New Customer ===================== */

    @Test public void tc_newcus_invalidName() {
        openSession(TELLER_1);
        launchTeller(NewCustomerActivity.class, TELLER_1);
        type("etFullName", "John1");          // T4 numbers not allowed
        type("etIdNumber", "111222333");
        type("etDob", "01/01/1990");
        type("etAddress", "1 Main St");
        type("etCity", "Hanoi");
        type("etState", "North");
        type("etPin", "123456");
        type("etPhone", "0901234500");
        type("etEmail", "new@example.com");
        type("etDailyLimit", "1000000");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_NEWCUS_001", DB_WAIT);
    }

    @Test public void tc_newcus_dupEmail() {
        openSession(TELLER_1);
        launchTeller(NewCustomerActivity.class, TELLER_1);
        type("etFullName", "Tran Van X");
        type("etIdNumber", "222333444");
        type("etDob", "01/01/1990");
        type("etAddress", "1 Main St");
        type("etCity", "Hanoi");
        type("etState", "North");
        type("etPin", "123456");
        type("etPhone", "0901234500");
        type("etEmail", "a@example.com");     // trùng C10001 → F21
        type("etDailyLimit", "1000000");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_NEWCUS_034", DB_WAIT);
    }

    @Test public void tc_newcus_success() {
        openSession(TELLER_1);
        launchTeller(NewCustomerActivity.class, TELLER_1);
        type("etFullName", "Nguyen Van Moi");
        type("etIdNumber", "333444555");
        type("etDob", "01/01/1995");
        type("etAddress", "10 New St");
        type("etCity", "Hanoi");
        type("etState", "North");
        type("etPin", "123456");
        type("etPhone", "0909999000");
        type("etEmail", "moi@example.com");
        type("etDailyLimit", "2000000");
        tap("btnSubmit");
        ScreenshotHelper.settleAndCapture(G, "TC_NEWCUS_036", DB_WAIT);
    }
}
