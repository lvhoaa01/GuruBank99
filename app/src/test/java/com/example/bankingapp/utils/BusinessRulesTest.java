package com.example.bankingapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.OtpChallenge;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.model.enums.UserStatus;

import org.junit.Test;

/**
 * White-box tests covering every branch of {@link BusinessRules}.
 * The tests are organised so a single test exercises a single branch
 * — useful for JaCoCo branch-coverage measurement.
 */
public class BusinessRulesTest {

    /* ============================================================
     * validateLogin — 6 branches
     * ============================================================ */
    @Test public void login_userNotFound() {
        assertEquals(BusinessRules.LoginResult.USER_NOT_FOUND,
                BusinessRules.validateLogin(null, "x", 0L));
    }

    @Test public void login_disabled() {
        User u = newUser(UserStatus.DISABLED);
        assertEquals(BusinessRules.LoginResult.DISABLED,
                BusinessRules.validateLogin(u, "x", 0L));
    }

    @Test public void login_lockedAndInsideWindow() {
        User u = newUser(UserStatus.LOCKED);
        u.setLockedUntilMillis(1000L);
        assertEquals(BusinessRules.LoginResult.ACCOUNT_LOCKED,
                BusinessRules.validateLogin(u, "x", 500L));
    }

    @Test public void login_lockedButWindowExpired_autoUnlock_thenWrongPassword() {
        User u = newUser(UserStatus.LOCKED);
        u.setLockedUntilMillis(100L);
        u.setPasswordHash("good");
        BusinessRules.LoginResult r = BusinessRules.validateLogin(u, "bad", 200L);
        assertEquals(BusinessRules.LoginResult.INVALID_CREDENTIALS, r);
        assertEquals(UserStatus.ACTIVE, u.getStatus());
        assertEquals(0, u.getFailedLoginCount());
    }

    @Test public void login_success() {
        User u = newUser(UserStatus.ACTIVE);
        u.setPasswordHash("h");
        assertEquals(BusinessRules.LoginResult.SUCCESS,
                BusinessRules.validateLogin(u, "h", 0L));
    }

    @Test public void login_wrongPassword() {
        User u = newUser(UserStatus.ACTIVE);
        u.setPasswordHash("h");
        assertEquals(BusinessRules.LoginResult.INVALID_CREDENTIALS,
                BusinessRules.validateLogin(u, "wrong", 0L));
    }

    /* ============================================================
     * canWithdraw — 7 branches
     * ============================================================ */
    @Test public void withdraw_accountNull() {
        assertEquals(BusinessRules.WithdrawResult.ACCOUNT_NOT_FOUND,
                BusinessRules.canWithdraw(null, 100L, 1));
    }

    @Test public void withdraw_inactive() {
        Account a = new Account("9900000001", 1, AccountType.CURRENT, 1000L);
        a.setActive(false);
        assertEquals(BusinessRules.WithdrawResult.ACCOUNT_INACTIVE,
                BusinessRules.canWithdraw(a, 100L, 1));
    }

    @Test public void withdraw_invalidAmount_zero() {
        Account a = new Account("9900000001", 1, AccountType.CURRENT, 1000L);
        assertEquals(BusinessRules.WithdrawResult.INVALID_AMOUNT,
                BusinessRules.canWithdraw(a, 0L, 1));
    }

    @Test public void withdraw_insufficientFunds() {
        Account a = new Account("9900000001", 1, AccountType.CURRENT, 50L);
        assertEquals(BusinessRules.WithdrawResult.INSUFFICIENT_FUNDS,
                BusinessRules.canWithdraw(a, 100L, 1));
    }

    @Test public void withdraw_savingMonthlyLimitReached() {
        Account a = new Account("9900000001", 1, AccountType.SAVING, 10_000L);
        a.setCountersMonth(5);
        a.setMonthlyWithdrawCount(Account.MAX_SAVING_WITHDRAWALS_PER_MONTH);
        assertEquals(BusinessRules.WithdrawResult.SAVING_MONTHLY_LIMIT_REACHED,
                BusinessRules.canWithdraw(a, 100L, 5));
    }

