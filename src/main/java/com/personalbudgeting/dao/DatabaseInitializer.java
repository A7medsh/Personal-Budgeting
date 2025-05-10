package com.personalbudgeting.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {
    
    public static void initialize() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Create tables if they don't exist
            createUserTable(connection);
            createIncomeTable(connection);
            createExpenseTable(connection);
            createBudgetTable(connection);
            createReminderTable(connection);
            
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createUserTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "username TEXT NOT NULL UNIQUE, " +
                     "email TEXT NOT NULL UNIQUE, " +
                     "password TEXT NOT NULL, " +
                     "otp_code TEXT, " +
                     "otp_expiry TEXT, " +
                     "verified INTEGER DEFAULT 0" +
                     ")";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    private static void createIncomeTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS income (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id INTEGER NOT NULL, " +
                     "source TEXT NOT NULL, " +
                     "amount REAL NOT NULL, " +
                     "date TEXT NOT NULL, " +
                     "description TEXT, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                     ")";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    private static void createExpenseTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS expense (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id INTEGER NOT NULL, " +
                     "category TEXT NOT NULL, " +
                     "amount REAL NOT NULL, " +
                     "date TEXT NOT NULL, " +
                     "description TEXT, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                     ")";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    private static void createBudgetTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS budget (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id INTEGER NOT NULL, " +
                     "category TEXT NOT NULL, " +
                     "amount REAL NOT NULL, " +
                     "period TEXT NOT NULL, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                     "UNIQUE(user_id, category, period)" +
                     ")";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    private static void createReminderTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS reminder (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id INTEGER NOT NULL, " +
                     "title TEXT NOT NULL, " +
                     "description TEXT, " +
                     "due_date TEXT NOT NULL, " +
                     "is_completed INTEGER DEFAULT 0, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                     ")";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
} 