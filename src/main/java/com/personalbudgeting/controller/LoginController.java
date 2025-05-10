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

public class LoginController implements Initializable {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button signUpButton;
    
    private UserService userService;
    private NotificationService notificationService;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService = new UserService();
        notificationService = NotificationService.getInstance();
    }
    
    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            notificationService.showError("Login Error", "Username and password cannot be empty.");
            return;
        }
        
        // Attempt to login
        boolean success = userService.login(username, password);
        
        if (success) {
            notificationService.showInfo("Login Success", "Welcome back, " + username + "!");
            
            try {
                // Navigate to dashboard (expense view as the main view)
                Main.setRoot("expense");
            } catch (IOException e) {
                notificationService.showError("Navigation Error", "Error navigating to dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            notificationService.showError("Login Failed", "Invalid username or password. Please try again.");
        }
    }
    
    @FXML
    private void handleSignUpLink(ActionEvent event) {
        try {
            Main.setRoot("signup");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to sign up view: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 