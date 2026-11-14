package com.expensetracker.service;

import com.expensetracker.dao.BudgetDAO;
import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.TransactionDAO;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BudgetService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int WARN_PERCENT = 80;

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final TransactionDAO txnDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // One row of the budget table
    public record Status(int budgetId, int categoryId, String categoryName,
                         BigDecimal limit, BigDecimal spent) {
        public BigDecimal remaining() {
            return limit.subtract(spent);
        }
        public int percentUsed() {
            return spent.multiply(HUNDRED).divide(limit, 0, RoundingMode.HALF_UP).intValue();
        }
    }

    public List<Status> statusesFor(int userId, YearMonth ym) throws SQLException {
        Map<Integer, String> names = categoryDAO.findByUser(userId).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));
        List<Transaction> txns = txnDAO.findByUser(userId);

        List<Status> result = new ArrayList<>();
        for (Budget b : budgetDAO.findByUserAndMonth(userId, ym.toString())) {
            result.add(new Status(b.getBudgetId(), b.getCategoryId(),
                    names.getOrDefault(b.getCategoryId(), "?"),
                    b.getLimitAmount(), spent(txns, b.getCategoryId(), ym)));
        }
        return result;
    }

    // Call this AFTER an expense is saved. Returns a warning message, or null if all is fine.
    public String checkWarning(int userId, int categoryId, String categoryName, LocalDate date)
            throws SQLException {
        YearMonth ym = YearMonth.from(date);
        Optional<Budget> budget = budgetDAO.find(userId, categoryId, ym.toString());
        if (budget.isEmpty()) return null;

        BigDecimal limit = budget.get().getLimitAmount();
        BigDecimal spent = spent(txnDAO.findByUser(userId), categoryId, ym);
        int percent = spent.multiply(HUNDRED).divide(limit, 0, RoundingMode.HALF_UP).intValue();

        if (spent.compareTo(limit) > 0) {
            return "You are OVER your " + categoryName + " budget for " + ym + "!\n\n"
                    + "Limit: " + money(limit) + "\n"
                    + "Spent: " + money(spent) + "\n"
                    + "Over by: " + money(spent.subtract(limit));
        }
        if (percent >= WARN_PERCENT) {
            return "Careful! You have used " + percent + "% of your " + categoryName
                    + " budget for " + ym + ".\n\n"
                    + "Limit: " + money(limit) + "\n"
                    + "Spent: " + money(spent) + "\n"
                    + "Remaining: " + money(limit.subtract(spent));
        }
        return null;
    }

    private BigDecimal spent(List<Transaction> txns, int categoryId, YearMonth ym) {
        return txns.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategoryId() == categoryId)
                .filter(t -> YearMonth.from(t.getTxnDate()).equals(ym))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String money(BigDecimal v) {
        return "\u20B9 " + String.format("%,.2f", v);
    }
}