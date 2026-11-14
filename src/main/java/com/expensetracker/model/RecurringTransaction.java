package com.expensetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringTransaction {
    private int recId;
    private int userId;
    private int categoryId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private String frequency;      // MONTHLY or WEEKLY
    private LocalDate nextDate;

    public RecurringTransaction() { }

    public RecurringTransaction(int recId, int userId, int categoryId, BigDecimal amount,
                                TransactionType type, String description,
                                String frequency, LocalDate nextDate) {
        this.recId = recId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.frequency = frequency;
        this.nextDate = nextDate;
    }

    public int getRecId() { return recId; }
    public void setRecId(int recId) { this.recId = recId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public LocalDate getNextDate() { return nextDate; }
    public void setNextDate(LocalDate nextDate) { this.nextDate = nextDate; }
}