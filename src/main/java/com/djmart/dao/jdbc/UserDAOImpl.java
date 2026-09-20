package com.djmart.dao.jdbc;

import com.djmart.dao.BaseDAO;
import com.djmart.dao.UserDAO;
import com.djmart.exception.DatabaseException;
import com.djmart.model.Role;
import com.djmart.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDAO using HikariCP and PreparedStatements.
 */
public class UserDAOImpl extends BaseDAO implements UserDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAOImpl.class);

    private static final String SQL_FIND_BY_ID =
            "SELECT id, name, email, password_hash, role, created_at FROM users WHERE id = ?";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT id, name, email, password_hash, role, created_at FROM users WHERE email = ?";

    private static final String SQL_INSERT =
            "INSERT INTO users (name, email, password_hash, role, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

    private static final String SQL_UPDATE =
            "UPDATE users SET name = ?, role = ? WHERE id = ?";

    private static final String SQL_UPDATE_PASSWORD =
            "UPDATE users SET password_hash = ? WHERE id = ?";

    private static final String SQL_FIND_ALL =
            "SELECT id, name, email, password_hash, role, created_at FROM users ORDER BY id ASC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM users";

    private static final String SQL_DELETE =
            "DELETE FROM users WHERE id = ?";

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error("Failed to find user by id {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Failed to find user by ID", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error("Failed to find user by email {}: {}", email, e.getMessage(), e);
            throw new DatabaseException("Failed to find user by email", e);
        }
    }

    @Override
    public User create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole() != null ? user.getRole().name() : Role.BUYER.name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }
            // Fetch populated timestamp
            return findById(user.getId()).orElse(user);
        } catch (SQLException e) {
            LOGGER.error("Failed to create user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new DatabaseException("Failed to create user", e);
        }
    }

    @Override
    public boolean update(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getRole().name());
            ps.setLong(3, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update user {}: {}", user.getId(), e.getMessage(), e);
            throw new DatabaseException("Failed to update user", e);
        }
    }

    @Override
    public boolean updatePassword(Long userId, String newPasswordHash) {
        if (userId == null || newPasswordHash == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PASSWORD)) {
            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update password for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseException("Failed to update user password", e);
        }
    }

    @Override
    public List<User> findAll(int offset, int limit) {
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL)) {
            ps.setInt(1, Math.max(1, limit));
            ps.setInt(2, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
            return users;
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch all users: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve users", e);
        }
    }

    @Override
    public long count() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to count users: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to count users", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to delete user {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Failed to delete user", e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
