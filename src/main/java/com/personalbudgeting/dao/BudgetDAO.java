package com.personalbudgeting.dao;

import com.personalbudgeting.model.Budget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetDAO {
    private Connection connection;
    
    public BudgetDAO() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    public boolean add(Budget budget) {
        String sql = "INSERT INTO budget (user_id, category, amount, period) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, budget.getUserId());
            stmt.setString(2, budget.getCategory());
            stmt.setDouble(3, budget.getAmount());
            stmt.setString(4, budget.getPeriod().toString());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        budget.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding budget: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean update(Budget budget) {
        String sql = "UPDATE budget SET amount = ? WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, budget.getAmount());
            stmt.setInt(2, budget.getId());
            stmt.setInt(3, budget.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean delete(int budgetId, int userId) {
        String sql = "DELETE FROM budget WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, budgetId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public Budget findById(int budgetId, int userId) {
        String sql = "SELECT * FROM budget WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, budgetId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractBudgetFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding budget by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public Budget findByCategory(int userId, String category, YearMonth period) {
        String sql = "SELECT * FROM budget WHERE user_id = ? AND category = ? AND period = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, category);
            stmt.setString(3, period.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractBudgetFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding budget by category: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Budget> findByPeriod(int userId, YearMonth period) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budget WHERE user_id = ? AND period = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, period.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = extractBudgetFromResultSet(rs);
                    budgets.add(budget);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding budgets by period: " + e.getMessage());
            e.printStackTrace();
        }
        return budgets;
    }
    
    public Map<String, Double> getBudgetsByCategory(int userId, YearMonth period) {
        Map<String, Double> categoryBudgets = new HashMap<>();
        String sql = "SELECT category, amount FROM budget WHERE user_id = ? AND period = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, period.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    double amount = rs.getDouble("amount");
                    categoryBudgets.put(category, amount);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting budgets by category: " + e.getMessage());
            e.printStackTrace();
        }
        return categoryBudgets;
    }
    
    public double getTotalBudget(int userId, YearMonth period) {
        String sql = "SELECT SUM(amount) AS total FROM budget WHERE user_id = ? AND period = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, period.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total budget: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
    
    private Budget extractBudgetFromResultSet(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getInt("id"));
        budget.setUserId(rs.getInt("user_id"));
        budget.setCategory(rs.getString("category"));
        budget.setAmount(rs.getDouble("amount"));
        budget.setPeriod(YearMonth.parse(rs.getString("period")));
        return budget;
    }
} 