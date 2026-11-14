package com.expensetracker;

import com.expensetracker.util.DBConnection;
import com.expensetracker.view.LoginFrame;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.sql.SQLException;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            DBConnection.initializeDatabase();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            return;
        }
        // Indigo accent color and rounded corners
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", "#4F46E5",
                "Button.arc", "12",
                "Component.arc", "12",
                "TextComponent.arc", "12"));
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}