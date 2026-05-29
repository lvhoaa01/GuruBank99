package com.example.bankingapp.utils;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.OtpChallenge;
import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.AccountType;
import com.example.bankingapp.model.enums.UserStatus;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Business rules driven by SRS section 3.10 (F1..F55) and 3.5 (OTP).
 * Each public method returns a typed enum result — keeps branching
 * explicit and lets unit tests measure both branch and path coverage
 * without depending on UI / string equality.
 *
 * No state — pure functions where possible.
 */
public final class BusinessRules {

    private BusinessRules() { }

    /* ============================================================
     * Login — F50
     * ============================================================ */
    public enum LoginResult {
        SUCCESS,
        USER_NOT_FOUND,
        ACCOUNT_LOCKED,
        DISABLED,
        INVALID_CREDENTIALS
    }

    /**
     * Cyclomatic complexity: 6 branches.
     * Branches:
     *  1. user == null              → USER_NOT_FOUND
     *  2. DISABLED                  → DISABLED
     *  3. LOCKED + still in window  → ACCOUNT_LOCKED
     *  4. LOCKED + window expired   → fall through (auto-unlock)
     *  5. Password match            → SUCCESS
     *  6. Password mismatch         → INVALID_CREDENTIALS
     */
    public static LoginResult validateLogin(User user, String submittedPasswordHash, long nowMillis) {
        if (user == null) {
            return LoginResult.USER_NOT_FOUND;
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            return LoginResult.DISABLED;
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            if (nowMillis < user.getLockedUntilMillis()) {
                return LoginResult.ACCOUNT_LOCKED;
            }
            // Auto-unlock when the lockout window has expired.
            user.setStatus(UserStatus.ACTIVE);
            user.setFailedLoginCount(0);
            user.setLockedUntilMillis(0L);
        }
        if (user.getPasswordHash() != null && user.getPasswordHash().equals(submittedPasswordHash)) {
            return LoginResult.SUCCESS;
        }
        return LoginResult.INVALID_CREDENTIALS;
    }

    /* ============================================================
     * Withdrawal — F15, F16, F55 (SAVING balance floor + monthly cap)
     * ============================================================ */
    public enum WithdrawResult {
        SUCCESS,
        ACCOUNT_NOT_FOUND,
        ACCOUNT_INACTIVE,
        INVALID_AMOUNT,
        INSUFFICIENT_FUNDS,
        SAVING_MONTHLY_LIMIT_REACHED,
        SAVING_BALANCE_FLOOR_VIOLATED
    }

    /**
     * Cyclomatic complexity: 7 branches.
     */
    public static WithdrawResult canWithdraw(Account account, long amount, int currentMonth1to12) {
        if (account == null) {
            return WithdrawResult.ACCOUNT_NOT_FOUND;
        }
        if (!account.isActive()) {
            return WithdrawResult.ACCOUNT_INACTIVE;
        }
        if (amount <= 0) {
            return WithdrawResult.INVALID_AMOUNT;
        }
        if (account.getBalance() < amount) {
            return WithdrawResult.INSUFFICIENT_FUNDS;
        }
        if (account.getType() == AccountType.SAVING) {
            int monthlyWithdraws = (account.getCountersMonth() == currentMonth1to12)
                    ? account.getMonthlyWithdrawCount() : 0;
            if (monthlyWithdraws >= Account.MAX_SAVING_WITHDRAWALS_PER_MONTH) {
                return WithdrawResult.SAVING_MONTHLY_LIMIT_REACHED;
            }
            if (account.getBalance() - amount < Account.MIN_SAVING_BALANCE) {
                return WithdrawResult.SAVING_BALANCE_FLOOR_VIOLATED;
            }
        }
        return WithdrawResult.SUCCESS;
    }

    /* ============================================================
     * Transfer — F9..F13 + F55
     * ============================================================ */
    public enum TransferResult {
        SUCCESS,
        SRC_NOT_FOUND,
        DST_NOT_FOUND,
        SRC_INACTIVE,
        DST_INACTIVE,
        SAME_ACCOUNT,
        NOT_OWNER,
        INSUFFICIENT_FUNDS,
        DAILY_LIMIT_EXCEEDED,
        SAVING_MONTHLY_LIMIT_REACHED,
        SAVING_BALANCE_FLOOR_VIOLATED,
        INVALID_AMOUNT
    }

