package com.expensetracker.model;

public class Category {
    private int categoryId;
    private int userId;
    private String name;
    private TransactionType type;

    public Category() { }

    public Category(int categoryId, int userId, String name, TransactionType type) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.name = name;
        this.type = type;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    @Override
    public String toString() { return name; }
}