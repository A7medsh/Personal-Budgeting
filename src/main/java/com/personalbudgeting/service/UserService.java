package com.personalbudgeting.service;

import com.personalbudgeting.dao.UserDAO;
import com.personalbudgeting.model.User;

import java.time.LocalDateTime;
import java.util.Random;
import org.apache.commons.validator.routines.EmailValidator;

public class UserService {
    private UserDAO userDAO;
    private static User currentUser;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    public boolean register(String username, String email, String password, String confirmPassword) {
        // Validate input
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return false;
        }
        
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            return false;
        }
        
        // Validate email format
        if (!EmailValidator.getInstance().isValid(email)) {
            return false;
        }
        
        // Check if username already exists
        if (userDAO.findByUsername(username) != null) {
            return false;
        }
        
        // Check if email already exists
        if (userDAO.findByEmail(email) != null) {
            return false;
        }
        
        // Create new user
        User user = new User(username, email, password);
        
        // Generate OTP for verification
        String otpCode = generateOtp();
        LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(15);
        user.setOtpCode(otpCode);
        user.setOtpExpiry(otpExpiry);
        
        boolean success = userDAO.register(user);
        
        if (success) {
            // Display OTP in console
            System.out.println("=================================================");
            System.out.println("Registration successful! OTP for " + username + ": " + otpCode);
            System.out.println("=================================================");
            return true;
        }
        
        return false;
    }
    
    public boolean verifyOtp(String username, String otpCode) {
        System.out.println("DEBUG: Starting OTP verification for user: " + username);
        System.out.println("DEBUG: Input OTP: " + otpCode);
        
        User user = userDAO.findByUsername(username);
        
        if (user == null) {
            System.out.println("DEBUG: User not found in database");
            return false;
        }
        
        if (user.getOtpCode() == null) {
            System.out.println("DEBUG: User's OTP code is null");
            return false;
        }
        
        if (user.getOtpExpiry() == null) {
            System.out.println("DEBUG: User's OTP expiry is null");
            return false;
        }
        
        System.out.println("DEBUG: User found with OTP: " + user.getOtpCode());
        System.out.println("DEBUG: OTP expiry: " + user.getOtpExpiry());
        System.out.println("DEBUG: Current time: " + LocalDateTime.now());
        
        // Check if OTP matches
        boolean otpMatches = user.getOtpCode().equals(otpCode);
        System.out.println("DEBUG: OTP match result: " + otpMatches);
        
        // Check if OTP is not expired
        boolean notExpired = LocalDateTime.now().isBefore(user.getOtpExpiry());
        System.out.println("DEBUG: OTP expiry status (not expired): " + notExpired);
        
        // Check if OTP matches and is not expired
        if (otpMatches && notExpired) {
            boolean verified = userDAO.verifyUser(user.getId());
            System.out.println("DEBUG: User verification result: " + verified);
            return verified;
        }
        
        System.out.println("DEBUG: OTP verification failed: " + (otpMatches ? "OTP matched but expired" : "OTP did not match"));
        return false;
    }
    
    public boolean login(String username, String password) {
        User user = userDAO.findByUsername(username);
        
        if (user != null && user.getPassword().equals(password) && user.isVerified()) {
            currentUser = user;
            return true;
        }
        
        return false;
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    private String generateOtp() {
        // Generate a 6-digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    public boolean updatePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (currentUser == null) {
            return false;
        }
        
        // Validate input
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return false;
        }
        
        // Check if current password is correct
        if (!currentUser.getPassword().equals(currentPassword)) {
            return false;
        }
        
        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }
        
        // Update password
        boolean success = userDAO.updatePassword(currentUser.getId(), newPassword);
        
        if (success) {
            // Update current user
            currentUser.setPassword(newPassword);
        }
        
        return success;
    }
} 