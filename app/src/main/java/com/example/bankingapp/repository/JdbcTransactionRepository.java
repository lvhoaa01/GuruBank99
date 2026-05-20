package com.example.bankingapp.repository;

import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.model.enums.TransactionStatus;
import com.example.bankingapp.model.enums.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcTransactionRepository implements TransactionRepository {

    private static final String SELECT_ALL =
            "SELECT transaction_id, source_account, destination_account, amount, fee, txn_type, " +
                    "description, timestamp_ms, performed_by_user_id, status, balance_after " +
                    "FROM transactions";

    @Override
    public Transaction save(Transaction t) {
        String sql =
                "INSERT INTO transactions (source_account, destination_account, amount, fee, txn_type, " +
                "                          description, timestamp_ms, performed_by_user_id, status, balance_after) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?) RETURNING transaction_id";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setNullable(ps, 1, t.getSourceAccount());
            setNullable(ps, 2, t.getDestinationAccount());
            ps.setLong(3, t.getAmount());
            ps.setLong(4, t.getFee());
            ps.setString(5, t.getType().name());
            ps.setString(6, t.getDescription());
            ps.setLong(7, t.getTimestampMillis());
            ps.setInt(8, t.getPerformedByUserId());
            ps.setString(9, t.getStatus().name());
            ps.setLong(10, t.getBalanceAfter());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long assignedId = rs.getLong(1);
                return new Transaction(
                        assignedId,
                        t.getSourceAccount(),
                        t.getDestinationAccount(),
                        t.getAmount(),
                        t.getFee(),
                        t.getType(),
                        t.getDescription(),
                        t.getTimestampMillis(),
                        t.getPerformedByUserId(),
                        t.getStatus(),
                        t.getBalanceAfter());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction", e);
        }
    }

    @Override
    public List<Transaction> findByAccount(String accountNumber) {
        String sql = SELECT_ALL +
                " WHERE source_account = ? OR destination_account = ? " +
                "ORDER BY timestamp_ms DESC";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, accountNumber);
            return readAll(ps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Transaction> findLastN(String accountNumber, int n) {
        String sql = SELECT_ALL +
                " WHERE source_account = ? OR destination_account = ? " +
                "ORDER BY timestamp_ms DESC LIMIT ?";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, accountNumber);
            ps.setInt(3, n);
            return readAll(ps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Transaction> findCustomized(String accountNumber,
                                             long fromMillisInclusive,
                                             long toMillisInclusive,
                                             long amountLowerLimit,
                                             int maxCount) {
        StringBuilder sql = new StringBuilder(SELECT_ALL);
        sql.append(" WHERE (source_account = ? OR destination_account = ?) ")
           .append("AND timestamp_ms BETWEEN ? AND ? ")
           .append("AND amount >= ? ")
           .append("ORDER BY timestamp_ms DESC");
        if (maxCount > 0) sql.append(" LIMIT ?");
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, accountNumber);
            ps.setString(2, accountNumber);
            ps.setLong(3, fromMillisInclusive);
            ps.setLong(4, toMillisInclusive);
            ps.setLong(5, amountLowerLimit);
            if (maxCount > 0) ps.setInt(6, maxCount);
            return readAll(ps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long sumTransfersOut(String accountNumber,
                                 long fromMillisInclusive,
                                 long toMillisInclusive) {
        String sql =
                "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE txn_type = 'TRANSFER' AND source_account = ? " +
                "AND timestamp_ms BETWEEN ? AND ?";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setLong(2, fromMillisInclusive);
            ps.setLong(3, toMillisInclusive);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Transaction> readAll(PreparedStatement ps) throws SQLException {
        List<Transaction> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return Collections.unmodifiableList(out);
    }

    private static Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getLong("transaction_id"),
                rs.getString("source_account"),
                rs.getString("destination_account"),
                rs.getLong("amount"),
                rs.getLong("fee"),
                TransactionType.valueOf(rs.getString("txn_type")),
                rs.getString("description"),
                rs.getLong("timestamp_ms"),
                rs.getInt("performed_by_user_id"),
                TransactionStatus.valueOf(rs.getString("status")),
                rs.getLong("balance_after"));
    }

    private static void setNullable(PreparedStatement ps, int idx, String value) throws SQLException {
        if (value == null) ps.setNull(idx, Types.VARCHAR);
        else               ps.setString(idx, value);
    }
}
