package com.personalbudgeting.dao;

import com.personalbudgeting.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class UserDAO {
    private Connection connection;
    
    public UserDAO() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, email, password, otp_code, otp_expiry, verified) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getOtpCode());
            
            if (user.getOtpExpiry() != null) {
                stmt.setString(5, user.getOtpExpiry().toString());
            } else {
                stmt.setNull(5, java.sql.Types.VARCHAR);
            }
            
            stmt.setInt(6, user.isVerified() ? 1 : 0);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                System.out.println("DEBUG: User registered with OTP: " + user.getOtpCode() + ", expiry: " + user.getOtpExpiry());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setOtpCode(rs.getString("otp_code"));
                    
                    String otpExpiry = rs.getString("otp_expiry");
                    if (otpExpiry != null && !otpExpiry.isEmpty()) {
                        user.setOtpExpiry(LocalDateTime.parse(otpExpiry));
                    }
                    
                    user.setVerified(rs.getInt("verified") == 1);
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setOtpCode(rs.getString("otp_code"));
                    
                    String otpExpiry = rs.getString("otp_expiry");
                    if (otpExpiry != null && !otpExpiry.isEmpty()) {
                        user.setOtpExpiry(LocalDateTime.parse(otpExpiry));
                    }
                    
                    user.setVerified(rs.getInt("verified") == 1);
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean updateOtp(int userId, String otpCode, LocalDateTime otpExpiry) {
        String sql = "UPDATE users SET otp_code = ?, otp_expiry = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, otpCode);
            stmt.setString(2, otpExpiry.toString());
            stmt.setInt(3, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating OTP: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean verifyUser(int userId) {
        String sql = "UPDATE users SET verified = 1, otp_code = NULL, otp_expiry = NULL WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error verifying user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
} 