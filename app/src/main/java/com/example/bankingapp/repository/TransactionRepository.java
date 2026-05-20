package com.example.bankingapp.repository;

import com.example.bankingapp.model.Transaction;

import java.util.List;

public interface TransactionRepository {

    /**
     * Persist a new transaction. The {@code transactionId} on the input is
     * ignored — the database assigns the real id (BIGSERIAL). Returns a
     * fresh {@link Transaction} carrying the assigned id.
     */
    Transaction save(Transaction transaction);

    /** Latest-first list of every transaction that touches the given account. */
    List<Transaction> findByAccount(String accountNumber);

    /** Last {@code n} transactions, newest first. Used by the Mini Statement screen. */
    List<Transaction> findLastN(String accountNumber, int n);

    /** Customised statement: range filter on date + amount lower bound, newest first. */
    List<Transaction> findCustomized(String accountNumber,
                                     long fromMillisInclusive,
                                     long toMillisInclusive,
                                     long amountLowerLimit,
                                     int maxCount);

    /** Sum of TRANSFER amounts moved OUT of the given account in [fromMillis, toMillis]. */
    long sumTransfersOut(String accountNumber, long fromMillisInclusive, long toMillisInclusive);
}
