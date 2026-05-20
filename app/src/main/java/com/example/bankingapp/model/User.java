package com.example.bankingapp.model;

import com.example.bankingapp.model.enums.UserRole;
import com.example.bankingapp.model.enums.UserStatus;

/**
 * A user of Bank99 — either a Customer (default) or a Teller.
 * Represents the credentials (Username, PasswordHash) and the
 * customer profile fields the SRS lists for New / Edit Customer.
 *
 * Tellers reuse this row for login; their employee details live in
 * the {@code tellers} table joined by {@code userId}.
 */
public class User {

    private final int customerId;            // MaKH (customer) or 1..N (teller)
    private final String username;           // "C" + MaKH or "T" + MaNV
    private String passwordHash;
    private String fullName;                 // HoTen
    private String idNumber;                 // CMND/CCCD
    private String dateOfBirth;              // DD/MM/YYYY
    private String gender;                   // "Nam"/"Nữ"
    private String address;
    private String city;
    private String state;
    private String pin;                      // 6 digits
    private String phone;
    private String email;
    private long dailyLimit;                 // HanMucNgay, VND
    private UserStatus status;
    private int failedLoginCount;
    private long lockedUntilMillis;          // 0 if not locked
    private UserRole role;
    private String branchId;                 // MaChiNhanh (e.g. "CN001"); customers + tellers both belong to one

    public User(int customerId, String username, String passwordHash) {
        this.customerId = customerId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE;
        this.failedLoginCount = 0;
        this.lockedUntilMillis = 0L;
        this.dailyLimit = 100_000_000L;
        this.role = UserRole.CUSTOMER;
    }

    public int getCustomerId() { return customerId; }
    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(long dailyLimit) { this.dailyLimit = dailyLimit; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public int getFailedLoginCount() { return failedLoginCount; }
    public void setFailedLoginCount(int failedLoginCount) { this.failedLoginCount = failedLoginCount; }

    public long getLockedUntilMillis() { return lockedUntilMillis; }
    public void setLockedUntilMillis(long lockedUntilMillis) { this.lockedUntilMillis = lockedUntilMillis; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
}
