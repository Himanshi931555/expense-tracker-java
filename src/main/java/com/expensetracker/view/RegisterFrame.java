package com.expensetracker.view;

import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.UserDAO;
import com.expensetracker.exception.DuplicateUserException;
import com.expensetracker.model.User;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public RegisterFrame() {
        setTitle("Expense Tracker - Register");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 5, 8, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        panel.add(title, g);

        g.gridwidth = 1;
        g.gridy = 1; g.gridx = 0; panel.add(new JLabel("Name"), g);
        g.gridx = 1; panel.add(nameField, g);
        g.gridy = 2; g.gridx = 0; panel.add(new JLabel("Email"), g);
        g.gridx = 1; panel.add(emailField, g);
        g.gridy = 3; g.gridx = 0; panel.add(new JLabel("Password"), g);
        g.gridx = 1; panel.add(passwordField, g);
        g.gridy = 4; g.gridx = 0; panel.add(new JLabel("Confirm"), g);
        g.gridx = 1; panel.add(confirmField, g);

        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to login");
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        panel.add(registerBtn, g);
        g.gridy = 6;
        panel.add(backBtn, g);

        registerBtn.addActionListener(e -> doRegister());
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        add(panel);
    }

    private void doRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            warn("All fields are required.");
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            warn("Please enter a valid email address.");
            return;
        }
        if (pass.length() < 6) {
            warn("Password must be at least 6 characters.");
            return;
        }
        if (!pass.equals(confirm)) {
            warn("Passwords do not match.");
            return;
        }
        try {
            User u = userDAO.register(name, email, pass);
            categoryDAO.addDefaults(u.getUserId());
            JOptionPane.showMessageDialog(this, "Account created! Please login.");
            new LoginFrame().setVisible(true);
            dispose();
        } catch (DuplicateUserException ex) {
            warn(ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }
}