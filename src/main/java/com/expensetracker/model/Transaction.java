package com.expensetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private int txnId;
    private int userId;
    private int categoryId;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDate txnDate;
    private String description;

    public Transaction() { }

    public Transaction(int txnId, int userId, int categoryId, BigDecimal amount,
                       TransactionType type, LocalDate txnDate, String description) {
        this.txnId = txnId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.txnDate = txnDate;
        this.description = description;
    }

    public int getTxnId() { return txnId; }
    public void setTxnId(int txnId) { this.txnId = txnId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public LocalDate getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDate txnDate) { this.txnDate = txnDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}