package com.example.bankingapp.model;

/**
 * Bank branch (ChiNhanh per SRS 3.15).
 * Format of {@code branchId}: "CN" + 3 digits (e.g. "CN001").
 */
public class Branch {

    private final String branchId;
    private String branchName;
    private String address;
    private String phone;

    public Branch(String branchId, String branchName, String address, String phone) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.phone = phone;
    }

    public String getBranchId() { return branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
