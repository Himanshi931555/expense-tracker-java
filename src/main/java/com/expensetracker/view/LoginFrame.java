package com.expensetracker.view;

import com.expensetracker.dao.UserDAO;
import com.expensetracker.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Expense Tracker - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 340);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 5, 8, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        JLabel title = new JLabel("Expense Tracker", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        g.gridy = 0; g.gridwidth = 2;
        panel.add(title, g);

        g.gridwidth = 1;
        g.gridy = 1; panel.add(new JLabel("Email"), g);
        g.gridx = 1; panel.add(emailField, g);

        g.gridx = 0; g.gridy = 2; panel.add(new JLabel("Password"), g);
        g.gridx = 1; panel.add(passwordField, g);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Create new account");
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        panel.add(loginBtn, g);
        g.gridy = 4;
        panel.add(registerBtn, g);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        add(panel);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Optional<User> user = userDAO.login(email, password);
            if (user.isPresent()) {
                new DashboardFrame(user.get()).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password.",
                        "Login failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}