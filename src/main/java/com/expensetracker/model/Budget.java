package com.expensetracker.model;

import java.math.BigDecimal;

public class Budget {
    private int budgetId;
    private int userId;
    private int categoryId;
    private String monthYear;      // format: 2026-11
    private BigDecimal limitAmount;

    public Budget() { }

    public Budget(int budgetId, int userId, int categoryId, String monthYear, BigDecimal limitAmount) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.monthYear = monthYear;
        this.limitAmount = limitAmount;
    }

    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
}