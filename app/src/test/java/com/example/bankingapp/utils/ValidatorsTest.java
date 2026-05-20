package com.example.bankingapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Black-box tests for {@link Validators}.
 *
 * Each input field exercises equivalence-partition + boundary classes:
 *  - valid (happy path)
 *  - empty
 *  - leading whitespace
 *  - contains letters / digits / special characters
 *  - boundary lengths (PIN 5/6/7, password 7/8/9, ID 9/10/11/12/13)
 */
public class ValidatorsTest {

    /* ===== Customer name (T4..T7) ===== */
    @Test public void customerName_valid() {
        assertTrue(Validators.validateCustomerName("Nguyen Van A").isValid());
    }
    @Test public void customerName_empty_T6() {
        assertEquals("T6", Validators.validateCustomerName("").getErrorCode());
    }
    @Test public void customerName_leadingSpace_T7() {
        assertEquals("T7", Validators.validateCustomerName(" John").getErrorCode());
    }
    @Test public void customerName_withDigit_T4() {
        assertEquals("T4", Validators.validateCustomerName("John1").getErrorCode());
    }
    @Test public void customerName_withSpecial_T5() {
        assertEquals("T5", Validators.validateCustomerName("John@").getErrorCode());
    }

    /* ===== PIN (T18..T22) ===== */
    @Test public void pin_valid_6digits() {
        assertTrue(Validators.validatePin("123456").isValid());
    }
    @Test public void pin_empty_T19() {
        assertEquals("T19", Validators.validatePin("").getErrorCode());
    }
    @Test public void pin_tooShort_T21() {
        assertEquals("T21", Validators.validatePin("12345").getErrorCode());
    }
    @Test public void pin_tooLong_T21() {
        assertEquals("T21", Validators.validatePin("1234567").getErrorCode());
    }
    @Test public void pin_withLetter_T18() {
        assertEquals("T18", Validators.validatePin("12345A").getErrorCode());
    }
    @Test public void pin_withSpecial_T20() {
        assertEquals("T20", Validators.validatePin("12345!").getErrorCode());
    }
    @Test public void pin_leadingSpace_T22() {
        assertEquals("T22", Validators.validatePin(" 12345").getErrorCode());
    }

    /* ===== Email (T27..T29) ===== */
    @Test public void email_valid() {
        assertTrue(Validators.validateEmail("a@b.co").isValid());
    }
    @Test public void email_empty_T27() {
        assertEquals("T27", Validators.validateEmail("").getErrorCode());
    }
    @Test public void email_leadingSpace_T29() {
        assertEquals("T29", Validators.validateEmail(" a@b.co").getErrorCode());
    }
    @Test public void email_invalidFormat_T28() {
        assertEquals("T28", Validators.validateEmail("not-an-email").getErrorCode());
    }

    /* ===== ID number (T30..T31) ===== */
    @Test public void id_valid9digits() { assertTrue(Validators.validateIdNumber("123456789").isValid()); }
    @Test public void id_valid12digits() { assertTrue(Validators.validateIdNumber("123456789012").isValid()); }
    @Test public void id_boundary10_invalid() { assertFalse(Validators.validateIdNumber("1234567890").isValid()); }
    @Test public void id_boundary11_invalid() { assertFalse(Validators.validateIdNumber("12345678901").isValid()); }
    @Test public void id_letters_T31() { assertEquals("T31", Validators.validateIdNumber("1234A6789").getErrorCode()); }

    /* ===== Amount ===== */
    @Test public void amount_valid_positive() { assertTrue(Validators.validateAmount("1000").isValid()); }
    @Test public void amount_zero_rejected() { assertEquals("AMT_NONPOSITIVE", Validators.validateAmount("0").getErrorCode()); }
    @Test public void amount_negative_rejected() { assertEquals("AMT_SPECIAL", Validators.validateAmount("-100").getErrorCode()); }
    @Test public void amount_empty() { assertEquals("AMT_EMPTY", Validators.validateAmount("").getErrorCode()); }
    @Test public void amount_letters() { assertEquals("AMT_LETTERS", Validators.validateAmount("12a3").getErrorCode()); }
    @Test public void amount_specials() { assertEquals("AMT_SPECIAL", Validators.validateAmount("1.5").getErrorCode()); }
    @Test public void amount_one_boundary() { assertTrue(Validators.validateAmount("1").isValid()); }

    /* ===== Password (T105..T108) — boundary tests ===== */
    @Test public void password_8chars_validIfDigitAndSpecial() {
        assertTrue(Validators.validateNewPassword("Abcde1!a").isValid());
    }
    @Test public void password_7chars_tooShort_T108() {
        assertEquals("T108", Validators.validateNewPassword("Ab1!abc").getErrorCode());
    }
    @Test public void password_noDigit_T106() {
        assertEquals("T106", Validators.validateNewPassword("Abcdef!@").getErrorCode());
    }
    @Test public void password_noSpecial_T107() {
        assertEquals("T107", Validators.validateNewPassword("Abcdef12").getErrorCode());
    }
    @Test public void password_confirmMismatch_T110() {
        assertEquals("T110", Validators.validatePasswordConfirmation("Abc1234!", "Different").getErrorCode());
    }
    @Test public void password_confirmEmpty_T109() {
        assertEquals("T109", Validators.validatePasswordConfirmation("Abc1234!", "").getErrorCode());
    }

    /* ===== Account number — invariant across screens ===== */
    @Test public void account_valid() { assertTrue(Validators.validateAccountNumber("9900000001").isValid()); }
    @Test public void account_empty() { assertEquals("ACCT_EMPTY", Validators.validateAccountNumber("").getErrorCode()); }
    @Test public void account_withLetters() { assertEquals("ACCT_LETTERS", Validators.validateAccountNumber("990000000A").getErrorCode()); }
    @Test public void account_withSpecial() { assertEquals("ACCT_SPECIAL", Validators.validateAccountNumber("99-0000001").getErrorCode()); }

    /* ===== Phone ===== */
    @Test public void phone_validVN() { assertTrue(Validators.validatePhone("0901234567").isValid()); }
    @Test public void phone_tooShort() { assertFalse(Validators.validatePhone("12345").isValid()); }
    @Test public void phone_withLetter_T25() { assertEquals("T25", Validators.validatePhone("0901a23456").getErrorCode()); }
}
