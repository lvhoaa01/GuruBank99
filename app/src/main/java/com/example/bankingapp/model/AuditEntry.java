package com.example.bankingapp.model;

/**
 * Audit log row (per SRS F54 and section 3.15 AuditLog table).
 * Immutable once written.
 */
public class AuditEntry {

    private final long logId;            // 0 when unsaved; DB assigns via BIGSERIAL
    private final Integer userId;        // nullable for system events
    private final String action;         // CREATE | UPDATE | DELETE | LOGIN | DEPOSIT | ...
    private final String tableAffected;
    private final String beforeJson;
    private final String afterJson;
    private final String ipAddress;
    private final long timestampMillis;

    public AuditEntry(long logId, Integer userId, String action, String tableAffected,
                      String beforeJson, String afterJson, String ipAddress, long timestampMillis) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.tableAffected = tableAffected;
        this.beforeJson = beforeJson;
        this.afterJson = afterJson;
        this.ipAddress = ipAddress;
        this.timestampMillis = timestampMillis;
    }

    public long getLogId() { return logId; }
    public Integer getUserId() { return userId; }
    public String getAction() { return action; }
    public String getTableAffected() { return tableAffected; }
    public String getBeforeJson() { return beforeJson; }
    public String getAfterJson() { return afterJson; }
    public String getIpAddress() { return ipAddress; }
    public long getTimestampMillis() { return timestampMillis; }
}
