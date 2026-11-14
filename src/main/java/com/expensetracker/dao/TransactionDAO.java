package com.expensetracker.dao;

import com.expensetracker.exception.InvalidAmountException;
import com.expensetracker.model.Transaction;
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

public class TransactionDAO {

    public void add(Transaction t) throws SQLException, InvalidAmountException {
        validate(t);
        String sql = "INSERT INTO transactions (user_id, category_id, amount, type, txn_date, description) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, t.getUserId());
            ps.setInt(2, t.getCategoryId());
            ps.setBigDecimal(3, t.getAmount());
            ps.setString(4, t.getType().name());
            ps.setString(5, t.getTxnDate().toString());
            ps.setString(6, t.getDescription());
            ps.executeUpdate();
        }
    }

    public void update(Transaction t) throws SQLException, InvalidAmountException {
        validate(t);
        String sql = "UPDATE transactions SET category_id=?, amount=?, type=?, txn_date=?, description=? "
                   + "WHERE txn_id=? AND user_id=?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, t.getCategoryId());
            ps.setBigDecimal(2, t.getAmount());
            ps.setString(3, t.getType().name());
            ps.setString(4, t.getTxnDate().toString());
            ps.setString(5, t.getDescription());
            ps.setInt(6, t.getTxnId());
            ps.setInt(7, t.getUserId());
            ps.executeUpdate();
        }
    }

    public void delete(int txnId, int userId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE txn_id = ? AND user_id = ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, txnId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<Transaction> findByUser(int userId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY txn_date DESC, txn_id DESC";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("txn_id"),
                            rs.getInt("user_id"),
                            rs.getInt("category_id"),
                            rs.getBigDecimal("amount"),
                            TransactionType.valueOf(rs.getString("type")),
                            LocalDate.parse(rs.getString("txn_date")),
                            rs.getString("description")));
                }
            }
        }
        return list;
    }

    private void validate(Transaction t) throws InvalidAmountException {
        if (t.getAmount() == null || t.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        if (t.getTxnDate() == null) {
            throw new InvalidAmountException("Date is required");
        }
    }
}