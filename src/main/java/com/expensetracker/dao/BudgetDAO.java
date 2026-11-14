package com.expensetracker.dao;

import com.expensetracker.exception.InvalidAmountException;
import com.expensetracker.model.Budget;
import com.expensetracker.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetDAO {

    // Insert a new budget, or update the limit if one already exists for that category and month
    public void save(Budget b) throws SQLException, InvalidAmountException {
        if (b.getLimitAmount() == null || b.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Budget limit must be greater than zero");
        }
        String sql = "INSERT INTO budgets (user_id, category_id, month_year, limit_amount) VALUES (?, ?, ?, ?) "
                   + "ON CONFLICT(user_id, category_id, month_year) DO UPDATE SET limit_amount = excluded.limit_amount";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getCategoryId());
            ps.setString(3, b.getMonthYear());
            ps.setBigDecimal(4, b.getLimitAmount());
            ps.executeUpdate();
        }
    }

    public void delete(int budgetId, int userId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE budget_id = ? AND user_id = ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<Budget> findByUserAndMonth(int userId, String monthYear) throws SQLException {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND month_year = ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Budget> find(int userId, int categoryId, String monthYear) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND category_id = ? AND month_year = ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setString(3, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    private Budget map(ResultSet rs) throws SQLException {
        return new Budget(
                rs.getInt("budget_id"),
                rs.getInt("user_id"),
                rs.getInt("category_id"),
                rs.getString("month_year"),
                rs.getBigDecimal("limit_amount"));
    }
}