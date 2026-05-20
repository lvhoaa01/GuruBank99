package com.example.bankingapp.repository;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.enums.AccountType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcAccountRepository implements AccountRepository {

    private static final String SELECT_ALL =
            "SELECT account_number, owner_customer_id, account_type, balance, active, " +
                    "monthly_withdraw_count, monthly_transfer_count, counters_month FROM accounts";

    @Override
    public Account findByNumber(String accountNumber) {
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL + " WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Account> findByOwner(int customerId) {
        List<Account> out = new ArrayList<>();
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL + " WHERE owner_customer_id = ?")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public void save(Account account) {
        String sql =
                "INSERT INTO accounts (account_number, owner_customer_id, account_type, balance, " +
                "                      active, monthly_withdraw_count, monthly_transfer_count, " +
                "                      counters_month) " +
                "VALUES (?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (account_number) DO UPDATE SET " +
                "  owner_customer_id      = EXCLUDED.owner_customer_id, " +
                "  account_type           = EXCLUDED.account_type, " +
                "  balance                = EXCLUDED.balance, " +
                "  active                 = EXCLUDED.active, " +
                "  monthly_withdraw_count = EXCLUDED.monthly_withdraw_count, " +
                "  monthly_transfer_count = EXCLUDED.monthly_transfer_count, " +
                "  counters_month         = EXCLUDED.counters_month";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, account.getAccountNumber());
            ps.setInt(2, account.getOwnerCustomerId());
            ps.setString(3, account.getType().name());
            ps.setLong(4, account.getBalance());
            ps.setBoolean(5, account.isActive());
            ps.setInt(6, account.getMonthlyWithdrawCount());
            ps.setInt(7, account.getMonthlyTransferCount());
            ps.setInt(8, account.getCountersMonth());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save account " + account.getAccountNumber(), e);
        }
    }

    @Override
    public List<Account> findAll() {
        List<Account> out = new ArrayList<>();
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableList(out);
    }

    private static Account map(ResultSet rs) throws SQLException {
        Account a = new Account(
                rs.getString("account_number"),
                rs.getInt("owner_customer_id"),
                AccountType.valueOf(rs.getString("account_type")),
                rs.getLong("balance"));
        a.setActive(rs.getBoolean("active"));
        a.setMonthlyWithdrawCount(rs.getInt("monthly_withdraw_count"));
        a.setMonthlyTransferCount(rs.getInt("monthly_transfer_count"));
        a.setCountersMonth(rs.getInt("counters_month"));
        return a;
    }
}