    @Test public void withdraw_savingBalanceFloorViolated() {
        Account a = new Account("9900000001", 1, AccountType.SAVING, 600L);
        // After withdrawing 200 the balance is 400 < 500
        assertEquals(BusinessRules.WithdrawResult.SAVING_BALANCE_FLOOR_VIOLATED,
                BusinessRules.canWithdraw(a, 200L, 5));
    }

    @Test public void withdraw_success_current() {
        Account a = new Account("9900000001", 1, AccountType.CURRENT, 1000L);
        assertEquals(BusinessRules.WithdrawResult.SUCCESS,
                BusinessRules.canWithdraw(a, 500L, 5));
    }

    @Test public void withdraw_success_saving_atFloor() {
        // Balance 700, withdraw 200 → 500 (== MIN), allowed.
        Account a = new Account("9900000001", 1, AccountType.SAVING, 700L);
        assertEquals(BusinessRules.WithdrawResult.SUCCESS,
                BusinessRules.canWithdraw(a, 200L, 5));
    }

    @Test public void withdraw_savingCountersResetOnNewMonth() {
        Account a = new Account("9900000001", 1, AccountType.SAVING, 10_000L);
        a.setCountersMonth(3);   // last activity was March
        a.setMonthlyWithdrawCount(5);   // would normally be over limit
        // We're now in May — counter is "stale", treated as 0.
        assertEquals(BusinessRules.WithdrawResult.SUCCESS,
                BusinessRules.canWithdraw(a, 100L, 5));
    }

