package com.expensetracker;

import com.expensetracker.util.DBConnection;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            DBConnection.initializeDatabase();
            System.out.println("Database connected and tables created successfully!");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}