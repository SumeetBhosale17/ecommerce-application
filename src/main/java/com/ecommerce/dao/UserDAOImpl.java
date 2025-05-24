package com.ecommerce.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.User;
import com.ecommerce.model.UserRole;
import com.ecommerce.utils.PasswordUtils;

public class UserDAOImpl implements UserDAO {
    @Override
    public User create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, contact, role, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getContact());
                stmt.setString(5, user.getRole().name());
                stmt.setTimestamp(6, now);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        user.setCreatedAt(now);
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
                return user;
            }
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
    }

    @Override
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return extractUserFromResultSet(rs);
                    }
                }
            }
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return extractUserFromResultSet(rs);
                    }
                }
            }
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return users;
    }

    @Override
    public User update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, contact = ?, role = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getContact());
                stmt.setString(5, user.getRole().name());
                stmt.setInt(6, user.getId());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating user failed, no rows affected.");
                }
                return user;
            }
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
    }

    @Override
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = extractUserFromResultSet(rs);
                        if (PasswordUtils.checkPassword(password, user.getPassword())) {
                            return user;
                        }
                    }
                }
            }
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) {
        try {
            return findByUsername(username);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User getUserById(int userId) {
        try {
            return findById(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteUser(int userId) {
        try {
            return delete(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        return update(user) != null;
    }

    @Override
    public List<User> getAdminUsers() throws SQLException {
        List<User> adminUsers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'ADMIN'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setContact(rs.getString("contact"));
                user.setRole(UserRole.ADMIN);
                adminUsers.add(user);
            }
            return adminUsers;
        }
    }

    @Override
    public boolean checkUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setContact(rs.getString("contact"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}

