package com.personalbudgeting.dao;

import com.personalbudgeting.model.Expense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDAO {
    private Connection connection;
    
    public ExpenseDAO() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    public boolean add(Expense expense) {
        String sql = "INSERT INTO expense (user_id, category, amount, date, description) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, expense.getUserId());
            stmt.setString(2, expense.getCategory());
            stmt.setDouble(3, expense.getAmount());
            stmt.setString(4, expense.getDate().toString());
            stmt.setString(5, expense.getDescription());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        expense.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding expense: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean update(Expense expense) {
        String sql = "UPDATE expense SET category = ?, amount = ?, date = ?, description = ? WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, expense.getCategory());
            stmt.setDouble(2, expense.getAmount());
            stmt.setString(3, expense.getDate().toString());
            stmt.setString(4, expense.getDescription());
            stmt.setInt(5, expense.getId());
            stmt.setInt(6, expense.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating expense: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean delete(int expenseId, int userId) {
        String sql = "DELETE FROM expense WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, expenseId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting expense: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public Expense findById(int expenseId, int userId) {
        String sql = "SELECT * FROM expense WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, expenseId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractExpenseFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding expense by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Expense> findAllByUserId(int userId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expense WHERE user_id = ? ORDER BY date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = extractExpenseFromResultSet(rs);
                    expenses.add(expense);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all expenses by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return expenses;
    }
    
    public List<Expense> findByPeriod(int userId, YearMonth period) {
        List<Expense> expenses = new ArrayList<>();
        
        // Calculate start and end dates for the month
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.atEndOfMonth();
        
        String sql = "SELECT * FROM expense WHERE user_id = ? AND date >= ? AND date <= ? ORDER BY date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, startDate.toString());
            stmt.setString(3, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = extractExpenseFromResultSet(rs);
                    expenses.add(expense);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding expenses by period: " + e.getMessage());
            e.printStackTrace();
        }
        
        return expenses;
    }
    
    public List<Expense> findByCategory(int userId, String category, YearMonth period) {
        List<Expense> expenses = new ArrayList<>();
        
        // Calculate start and end dates for the month
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.atEndOfMonth();
        
        String sql = "SELECT * FROM expense WHERE user_id = ? AND category = ? AND date >= ? AND date <= ? ORDER BY date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, category);
            stmt.setString(3, startDate.toString());
            stmt.setString(4, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = extractExpenseFromResultSet(rs);
                    expenses.add(expense);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding expenses by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return expenses;
    }
    
    public double getTotalExpenseByPeriod(int userId, YearMonth period) {
        // Calculate start and end dates for the month
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.atEndOfMonth();
        
        String sql = "SELECT SUM(amount) AS total FROM expense WHERE user_id = ? AND date >= ? AND date <= ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, startDate.toString());
            stmt.setString(3, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total expense by period: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    public Map<String, Double> getExpensesByCategory(int userId, YearMonth period) {
        Map<String, Double> categoryExpenses = new HashMap<>();
        
        // Calculate start and end dates for the month
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.atEndOfMonth();
        
        String sql = "SELECT category, SUM(amount) AS total FROM expense WHERE user_id = ? AND date >= ? AND date <= ? GROUP BY category";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, startDate.toString());
            stmt.setString(3, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    double amount = rs.getDouble("total");
                    categoryExpenses.put(category, amount);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating expenses by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categoryExpenses;
    }
    
    private Expense extractExpenseFromResultSet(ResultSet rs) throws SQLException {
        Expense expense = new Expense();
        expense.setId(rs.getInt("id"));
        expense.setUserId(rs.getInt("user_id"));
        expense.setCategory(rs.getString("category"));
        expense.setAmount(rs.getDouble("amount"));
        expense.setDate(LocalDate.parse(rs.getString("date")));
        expense.setDescription(rs.getString("description"));
        return expense;
    }
} 