package com.personalbudgeting.dao;

import com.personalbudgeting.model.Reminder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReminderDAO {
    private Connection connection;
    
    public ReminderDAO() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    public boolean add(Reminder reminder) {
        String sql = "INSERT INTO reminder (user_id, title, description, due_date, is_completed) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reminder.getUserId());
            stmt.setString(2, reminder.getTitle());
            stmt.setString(3, reminder.getDescription());
            stmt.setString(4, reminder.getDueDate().toString());
            stmt.setInt(5, reminder.isCompleted() ? 1 : 0);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        reminder.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding reminder: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean update(Reminder reminder) {
        String sql = "UPDATE reminder SET title = ?, description = ?, due_date = ?, is_completed = ? WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reminder.getTitle());
            stmt.setString(2, reminder.getDescription());
            stmt.setString(3, reminder.getDueDate().toString());
            stmt.setInt(4, reminder.isCompleted() ? 1 : 0);
            stmt.setInt(5, reminder.getId());
            stmt.setInt(6, reminder.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reminder: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean markAsCompleted(int reminderId, int userId) {
        String sql = "UPDATE reminder SET is_completed = 1 WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reminderId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking reminder as completed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean delete(int reminderId, int userId) {
        String sql = "DELETE FROM reminder WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reminderId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting reminder: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public Reminder findById(int reminderId, int userId) {
        String sql = "SELECT * FROM reminder WHERE id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reminderId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractReminderFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding reminder by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Reminder> findAllByUserId(int userId) {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminder WHERE user_id = ? ORDER BY due_date ASC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reminder reminder = extractReminderFromResultSet(rs);
                    reminders.add(reminder);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all reminders by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return reminders;
    }
    
    public List<Reminder> findActiveReminders(int userId) {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminder WHERE user_id = ? AND is_completed = 0 ORDER BY due_date ASC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reminder reminder = extractReminderFromResultSet(rs);
                    reminders.add(reminder);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding active reminders: " + e.getMessage());
            e.printStackTrace();
        }
        return reminders;
    }
    
    public List<Reminder> findDueReminders(int userId, LocalDateTime currentDateTime) {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminder WHERE user_id = ? AND is_completed = 0 AND due_date <= ? ORDER BY due_date ASC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, currentDateTime.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reminder reminder = extractReminderFromResultSet(rs);
                    reminders.add(reminder);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding due reminders: " + e.getMessage());
            e.printStackTrace();
        }
        return reminders;
    }
    
    private Reminder extractReminderFromResultSet(ResultSet rs) throws SQLException {
        Reminder reminder = new Reminder();
        reminder.setId(rs.getInt("id"));
        reminder.setUserId(rs.getInt("user_id"));
        reminder.setTitle(rs.getString("title"));
        reminder.setDescription(rs.getString("description"));
        reminder.setDueDate(LocalDateTime.parse(rs.getString("due_date")));
        reminder.setCompleted(rs.getInt("is_completed") == 1);
        return reminder;
    }
} 