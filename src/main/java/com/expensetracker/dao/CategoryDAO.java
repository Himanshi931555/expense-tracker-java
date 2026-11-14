package com.expensetracker.dao;

import com.expensetracker.model.Category;
import com.expensetracker.model.TransactionType;
import com.expensetracker.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public void add(Category c) throws SQLException {
        String sql = "INSERT INTO categories (user_id, name, type) VALUES (?, ?, ?)";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getUserId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getType().name());
            ps.executeUpdate();
        }
    }

    public void delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.executeUpdate();
        }
    }

    public List<Category> findByUser(int userId) throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, user_id, name, type FROM categories WHERE user_id = ? ORDER BY name";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Category(
                            rs.getInt("category_id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            TransactionType.valueOf(rs.getString("type"))));
                }
            }
        }
        return list;
    }

    // Adds default categories for a newly registered user
    public void addDefaults(int userId) throws SQLException {
        String[] expense = {"Food", "Travel", "Rent", "Bills", "Shopping", "Health", "Entertainment"};
        String[] income = {"Salary", "Freelance", "Gift", "Other Income"};
        for (String n : expense) add(new Category(0, userId, n, TransactionType.EXPENSE));
        for (String n : income) add(new Category(0, userId, n, TransactionType.INCOME));
    }
}