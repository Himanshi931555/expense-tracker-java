package com.expensetracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String URL = "jdbc:sqlite:expense_tracker.db";
    private static Connection connection;

    private DBConnection() { }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public static void initializeDatabase() throws SQLException {
        try (Statement st = getConnection().createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    UNIQUE (user_id, name, type)
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    txn_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
                    type TEXT NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
                    txn_date TEXT NOT NULL,
                    description TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (category_id) REFERENCES categories(category_id)
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    budget_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    month_year TEXT NOT NULL,
                    limit_amount DECIMAL(10,2) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (category_id) REFERENCES categories(category_id),
                    UNIQUE (user_id, category_id, month_year)
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS recurring (
                    rec_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
                    type TEXT NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
                    description TEXT,
                    frequency TEXT NOT NULL CHECK (frequency IN ('MONTHLY','WEEKLY')),
                    next_date TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (category_id) REFERENCES categories(category_id)
                )""");
        }
    }
}