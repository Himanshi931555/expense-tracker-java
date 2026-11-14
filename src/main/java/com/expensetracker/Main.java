package com.expensetracker;

import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.TransactionDAO;
import com.expensetracker.dao.UserDAO;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import com.expensetracker.util.DBConnection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try {
            DBConnection.initializeDatabase();

            UserDAO userDAO = new UserDAO();
            CategoryDAO categoryDAO = new CategoryDAO();
            TransactionDAO txnDAO = new TransactionDAO();

            String email = "test@example.com";
            if (userDAO.findByEmail(email).isEmpty()) {
                User u = userDAO.register("Test User", email, "pass123");
                categoryDAO.addDefaults(u.getUserId());
                System.out.println("Registered: " + u);
            }

            Optional<User> logged = userDAO.login(email, "pass123");
            System.out.println("Login OK? " + logged.isPresent());
            System.out.println("Wrong password OK? " + userDAO.login(email, "wrong").isPresent());

            User user = logged.get();
            List<Category> cats = categoryDAO.findByUser(user.getUserId());
            Category food = cats.stream().filter(c -> c.getName().equals("Food")).findFirst().get();

            txnDAO.add(new Transaction(0, user.getUserId(), food.getCategoryId(),
                    new BigDecimal("250.50"), TransactionType.EXPENSE, LocalDate.now(), "Lunch"));

            for (Transaction t : txnDAO.findByUser(user.getUserId())) {
                System.out.println(t.getTxnDate() + " | " + t.getType() + " | " + t.getAmount() + " | " + t.getDescription());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}