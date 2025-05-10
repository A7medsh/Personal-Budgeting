package com.personalbudgeting.model;

import java.time.LocalDate;

public class Expense {
    private int id;
    private int userId;
    private String category;
    private double amount;
    private LocalDate date;
    private String description;
    
    // Constructors
    public Expense() {
    }
    
    public Expense(int userId, String category, double amount, LocalDate date, String description) {
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "Expense{id=" + id + ", category='" + category + "', amount=" + amount + ", date=" + date + "}";
    }
} 