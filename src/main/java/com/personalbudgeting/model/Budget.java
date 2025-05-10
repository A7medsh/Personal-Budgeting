package com.personalbudgeting.model;

import java.time.YearMonth;

public class Budget {
    private int id;
    private int userId;
    private String category;
    private double amount;
    private YearMonth period; // Budget period (year-month)
    
    // Non-persisted fields used for display
    private double spentAmount;
    private double remainingAmount;
    private double percentage;
    
    // Constructors
    public Budget() {
    }
    
    public Budget(int userId, String category, double amount, YearMonth period) {
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.period = period;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public YearMonth getPeriod() {
        return period;
    }
    
    public void setPeriod(YearMonth period) {
        this.period = period;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
    
    @Override
    public String toString() {
        return "Budget{id=" + id + ", category='" + category + "', amount=" + amount + ", period=" + period + "}";
    }
} 