package com.example.bankingapp.model;

import com.example.bankingapp.model.enums.AccountType;

/**
 * A bank account. Balance is stored as a {@code long} of VND (no decimals)
 * to avoid floating-point rounding errors during balance math in tests.
 */
public class Account {

    /** Minimum balance that must remain in a SAVING account after any debit. */
    public static final long MIN_SAVING_BALANCE = 500L;

    /** Monthly cap for SAVING accounts. */
    public static final int MAX_SAVING_WITHDRAWALS_PER_MONTH = 5;
    public static final int MAX_SAVING_TRANSFERS_PER_MONTH = 10;

    private final String accountNumber;     // "99" + 10 digits
    private final int ownerCustomerId;
    private final AccountType type;
    private long balance;
    private boolean active;

    // SRS 3.7: monthly counters used to enforce SAVING limits.
    private int monthlyWithdrawCount;
    private int monthlyTransferCount;
    private int countersMonth;              // 1..12 — month the counters refer to

    public Account(String accountNumber, int ownerCustomerId, AccountType type, long initialBalance) {
        this.accountNumber = accountNumber;
        this.ownerCustomerId = ownerCustomerId;
        this.type = type;
        this.balance = initialBalance;
        this.active = true;
    }

    public String getAccountNumber() { return accountNumber; }
    public int getOwnerCustomerId() { return ownerCustomerId; }
    public AccountType getType() { return type; }

    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getMonthlyWithdrawCount() { return monthlyWithdrawCount; }
    public void setMonthlyWithdrawCount(int v) { this.monthlyWithdrawCount = v; }

    public int getMonthlyTransferCount() { return monthlyTransferCount; }
    public void setMonthlyTransferCount(int v) { this.monthlyTransferCount = v; }

    public int getCountersMonth() { return countersMonth; }
    public void setCountersMonth(int v) { this.countersMonth = v; }
}
