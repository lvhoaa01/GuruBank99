package com.example.bankingapp.repository;

import com.example.bankingapp.model.Branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcBranchRepository implements BranchRepository {

    private static final String SELECT_ALL =
            "SELECT branch_id, branch_name, address, phone FROM branches";

    @Override
    public Branch findById(String branchId) {
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL + " WHERE branch_id = ?")) {
            ps.setString(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Branch> findAll() {
        List<Branch> out = new ArrayList<>();
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL + " ORDER BY branch_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public void save(Branch b) {
        String sql = "INSERT INTO branches (branch_id, branch_name, address, phone) " +
                "VALUES (?,?,?,?) " +
                "ON CONFLICT (branch_id) DO UPDATE SET " +
                "  branch_name = EXCLUDED.branch_name, " +
                "  address     = EXCLUDED.address, " +
                "  phone       = EXCLUDED.phone";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getBranchId());
            ps.setString(2, b.getBranchName());
            ps.setString(3, b.getAddress());
            ps.setString(4, b.getPhone());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save branch " + b.getBranchId(), e);
        }
    }

    private static Branch map(ResultSet rs) throws SQLException {
        return new Branch(
                rs.getString("branch_id"),
                rs.getString("branch_name"),
                rs.getString("address"),
                rs.getString("phone"));
    }
}
