package com.expensetracker.dao;

import com.expensetracker.exception.DuplicateUserException;
import com.expensetracker.model.User;
import com.expensetracker.util.DBConnection;
import com.expensetracker.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDAO {

    public User register(String name, String email, String password)
            throws SQLException, DuplicateUserException {
        if (findByEmail(email).isPresent()) {
            throw new DuplicateUserException("Email already registered: " + email);
        }
        String sql = "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String hash = PasswordUtil.hashPassword(password);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : 0;
                return new User(id, name, email, hash);
            }
        }
    }

    public Optional<User> login(String email, String password) throws SQLException {
        Optional<User> user = findByEmail(email);
        if (user.isPresent() && PasswordUtil.verifyPassword(password, user.get().getPasswordHash())) {
            return user;
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, name, email, password_hash FROM users WHERE email = ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash")));
                }
            }
        }
        return Optional.empty();
    }
}