package com.example.bankingapp.repository;

import com.example.bankingapp.model.AuditEntry;

import java.util.List;

public interface AuditLogRepository {

    /** Persist an audit entry; returns the saved instance with a real {@code logId}. */
    AuditEntry save(AuditEntry entry);

    /** Most recent {@code limit} entries for one user, newest first. */
    List<AuditEntry> findByUser(int userId, int limit);

    /** Most recent {@code limit} entries overall, newest first. */
    List<AuditEntry> findRecent(int limit);
}
