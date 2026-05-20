package com.example.bankingapp.model;

import com.example.bankingapp.model.enums.TransactionStatus;
import com.example.bankingapp.model.enums.TransactionType;

/**
 * One financial movement: deposit, withdrawal, or transfer.
 * Both source and destination are nullable to support all three cases:
 *  - DEPOSIT : sourceAccount = null
 *  - WITHDRAW: destinationAccount = null
 *  - TRANSFER: both set
 */
public class Transaction {

    private final long transactionId;       // MaGD
    private final String sourceAccount;     // nullable
    private final String destinationAccount;// nullable
    private final long amount;              // > 0, VND
    private final long fee;                 // currently always 0
    private final TransactionType type;
    private final String description;
    private final long timestampMillis;
    private final int performedByUserId;
    private final TransactionStatus status;
    private final long balanceAfter;        // balance of the *primary* account after the txn

    public Transaction(long transactionId,
                       String sourceAccount,
                       String destinationAccount,
                       long amount,
                       long fee,
                       TransactionType type,
                       String description,
                       long timestampMillis,
                       int performedByUserId,
                       TransactionStatus status,
                       long balanceAfter) {
        this.transactionId = transactionId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.fee = fee;
        this.type = type;
        this.description = description;
        this.timestampMillis = timestampMillis;
        this.performedByUserId = performedByUserId;
        this.status = status;
        this.balanceAfter = balanceAfter;
    }

    public long getTransactionId() { return transactionId; }
    public String getSourceAccount() { return sourceAccount; }
    public String getDestinationAccount() { return destinationAccount; }
    public long getAmount() { return amount; }
    public long getFee() { return fee; }
    public TransactionType getType() { return type; }
    public String getDescription() { return description; }
    public long getTimestampMillis() { return timestampMillis; }
    public int getPerformedByUserId() { return performedByUserId; }
    public TransactionStatus getStatus() { return status; }
    public long getBalanceAfter() { return balanceAfter; }
}
