package com.example.bankingapp.utils;

import com.example.bankingapp.model.AuditEntry;
import com.example.bankingapp.repository.AuditLogRepository;

/**
 * Thin facade over {@link AuditLogRepository}. ViewModels call
 * {@code log(...)} after any successful CRUD / transaction; the write
 * happens on the IO executor and never blocks the caller.
 *
 * Audit is best-effort: if the insert fails, the originating action is
 * NOT rolled back. SRS F54 says audit must be unmodifiable but does not
 * require it to be transactional with the source operation.
 */
public class AuditLogger {

    /** Common action constants — keep the strings in one place. */
    public static final String ACTION_LOGIN    = "LOGIN";
    public static final String ACTION_LOGOUT   = "LOGOUT";
    public static final String ACTION_CREATE   = "CREATE";
    public static final String ACTION_UPDATE   = "UPDATE";
    public static final String ACTION_DELETE   = "DELETE";
    public static final String ACTION_DEPOSIT  = "DEPOSIT";
    public static final String ACTION_WITHDRAW = "WITHDRAW";
    public static final String ACTION_TRANSFER = "TRANSFER";

    private final AuditLogRepository repo;
    private final AppExecutors executors;

    public AuditLogger(AuditLogRepository repo, AppExecutors executors) {
        this.repo = repo;
        this.executors = executors;
    }

    public void log(Integer userId, String action, String tableAffected,
                    String beforeJson, String afterJson) {
        AuditEntry entry = new AuditEntry(
                0L, userId, action, tableAffected, beforeJson, afterJson,
                null, System.currentTimeMillis());
        executors.io().execute(() -> {
            try {
                repo.save(entry);
            } catch (RuntimeException ignored) {
                // Audit write failed — swallow so it doesn't cascade.
            }
        });
    }

    /** Convenience overload — no before/after detail. */
    public void log(Integer userId, String action, String tableAffected) {
        log(userId, action, tableAffected, null, null);
    }
}
