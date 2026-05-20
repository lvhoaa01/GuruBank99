package com.example.bankingapp.repository;

import com.example.bankingapp.model.Teller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcTellerRepository implements TellerRepository {

    private static final String SELECT_ALL =
            "SELECT teller_id, user_id, full_name, branch_id FROM tellers";

    @Override
    public Teller findById(int tellerId) {
        return queryOne(SELECT_ALL + " WHERE teller_id = ?", ps -> ps.setInt(1, tellerId));
    }

    @Override
    public Teller findByUserId(int userId) {
        return queryOne(SELECT_ALL + " WHERE user_id = ?", ps -> ps.setInt(1, userId));
    }

    @Override
    public List<Teller> findByBranch(String branchId) {
        List<Teller> out = new ArrayList<>();
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL + " WHERE branch_id = ?")) {
            ps.setString(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public void save(Teller t) {
        String sql = "INSERT INTO tellers (teller_id, user_id, full_name, branch_id) " +
                "VALUES (?,?,?,?) " +
                "ON CONFLICT (teller_id) DO UPDATE SET " +
                "  user_id   = EXCLUDED.user_id, " +
                "  full_name = EXCLUDED.full_name, " +
                "  branch_id = EXCLUDED.branch_id";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, t.getTellerId());
            ps.setInt(2, t.getUserId());
            ps.setString(3, t.getFullName());
            ps.setString(4, t.getBranchId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save teller " + t.getTellerId(), e);
        }
    }

    @Override
    public int nextTellerId() {
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(MAX(teller_id), 0) + 1 FROM tellers");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Teller queryOne(String sql, Binder binder) {
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Teller map(ResultSet rs) throws SQLException {
        return new Teller(
                rs.getInt("teller_id"),
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("branch_id"));
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
