package com.example.bankingapp.repository;

/**
 * App-scoped singleton holder for the JDBC-backed repositories.
 * Seed data is loaded by {@code db/seed.sql} + {@code db/v2-teller-seed.sql}.
 * Tests that want fakes call {@link #replaceForTests}.
 */
public final class RepositoryProvider {

    private static UserRepository userRepository;
    private static AccountRepository accountRepository;
    private static TransactionRepository transactionRepository;
    private static BranchRepository branchRepository;
    private static TellerRepository tellerRepository;
    private static AuditLogRepository auditLogRepository;

    private RepositoryProvider() { }

    public static synchronized void init() {
        if (userRepository != null) return;
        userRepository = new JdbcUserRepository();
        accountRepository = new JdbcAccountRepository();
        transactionRepository = new JdbcTransactionRepository();
        branchRepository = new JdbcBranchRepository();
        tellerRepository = new JdbcTellerRepository();
        auditLogRepository = new JdbcAuditLogRepository();
    }

    public static synchronized void replaceForTests(UserRepository u,
                                                    AccountRepository a,
                                                    TransactionRepository t,
                                                    BranchRepository b,
                                                    TellerRepository tl,
                                                    AuditLogRepository al) {
        userRepository = u;
        accountRepository = a;
        transactionRepository = t;
        branchRepository = b;
        tellerRepository = tl;
        auditLogRepository = al;
    }

    public static UserRepository users()           { ensureInit(); return userRepository; }
    public static AccountRepository accounts()     { ensureInit(); return accountRepository; }
    public static TransactionRepository transactions() { ensureInit(); return transactionRepository; }
    public static BranchRepository branches()      { ensureInit(); return branchRepository; }
    public static TellerRepository tellers()       { ensureInit(); return tellerRepository; }
    public static AuditLogRepository auditLog()    { ensureInit(); return auditLogRepository; }

    private static void ensureInit() {
        if (userRepository == null) init();
    }
}
