package com.example.bankingapp.model;

/**
 * Teller employee (NhanVien per SRS 3.15). Joined 1-1 with a {@link User}
 * whose role is {@code TELLER}. Belongs to exactly one {@link Branch}.
 */
public class Teller {

    private final int tellerId;     // MaNV
    private final int userId;       // FK → users.customer_id (same ID space)
    private String fullName;
    private String branchId;

    public Teller(int tellerId, int userId, String fullName, String branchId) {
        this.tellerId = tellerId;
        this.userId = userId;
        this.fullName = fullName;
        this.branchId = branchId;
    }

    public int getTellerId() { return tellerId; }
    public int getUserId() { return userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
}
