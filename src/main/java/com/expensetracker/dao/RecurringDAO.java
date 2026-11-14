package com.expensetracker.dao;

import com.expensetracker.exception.InvalidAmountException;
import com.expensetracker.model.RecurringTransaction;
import com.expensetracker.model.TransactionType;
import com.expensetracker.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecurringDAO {

    public void add(RecurringTransaction r) throws SQLException, InvalidAmountException {
        if (r.getAmount() == null || r.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        String sql = "INSERT INTO recurring (user_id, category_id, amount, type, description, frequency, next_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getCategoryId());
            ps.setBigDecimal(3, r.getAmount());
            ps.setString(4, r.getType().name());
            ps.setString(5, r.getDescription());
            ps.setString(6, r.getFrequency());
            ps.setString(7, r.getNextDate().toString());
            ps.executeUpdate();
        }
    }

    public void delete(int recId, int userId) throws SQLException {
        String sql = "DELETE FROM recurring WHERE rec_id = ? AND user_id = ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, recId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateNextDate(int recId, LocalDate next) throws SQLException {
        String sql = "UPDATE recurring SET next_date = ? WHERE rec_id = ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, next.toString());
            ps.setInt(2, recId);
            ps.executeUpdate();
        }
    }

    public List<RecurringTransaction> findByUser(int userId) throws SQLException {
        List<RecurringTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring WHERE user_id = ? ORDER BY next_date";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private RecurringTransaction map(ResultSet rs) throws SQLException {
        return new RecurringTransaction(
                rs.getInt("rec_id"),
                rs.getInt("user_id"),
                rs.getInt("category_id"),
                rs.getBigDecimal("amount"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("description"),
                rs.getString("frequency"),
                LocalDate.parse(rs.getString("next_date")));
    }
}