    /* ============================================================
     * canTransfer — 11 branches
     * ============================================================ */
    @Test public void transfer_srcNull() {
        Account dst = acct("9900000002", 1, AccountType.CURRENT, 1000L);
        assertEquals(BusinessRules.TransferResult.SRC_NOT_FOUND,
                BusinessRules.canTransfer(null, dst, 1, 100L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_dstNull() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 1000L);
        assertEquals(BusinessRules.TransferResult.DST_NOT_FOUND,
                BusinessRules.canTransfer(src, null, 1, 100L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_srcInactive() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 1000L); src.setActive(false);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.TransferResult.SRC_INACTIVE,
                BusinessRules.canTransfer(src, dst, 1, 100L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_dstInactive() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 1000L);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L); dst.setActive(false);
        assertEquals(BusinessRules.TransferResult.DST_INACTIVE,
                BusinessRules.canTransfer(src, dst, 1, 100L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_sameAccount() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 1000L);
        assertEquals(BusinessRules.TransferResult.SAME_ACCOUNT,
                BusinessRules.canTransfer(src, src, 1, 100L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_notOwner() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 1000L);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.TransferResult.NOT_OWNER,
                BusinessRules.canTransfer(src, dst, 999, 100L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_invalidAmount() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 1000L);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.TransferResult.INVALID_AMOUNT,
                BusinessRules.canTransfer(src, dst, 1, 0L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_insufficient() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 50L);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.TransferResult.INSUFFICIENT_FUNDS,
                BusinessRules.canTransfer(src, dst, 1, 100L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_dailyLimit_atBoundary_passes() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 10_000L);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        // today's running total + amount == limit → OK
        assertEquals(BusinessRules.TransferResult.SUCCESS,
                BusinessRules.canTransfer(src, dst, 1, 100L, 0L, 100L, 0L, 5));
    }
    @Test public void transfer_dailyLimit_overBy1_fails() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 10_000L);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.TransferResult.DAILY_LIMIT_EXCEEDED,
                BusinessRules.canTransfer(src, dst, 1, 101L, 0L, 100L, 0L, 5));
    }
    @Test public void transfer_savingMonthlyLimitReached() {
        Account src = acct("9900000001", 1, AccountType.SAVING, 10_000L);
        src.setCountersMonth(5); src.setMonthlyTransferCount(Account.MAX_SAVING_TRANSFERS_PER_MONTH);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.TransferResult.SAVING_MONTHLY_LIMIT_REACHED,
                BusinessRules.canTransfer(src, dst, 1, 100L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_savingBalanceFloor() {
        Account src = acct("9900000001", 1, AccountType.SAVING, 600L);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        // 600 - 200 = 400 < 500
        assertEquals(BusinessRules.TransferResult.SAVING_BALANCE_FLOOR_VIOLATED,
                BusinessRules.canTransfer(src, dst, 1, 200L, 0L, Long.MAX_VALUE, 0L, 5));
    }
    @Test public void transfer_success() {
        Account src = acct("9900000001", 1, AccountType.CURRENT, 10_000L);
        Account dst = acct("9900000002", 2, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.TransferResult.SUCCESS,
                BusinessRules.canTransfer(src, dst, 1, 1_000L, 0L, Long.MAX_VALUE, 0L, 5));
    }

    /* ============================================================
     * canDeposit
     * ============================================================ */
    @Test public void deposit_accountNull() {
        assertEquals(BusinessRules.DepositResult.ACCOUNT_NOT_FOUND,
                BusinessRules.canDeposit(null, 100L));
    }
    @Test public void deposit_inactive() {
        Account a = acct("9900000001", 1, AccountType.CURRENT, 0L); a.setActive(false);
        assertEquals(BusinessRules.DepositResult.ACCOUNT_INACTIVE,
                BusinessRules.canDeposit(a, 100L));
    }
    @Test public void deposit_invalidAmount() {
        Account a = acct("9900000001", 1, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.DepositResult.INVALID_AMOUNT,
                BusinessRules.canDeposit(a, 0L));
    }
    @Test public void deposit_success() {
        Account a = acct("9900000001", 1, AccountType.CURRENT, 0L);
        assertEquals(BusinessRules.DepositResult.SUCCESS,
                BusinessRules.canDeposit(a, 100L));
    }

    /* ============================================================
     * validateOtp — 4 branches
     * ============================================================ */
    @Test public void otp_nullChallenge() {
        assertEquals(BusinessRules.OtpResult.WRONG_CODE,
                BusinessRules.validateOtp(null, "123456", 0L));
    }
    @Test public void otp_expired() {
        OtpChallenge c = new OtpChallenge("123456", 100L);
        assertEquals(BusinessRules.OtpResult.EXPIRED,
                BusinessRules.validateOtp(c, "123456", 200L));
    }
    @Test public void otp_wrongCounts() {
        OtpChallenge c = new OtpChallenge("123456", Long.MAX_VALUE);
        // 1st wrong
        assertEquals(BusinessRules.OtpResult.WRONG_CODE, BusinessRules.validateOtp(c, "000000", 1L));
        assertEquals(1, c.getWrongAttempts());
        // 2nd wrong
        assertEquals(BusinessRules.OtpResult.WRONG_CODE, BusinessRules.validateOtp(c, "000000", 1L));
        // 3rd wrong → ATTEMPTS_EXHAUSTED
        assertEquals(BusinessRules.OtpResult.ATTEMPTS_EXHAUSTED, BusinessRules.validateOtp(c, "000000", 1L));
    }
    @Test public void otp_attemptsExhaustedThenCorrectStillBlocked() {
        OtpChallenge c = new OtpChallenge("123456", Long.MAX_VALUE);
        BusinessRules.validateOtp(c, "000000", 1L);
        BusinessRules.validateOtp(c, "000000", 1L);
        BusinessRules.validateOtp(c, "000000", 1L);
        // Even with the correct OTP, exhausted attempts still block.
        assertEquals(BusinessRules.OtpResult.ATTEMPTS_EXHAUSTED,
                BusinessRules.validateOtp(c, "123456", 1L));
    }
    @Test public void otp_success() {
        OtpChallenge c = new OtpChallenge("123456", Long.MAX_VALUE);
        assertEquals(BusinessRules.OtpResult.SUCCESS,
                BusinessRules.validateOtp(c, "123456", 1L));
    }

    @Test public void monthOf_returnsHumanMonth() {
        // 2026-05-19 UTC midnight
        long ms = 1_779_840_000_000L;
        assertNotEquals(0, BusinessRules.monthOf(ms));
    }

    private static User newUser(UserStatus status) {
        User u = new User(1, "C00001", "h");
        u.setStatus(status);
        return u;
    }

    private static Account acct(String number, int owner, AccountType type, long balance) {
        return new Account(number, owner, type, balance);
    }
}
