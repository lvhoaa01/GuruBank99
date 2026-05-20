package com.example.bankingapp.repository;

import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.UserRole;
import com.example.bankingapp.model.enums.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcUserRepository implements UserRepository {

    private static final String SELECT_ALL =
            "SELECT customer_id, username, password_hash, full_name, id_number, " +
                    "date_of_birth, gender, address, city, state, pin, phone, email, " +
                    "daily_limit, status, failed_login_count, locked_until_ms, " +
                    "role, branch_id FROM users";

    @Override
    public User findById(int customerId) {
        return queryOne(SELECT_ALL + " WHERE customer_id = ?", ps -> ps.setInt(1, customerId));
    }

    @Override
    public User findByUsername(String username) {
        return queryOne(SELECT_ALL + " WHERE username = ?", ps -> ps.setString(1, username));
    }

    @Override
    public User findByEmail(String email) {
        return queryOne(SELECT_ALL + " WHERE LOWER(email) = LOWER(?)",
                ps -> ps.setString(1, email));
    }

    @Override
    public User findByIdNumber(String idNumber) {
        return queryOne(SELECT_ALL + " WHERE id_number = ?", ps -> ps.setString(1, idNumber));
    }

    @Override
    public void save(User user) {
        // Upsert keyed by customer_id. PostgreSQL ON CONFLICT keeps the call site simple.
        String sql =
                "INSERT INTO users (customer_id, username, password_hash, full_name, id_number, " +
                "                   date_of_birth, gender, address, city, state, pin, phone, email, " +
                "                   daily_limit, status, failed_login_count, locked_until_ms, " +
                "                   role, branch_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (customer_id) DO UPDATE SET " +
                "  username           = EXCLUDED.username, " +
                "  password_hash      = EXCLUDED.password_hash, " +
                "  full_name          = EXCLUDED.full_name, " +
                "  id_number          = EXCLUDED.id_number, " +
                "  date_of_birth      = EXCLUDED.date_of_birth, " +
                "  gender             = EXCLUDED.gender, " +
                "  address            = EXCLUDED.address, " +
                "  city               = EXCLUDED.city, " +
                "  state              = EXCLUDED.state, " +
                "  pin                = EXCLUDED.pin, " +
                "  phone              = EXCLUDED.phone, " +
                "  email              = EXCLUDED.email, " +
                "  daily_limit        = EXCLUDED.daily_limit, " +
                "  status             = EXCLUDED.status, " +
                "  failed_login_count = EXCLUDED.failed_login_count, " +
                "  locked_until_ms    = EXCLUDED.locked_until_ms, " +
                "  role               = EXCLUDED.role, " +
                "  branch_id          = EXCLUDED.branch_id";
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, user.getCustomerId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getIdNumber());
            ps.setString(6, user.getDateOfBirth());
            ps.setString(7, user.getGender());
            ps.setString(8, user.getAddress());
            ps.setString(9, user.getCity());
            ps.setString(10, user.getState());
            ps.setString(11, user.getPin());
            ps.setString(12, user.getPhone());
            ps.setString(13, user.getEmail());
            ps.setLong(14, user.getDailyLimit());
            ps.setString(15, user.getStatus().name());
            ps.setInt(16, user.getFailedLoginCount());
            ps.setLong(17, user.getLockedUntilMillis());
            ps.setString(18, user.getRole() == null ? "CUSTOMER" : user.getRole().name());
            ps.setString(19, user.getBranchId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user " + user.getCustomerId(), e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> out = new ArrayList<>();
        try (Connection c = JdbcConnectionProvider.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    private User queryOne(String sql, Binder binder) {
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

    private static User map(ResultSet rs) throws SQLException {
        User u = new User(rs.getInt("customer_id"),
                rs.getString("username"),
                rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setIdNumber(rs.getString("id_number"));
        u.setDateOfBirth(rs.getString("date_of_birth"));
        u.setGender(rs.getString("gender"));
        u.setAddress(rs.getString("address"));
        u.setCity(rs.getString("city"));
        u.setState(rs.getString("state"));
        u.setPin(rs.getString("pin"));
        u.setPhone(rs.getString("phone"));
        u.setEmail(rs.getString("email"));
        u.setDailyLimit(rs.getLong("daily_limit"));
        u.setStatus(UserStatus.valueOf(rs.getString("status")));
        u.setFailedLoginCount(rs.getInt("failed_login_count"));
        u.setLockedUntilMillis(rs.getLong("locked_until_ms"));
        String role = rs.getString("role");
        u.setRole(role == null ? UserRole.CUSTOMER : UserRole.valueOf(role));
        u.setBranchId(rs.getString("branch_id"));
        return u;
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
