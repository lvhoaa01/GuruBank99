package com.example.bankingapp.repository;

import com.example.bankingapp.model.AuditEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcAuditLogRepository implements AuditLogRepository {

    private static final String SELECT_ALL =
            "SELECT log_id, user_id, action, table_affected, before_json, after_json, " +
                    "ip_address, timestamp_ms FROM audit_logs";

    @Override
    public AuditEntry save(AuditEntry e) {
        String sql = "INSERT INTO audit_logs (user_id, action, table_affected, " +
                "                              before_json, after_json, ip_address, timestamp_ms) " +
                "VALUES (?,?,?,?,?,?,?) RETURNING log_id";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (e.getUserId() == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, e.getUserId());
            ps.setString(2, e.getAction());
            setNullable(ps, 3, e.getTableAffected());
            setNullable(ps, 4, e.getBeforeJson());
            setNullable(ps, 5, e.getAfterJson());
            setNullable(ps, 6, e.getIpAddress());
            ps.setLong(7, e.getTimestampMillis());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long id = rs.getLong(1);
                return new AuditEntry(id, e.getUserId(), e.getAction(), e.getTableAffected(),
                        e.getBeforeJson(), e.getAfterJson(), e.getIpAddress(), e.getTimestampMillis());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save audit entry", ex);
        }
    }

    @Override
    public List<AuditEntry> findByUser(int userId, int limit) {
        String sql = SELECT_ALL +
                " WHERE user_id = ? ORDER BY timestamp_ms DESC LIMIT ?";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            return readAll(ps);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<AuditEntry> findRecent(int limit) {
        String sql = SELECT_ALL + " ORDER BY timestamp_ms DESC LIMIT ?";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            return readAll(ps);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<AuditEntry> readAll(PreparedStatement ps) throws SQLException {
        List<AuditEntry> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return Collections.unmodifiableList(out);
    }

    private static AuditEntry map(ResultSet rs) throws SQLException {
        Integer userId = rs.getInt("user_id");
        if (rs.wasNull()) userId = null;
        return new AuditEntry(
                rs.getLong("log_id"),
                userId,
                rs.getString("action"),
                rs.getString("table_affected"),
                rs.getString("before_json"),
                rs.getString("after_json"),
                rs.getString("ip_address"),
                rs.getLong("timestamp_ms"));
    }

    private static void setNullable(PreparedStatement ps, int idx, String value) throws SQLException {
        if (value == null) ps.setNull(idx, Types.VARCHAR);
        else               ps.setString(idx, value);
    }
}