    /**
     * Cyclomatic complexity: 11 branches.
     *
     * Branches in order:
     *  src null, dst null, src inactive, dst inactive, same account,
     *  customer not owner, amount ≤ 0, insufficient balance (incl. fee),
     *  daily limit exceeded, saving monthly cap, saving balance floor.
     */
    public static TransferResult canTransfer(Account src,
                                              Account dst,
                                              int requestingCustomerId,
                                              long amount,
                                              long fee,
                                              long dailyLimit,
                                              long todaysTransferTotal,
                                              int currentMonth1to12) {
        if (src == null) {
            return TransferResult.SRC_NOT_FOUND;
        }
        if (dst == null) {
            return TransferResult.DST_NOT_FOUND;
        }
        if (!src.isActive()) {
            return TransferResult.SRC_INACTIVE;
        }
        if (!dst.isActive()) {
            return TransferResult.DST_INACTIVE;
        }
        if (src.getAccountNumber().equals(dst.getAccountNumber())) {
            return TransferResult.SAME_ACCOUNT;
        }
        if (src.getOwnerCustomerId() != requestingCustomerId) {
            return TransferResult.NOT_OWNER;
        }
        if (amount <= 0) {
            return TransferResult.INVALID_AMOUNT;
        }
        long required = amount + fee;
        if (src.getBalance() < required) {
            return TransferResult.INSUFFICIENT_FUNDS;
        }
        if (todaysTransferTotal + amount > dailyLimit) {
            return TransferResult.DAILY_LIMIT_EXCEEDED;
        }
        if (src.getType() == AccountType.SAVING) {
            int monthlyTransfers = (src.getCountersMonth() == currentMonth1to12)
                    ? src.getMonthlyTransferCount() : 0;
            if (monthlyTransfers >= Account.MAX_SAVING_TRANSFERS_PER_MONTH) {
                return TransferResult.SAVING_MONTHLY_LIMIT_REACHED;
            }
            if (src.getBalance() - required < Account.MIN_SAVING_BALANCE) {
                return TransferResult.SAVING_BALANCE_FLOOR_VIOLATED;
            }
        }
        return TransferResult.SUCCESS;
    }

    /* ============================================================
     * Deposit — F18 (account must exist & be active)
     * ============================================================ */
    public enum DepositResult {
        SUCCESS,
        ACCOUNT_NOT_FOUND,
        ACCOUNT_INACTIVE,
        INVALID_AMOUNT
    }

    public static DepositResult canDeposit(Account account, long amount) {
        if (account == null) {
            return DepositResult.ACCOUNT_NOT_FOUND;
        }
        if (!account.isActive()) {
            return DepositResult.ACCOUNT_INACTIVE;
        }
        if (amount <= 0) {
            return DepositResult.INVALID_AMOUNT;
        }
        return DepositResult.SUCCESS;
    }

    /* ============================================================
     * OTP — SRS 3.5 + F14
     * ============================================================ */
    public enum OtpResult {
        SUCCESS,
        EXPIRED,
        WRONG_CODE,
        ATTEMPTS_EXHAUSTED
    }

    /**
     * Cyclomatic complexity: 4 branches.
     */

    
    public static OtpResult validateOtp(OtpChallenge challenge, String input, long nowMillis) {
        if (challenge == null) {
            return OtpResult.WRONG_CODE;
        }
        if (challenge.isExpired(nowMillis)) {
            return OtpResult.EXPIRED;
        }
        if (challenge.getWrongAttempts() >= OtpChallenge.MAX_WRONG_ATTEMPTS) {
            return OtpResult.ATTEMPTS_EXHAUSTED;
        }
        if (!challenge.getOtpCode().equals(input)) {
            challenge.incrementWrongAttempts();
            if (challenge.getWrongAttempts() >= OtpChallenge.MAX_WRONG_ATTEMPTS) {
                return OtpResult.ATTEMPTS_EXHAUSTED;
            }
            return OtpResult.WRONG_CODE;
        }
        return OtpResult.SUCCESS;
    }

    /* ============================================================
     * Branch isolation — F8, F17, F19, F25, F29, F32, F34, F37, F47
     * "Teller KHÔNG thao tác ngoài chi nhánh mình quản lý" (SRS 3.13)
     * ============================================================ */
    // Có nghĩa
    public enum BranchAccessResult {
        ALLOWED,
        NOT_SAME_BRANCH,
        TELLER_BRANCH_MISSING,
        TARGET_BRANCH_MISSING
    }

    /**
     * Decide whether a teller from {@code tellerBranchId} may touch a customer
     * (or anything keyed on that customer's branch) belonging to
     * {@code targetBranchId}. 4-branch CFG keeps the cases explicit for
     * branch coverage tooling.
     */
    public static BranchAccessResult tellerCanAccessCustomer(String tellerBranchId, String targetBranchId) {
        if (tellerBranchId == null) {
            return BranchAccessResult.TELLER_BRANCH_MISSING;
        }
        if (targetBranchId == null) {
            return BranchAccessResult.TARGET_BRANCH_MISSING;
        }
        if (!tellerBranchId.equals(targetBranchId)) {
            return BranchAccessResult.NOT_SAME_BRANCH;
        }
        return BranchAccessResult.ALLOWED;
    }

    /**
     * Same idea but for an account: the caller resolves the account's
     * owner-customer branch and passes it in. Splitting it out from
     * {@link #tellerCanAccessCustomer} keeps each call site self-documenting.
     */
    public static BranchAccessResult tellerCanAccessAccount(String tellerBranchId, String accountOwnerBranchId) {
        return tellerCanAccessCustomer(tellerBranchId, accountOwnerBranchId);
    }

    /* ============================================================
     * Helpers
     * ============================================================ */

    /** 1..12. UTC keeps tests deterministic across machines. */
    public static int monthOf(long millis) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(millis);
        return c.get(Calendar.MONTH) + 1;
    }

    /** Truncate to the start of a UTC day (00:00:00.000) for "today" comparisons. */
    public static long startOfUtcDay(long millis) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
