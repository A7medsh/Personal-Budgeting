package com.personalbudgeting.model;

import java.time.LocalDateTime;

public class Reminder {
    private int id;
    private int userId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private boolean isCompleted;
    
    // Constructors
    public Reminder() {
    }
    
    public Reminder(int userId, String title, String description, LocalDateTime dueDate) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = false;
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    @Override
    public String toString() {
        return "Reminder{id=" + id + ", title='" + title + "', dueDate=" + dueDate + ", completed=" + isCompleted + "}";
    }
} 