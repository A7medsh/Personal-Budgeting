package com.personalbudgeting.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.personalbudgeting.service.ReminderService;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private static NotificationService instance;
    private List<String> notificationHistory;
    
    // Private constructor to enforce singleton pattern
    private NotificationService() {
        notificationHistory = new ArrayList<>();
    }
    
    // Singleton getInstance method
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    // Show an information notification
    public void showInfo(String title, String message) {
        notificationHistory.add("[INFO] " + title + ": " + message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // Show a warning notification
    public void showWarning(String title, String message) {
        notificationHistory.add("[WARNING] " + title + ": " + message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // Show an error notification
    public void showError(String title, String message) {
        notificationHistory.add("[ERROR] " + title + ": " + message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // Show a budget alert notification
    public void showBudgetAlert(String category, double budgetAmount, double spentAmount) {
        String title = "Budget Alert";
        String message = "You have exceeded your budget for " + category + ".\n" +
                         "Budget: $" + String.format("%.2f", budgetAmount) + "\n" +
                         "Spent: $" + String.format("%.2f", spentAmount);
        
        showWarning(title, message);
    }
    
    // Show a reminder notification
    public void showReminderAlert(String reminderTitle, String description, int reminderId) {
        String title = "Reminder Alert";
        
        // Format the message with all reminder details
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Your reminder is due:\n\n");
        messageBuilder.append("Title: ").append(reminderTitle).append("\n");
        
        // Add description if available
        if (description != null && !description.isEmpty()) {
            messageBuilder.append("\nDescription:\n").append(description);
        }
        
        // Add current time
        messageBuilder.append("\n\nCurrent time: ")
                     .append(java.time.LocalDateTime.now().format(
                         java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Log the notification
        notificationHistory.add("[REMINDER] " + title + ": " + messageBuilder.toString());
        
        // Play a notification sound
        try {
            // Use the default system beep sound
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Failed to play notification sound: " + e.getMessage());
        }
        
        // Show a popup alert with custom buttons
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(reminderTitle);
            alert.setContentText(messageBuilder.toString());
            
            // Make dialog resizable for longer descriptions
            alert.setResizable(true);
            
            // Add custom buttons
            javafx.scene.control.ButtonType doneButton = new javafx.scene.control.ButtonType("Mark as Complete", 
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            javafx.scene.control.ButtonType closeButton = new javafx.scene.control.ButtonType("Close", 
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(doneButton, closeButton);
            
            // Show and wait for user response
            javafx.scene.control.ButtonType result = alert.showAndWait().orElse(closeButton);
            
            // Handle the "Mark as Complete" button
            if (result == doneButton) {
                System.out.println("User clicked 'Mark as Complete' for reminder: " + reminderTitle + " (ID: " + reminderId + ")");
                // Get the ReminderService and mark the reminder as completed
                ReminderService reminderService = new ReminderService();
                boolean success = reminderService.markReminderAsCompleted(reminderId);
                if (success) {
                    updateReminderStatus(reminderId, reminderTitle, true);
                    showInfo("Success", "Reminder marked as completed.");
                } else {
                    showError("Error", "Failed to mark reminder as completed.");
                }
            }
        });
    }
    
    // Get the notification history
    public List<String> getNotificationHistory() {
        return new ArrayList<>(notificationHistory); // Return a copy to prevent modification
    }
    
    // Clear the notification history
    public void clearNotificationHistory() {
        notificationHistory.clear();
    }
    
    // Update notification history with reminder completion status
    public void updateReminderStatus(int reminderId, String title, boolean completed) {
        String status = completed ? "COMPLETED" : "PENDING";
        notificationHistory.add("[REMINDER UPDATE] Reminder ID " + reminderId + 
                               " '" + title + "' status changed to: " + status);
    }
} 