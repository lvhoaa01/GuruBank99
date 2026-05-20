package com.example.bankingapp.utils;

import java.util.regex.Pattern;

/**
 * Stateless input validators. Each rule maps to one or more "T#" items
 * from SRS section 3.9 (Technical Requirements). Methods return a
 * {@link ValidationResult} carrying a stable error code so tests can
 * assert on cause without depending on UI strings.
 *
 * Each method is intentionally written with explicit branches (return
 * on first failure) so branch / path coverage tools can measure them.
 */
public final class Validators {

    private Validators() { }

    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");
    private static final Pattern LETTERS_AND_SPACES = Pattern.compile("^[\\p{L} ]+$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9 ].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern HAS_SPECIAL_PASSWORD = Pattern.compile(".*[^A-Za-z0-9].*");

    /* ============================================================
     * Generic helpers
     * ============================================================ */

    public static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean startsWithSpace(String s) {
        return s != null && !s.isEmpty() && Character.isWhitespace(s.charAt(0));
    }

    public static boolean containsLetter(String s) {
        return s != null && HAS_LETTER.matcher(s).matches();
    }

    public static boolean containsDigit(String s) {
        return s != null && HAS_DIGIT.matcher(s).matches();
    }

    public static boolean containsSpecial(String s) {
        return s != null && SPECIAL.matcher(s).matches();
    }

    /* ============================================================
     * Customer Name — T4..T7
     * ============================================================ */
    public static ValidationResult validateCustomerName(String name) {
        if (isBlank(name)) {
            return ValidationResult.fail("T6", "Customer name is required");
        }
        if (startsWithSpace(name)) {
            return ValidationResult.fail("T7", "Customer name cannot start with whitespace");
        }
        if (containsDigit(name)) {
            return ValidationResult.fail("T4", "Customer name cannot contain digits");
        }
        if (!LETTERS_AND_SPACES.matcher(name).matches()) {
            return ValidationResult.fail("T5", "Customer name cannot contain special characters");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Address — T8..T10
     * ============================================================ */
    public static ValidationResult validateAddress(String address) {
        if (isBlank(address)) {
            return ValidationResult.fail("T8", "Address is required");
        }
        if (startsWithSpace(address)) {
            return ValidationResult.fail("T9", "Address cannot start with whitespace");
        }
        // Allow letters, digits, spaces, commas and slashes — common in Vietnamese addresses.
        if (!address.matches("^[\\p{L}0-9 ,./-]+$")) {
            return ValidationResult.fail("T10", "Address cannot contain special characters");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * City — T11..T14
     * ============================================================ */
    public static ValidationResult validateCity(String city) {
        if (isBlank(city)) {
            return ValidationResult.fail("T12", "City is required");
        }
        if (startsWithSpace(city)) {
            return ValidationResult.fail("T14", "City cannot start with whitespace");
        }
        if (containsDigit(city)) {
            return ValidationResult.fail("T13", "City cannot contain digits");
        }
        if (!LETTERS_AND_SPACES.matcher(city).matches()) {
            return ValidationResult.fail("T11", "City cannot contain special characters");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * State — T15..T17.1
     * ============================================================ */
    public static ValidationResult validateState(String state) {
        if (isBlank(state)) {
            return ValidationResult.fail("T16", "State is required");
        }
        if (startsWithSpace(state)) {
            return ValidationResult.fail("T17.1", "State cannot start with whitespace");
        }
        if (containsDigit(state)) {
            return ValidationResult.fail("T15", "State cannot contain digits");
        }
        if (!LETTERS_AND_SPACES.matcher(state).matches()) {
            return ValidationResult.fail("T17", "State cannot contain special characters");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * PIN — T18..T22 (also reused as T77..T81 in Edit Customer)
     * ============================================================ */
    public static ValidationResult validatePin(String pin) {
        if (isBlank(pin)) {
            return ValidationResult.fail("T19", "PIN is required");
        }
        if (startsWithSpace(pin)) {
            return ValidationResult.fail("T22", "PIN cannot start with whitespace");
        }
        if (containsLetter(pin)) {
            return ValidationResult.fail("T18", "PIN cannot contain letters");
        }
        if (containsSpecial(pin)) {
            return ValidationResult.fail("T20", "PIN cannot contain special characters");
        }
        if (pin.length() != 6 || !DIGITS_ONLY.matcher(pin).matches()) {
            return ValidationResult.fail("T21", "PIN must be exactly 6 digits");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Telephone — T23..T26
     * ============================================================ */
    public static ValidationResult validatePhone(String phone) {
        if (isBlank(phone)) {
            return ValidationResult.fail("T23", "Phone is required");
        }
        if (startsWithSpace(phone)) {
            return ValidationResult.fail("T26", "Phone cannot start with whitespace");
        }
        if (containsLetter(phone)) {
            return ValidationResult.fail("T25", "Phone cannot contain letters");
        }
        if (containsSpecial(phone)) {
            return ValidationResult.fail("T24", "Phone cannot contain special characters");
        }
        if (phone.length() < 9 || phone.length() > 15) {
            return ValidationResult.fail("T24", "Phone must be 9–15 digits");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Email — T27..T29
     * ============================================================ */
    public static ValidationResult validateEmail(String email) {
        if (isBlank(email)) {
            return ValidationResult.fail("T27", "Email is required");
        }
        if (startsWithSpace(email)) {
            return ValidationResult.fail("T29", "Email cannot start with whitespace");
        }
        if (!EMAIL.matcher(email).matches()) {
            return ValidationResult.fail("T28", "Email format is invalid");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * CMND/CCCD — T30..T31
     * ============================================================ */
    public static ValidationResult validateIdNumber(String idNumber) {
        if (isBlank(idNumber)) {
            return ValidationResult.fail("T30", "ID number is required");
        }
        if (!DIGITS_ONLY.matcher(idNumber).matches()) {
            return ValidationResult.fail("T31", "ID number must contain digits only");
        }
        if (idNumber.length() != 9 && idNumber.length() != 12) {
            return ValidationResult.fail("T30", "ID number must be 9 or 12 digits");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Account Number — T36..T38, T48..T50, T54..T56, T89..T94, T101..T103,
     * T111..T113. The rule is the same in every screen.
     * ============================================================ */
    public static ValidationResult validateAccountNumber(String account) {
        if (isBlank(account)) {
            return ValidationResult.fail("ACCT_EMPTY", "Account number is required");
        }
        if (containsLetter(account)) {
            return ValidationResult.fail("ACCT_LETTERS", "Account number cannot contain letters");
        }
        if (containsSpecial(account)) {
            return ValidationResult.fail("ACCT_SPECIAL", "Account number cannot contain special characters");
        }
        if (!DIGITS_ONLY.matcher(account).matches()) {
            return ValidationResult.fail("ACCT_FORMAT", "Account number must be digits only");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Customer ID — T1..T3.1
     * ============================================================ */
    public static ValidationResult validateCustomerId(String customerId) {
        if (isBlank(customerId)) {
            return ValidationResult.fail("T1", "Customer ID is required");
        }
        if (startsWithSpace(customerId)) {
            return ValidationResult.fail("T3.1", "Customer ID cannot start with whitespace");
        }
        if (containsLetter(customerId)) {
            return ValidationResult.fail("T3", "Customer ID cannot contain letters");
        }
        if (containsSpecial(customerId)) {
            return ValidationResult.fail("T2", "Customer ID cannot contain special characters");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Amount — used by Deposit / Withdraw / Transfer
     * T57..T59, T95..T97, T114..T116
     * ============================================================ */
    public static ValidationResult validateAmount(String amountStr) {
        if (isBlank(amountStr)) {
            return ValidationResult.fail("AMT_EMPTY", "Amount is required");
        }
        if (containsLetter(amountStr)) {
            return ValidationResult.fail("AMT_LETTERS", "Amount cannot contain letters");
        }
        if (containsSpecial(amountStr)) {
            return ValidationResult.fail("AMT_SPECIAL", "Amount cannot contain special characters");
        }
        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (NumberFormatException nfe) {
            return ValidationResult.fail("AMT_FORMAT", "Amount must be a number");
        }
        if (amount <= 0) {
            return ValidationResult.fail("AMT_NONPOSITIVE", "Amount must be greater than zero");
        }
        return ValidationResult.ok();
    }

    /** Parse with the assumption that {@link #validateAmount(String)} already passed. */
    public static long parseAmount(String amountStr) {
        return Long.parseLong(amountStr);
    }

    /* ============================================================
     * Description — T60, T98, T117
     * ============================================================ */
    public static ValidationResult validateDescription(String description) {
        if (isBlank(description)) {
            return ValidationResult.fail("DESC_EMPTY", "Description is required");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Login fields — T99, T100
     * ============================================================ */
    public static ValidationResult validateUsername(String username) {
        if (isBlank(username)) {
            return ValidationResult.fail("T99", "Username is required");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validatePasswordNotEmpty(String password) {
        if (isBlank(password)) {
            return ValidationResult.fail("T100", "Password is required");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Change Password — T105..T110
     * ============================================================ */
    public static ValidationResult validateNewPassword(String newPassword) {
        if (isBlank(newPassword)) {
            return ValidationResult.fail("T105", "New password is required");
        }
        if (newPassword.length() < 8) {
            return ValidationResult.fail("T108", "Password must be at least 8 characters");
        }
        if (!HAS_DIGIT.matcher(newPassword).matches()) {
            return ValidationResult.fail("T106", "Password must contain at least one digit");
        }
        if (!HAS_SPECIAL_PASSWORD.matcher(newPassword).matches()) {
            return ValidationResult.fail("T107", "Password must contain at least one special character");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validatePasswordConfirmation(String newPassword, String confirm) {
        if (isBlank(confirm)) {
            return ValidationResult.fail("T109", "Confirm password is required");
        }
        if (!confirm.equals(newPassword)) {
            return ValidationResult.fail("T110", "Passwords do not match");
        }
        return ValidationResult.ok();
    }

    /* ============================================================
     * Date — T32, T33
     * ============================================================ */
    public static ValidationResult validateDateOfBirth(String dob) {
        if (isBlank(dob)) {
            return ValidationResult.fail("T33", "Date of birth is required");
        }
        if (!dob.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            return ValidationResult.fail("T33", "Date of birth must be DD/MM/YYYY");
        }
        return ValidationResult.ok();
    }
}
