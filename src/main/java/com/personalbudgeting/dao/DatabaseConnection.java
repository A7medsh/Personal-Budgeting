package com.personalbudgeting.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DATABASE_URL = "jdbc:sqlite:budget.db";
    private static Connection connection;
    
    // Private constructor to prevent instantiation
    private DatabaseConnection() {}
    
    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                // Load the SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                
                // Create a connection to the database
                connection = DriverManager.getConnection(DATABASE_URL);
                
                // Set some database properties for better performance
                connection.setAutoCommit(true);
                
                System.out.println("Database connection established successfully.");
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Check if connection is still valid
            try {
                if (connection.isClosed()) {
                    // Reconnect if connection is closed
                    connection = DriverManager.getConnection(DATABASE_URL);
                    connection.setAutoCommit(true);
                    System.out.println("Database reconnection established successfully.");
                }
            } catch (SQLException e) {
                System.err.println("Database reconnection error: " + e.getMessage());
                e.printStackTrace();
                
                // Try to create a new connection
                try {
                    connection = DriverManager.getConnection(DATABASE_URL);
                    connection.setAutoCommit(true);
                    System.out.println("New database connection established successfully.");
                } catch (SQLException ex) {
                    System.err.println("Failed to establish new connection: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        return connection;
    }
    
    // This method should only be called when the application is shutting down
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 