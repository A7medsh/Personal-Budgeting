package com.personalbudgeting.model;

import java.time.LocalDate;

public class Income {
    private int id;
    private int userId;
    private String source;
    private double amount;
    private LocalDate date;
    private String description;
    
    // Constructors
    public Income() {
    }
    
    public Income(int userId, String source, double amount, LocalDate date, String description) {
        this.userId = userId;
        this.source = source;
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
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
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
        return "Income{id=" + id + ", source='" + source + "', amount=" + amount + ", date=" + date + "}";
    }
} 