package com.personalbudgeting.dao;

import com.personalbudgeting.model.Income;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class IncomeDAO {
    private Connection connection;
    
    public IncomeDAO() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    public boolean add(Income income) {
        String sql = "INSERT INTO income (user_id, source, amount, date, description) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, income.getUserId());
            stmt.setString(2, income.getSource());
            stmt.setDouble(3, income.getAmount());
            stmt.setString(4, income.getDate().toString());
            stmt.setString(5, income.getDescription());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        income.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding income: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean update(Income income) {
        String sql = "UPDATE income SET source = ?, amount = ?, date = ?, description = ? WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, income.getSource());
            stmt.setDouble(2, income.getAmount());
            stmt.setString(3, income.getDate().toString());
            stmt.setString(4, income.getDescription());
            stmt.setInt(5, income.getId());
            stmt.setInt(6, income.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating income: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean delete(int incomeId, int userId) {
        String sql = "DELETE FROM income WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incomeId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting income: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public Income findById(int incomeId, int userId) {
        String sql = "SELECT * FROM income WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incomeId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractIncomeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding income by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Income> findAllByUserId(int userId) {
        List<Income> incomes = new ArrayList<>();
        String sql = "SELECT * FROM income WHERE user_id = ? ORDER BY date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Income income = extractIncomeFromResultSet(rs);
                    incomes.add(income);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all incomes by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return incomes;
    }
    
    public List<Income> findByPeriod(int userId, YearMonth period) {
        List<Income> incomes = new ArrayList<>();
        
        // Calculate start and end dates for the month
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.atEndOfMonth();
        
        String sql = "SELECT * FROM income WHERE user_id = ? AND date >= ? AND date <= ? ORDER BY date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, startDate.toString());
            stmt.setString(3, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Income income = extractIncomeFromResultSet(rs);
                    incomes.add(income);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding incomes by period: " + e.getMessage());
            e.printStackTrace();
        }
        
        return incomes;
    }
    
    public double getTotalIncomeByPeriod(int userId, YearMonth period) {
        // Calculate start and end dates for the month
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.atEndOfMonth();
        
        String sql = "SELECT SUM(amount) AS total FROM income WHERE user_id = ? AND date >= ? AND date <= ?";
        
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
            System.err.println("Error calculating total income by period: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    private Income extractIncomeFromResultSet(ResultSet rs) throws SQLException {
        Income income = new Income();
        income.setId(rs.getInt("id"));
        income.setUserId(rs.getInt("user_id"));
        income.setSource(rs.getString("source"));
        income.setAmount(rs.getDouble("amount"));
        income.setDate(LocalDate.parse(rs.getString("date")));
        income.setDescription(rs.getString("description"));
        return income;
    }
} 