package com.personalbudgeting.controller;

import com.personalbudgeting.Main;
import com.personalbudgeting.service.NotificationService;
import com.personalbudgeting.service.UserService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button signUpButton;
    
    @FXML
    private Button backToLoginButton;
    
    @FXML
    private TextField otpField;
    
    @FXML
    private Button verifyOtpButton;
    
    private UserService userService;
    private NotificationService notificationService;
    private boolean registrationComplete = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService = new UserService();
        notificationService = NotificationService.getInstance();
        
        // Initially hide OTP fields
        otpField.setVisible(false);
        verifyOtpButton.setVisible(false);
    }
    
    @FXML
    private void handleSignUpButton(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            notificationService.showError("Sign Up Error", "All fields are required.");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            notificationService.showError("Sign Up Error", "Passwords do not match.");
            return;
        }
        
        // Attempt to register
        boolean success = userService.register(username, email, password, confirmPassword);
        
        if (success) {
            notificationService.showInfo("Registration Success", "Registration successful! Please check your OTP (shown in console for demo).");
            
            // Show OTP verification fields
            otpField.setVisible(true);
            verifyOtpButton.setVisible(true);
            
            // Disable registration fields
            usernameField.setDisable(true);
            emailField.setDisable(true);
            passwordField.setDisable(true);
            confirmPasswordField.setDisable(true);
            signUpButton.setDisable(true);
            
            registrationComplete = true;
        } else {
            notificationService.showError("Registration Failed", "Failed to register. Username or email might already be in use.");
        }
    }
    
    @FXML
    private void handleVerifyOtpButton(ActionEvent event) {
        if (!registrationComplete) {
            notificationService.showError("Verification Error", "Please complete registration first.");
            return;
        }
        
        String username = usernameField.getText();
        String otp = otpField.getText();
        
        if (otp.isEmpty()) {
            notificationService.showError("Verification Error", "OTP cannot be empty.");
            return;
        }
        
        boolean success = userService.verifyOtp(username, otp);
        
        if (success) {
            notificationService.showInfo("Verification Success", "Account verified successfully! You can now log in.");
            
            try {
                // Navigate back to login
                Main.setRoot("login");
            } catch (IOException e) {
                notificationService.showError("Navigation Error", "Error navigating to login: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            notificationService.showError("Verification Failed", "Invalid or expired OTP. Please try again.");
        }
    }
    
    @FXML
    private void handleBackToLoginButton(ActionEvent event) {
        try {
            // Navigate back to login
            Main.setRoot("login");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 