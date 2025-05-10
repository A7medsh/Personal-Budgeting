package com.personalbudgeting.service;

import com.personalbudgeting.dao.ReminderDAO;
import com.personalbudgeting.model.Reminder;
import com.personalbudgeting.model.User;
import com.personalbudgeting.service.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class ReminderService {
    private ReminderDAO reminderDAO;
    private Timer reminderTimer;
    private List<Reminder> activeReminders;
    private Set<Integer> processedReminderIds;
    private static Consumer<Void> uiRefreshCallback;
    
    public ReminderService() {
        this.reminderDAO = new ReminderDAO();
        this.activeReminders = new ArrayList<>();
        this.processedReminderIds = new HashSet<>();
        this.reminderTimer = new Timer(true); // Daemon timer
        
        System.out.println("Initializing ReminderService - scheduling reminder checks");
        
        // Schedule a task to check for due reminders every 30 seconds
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Checking for due reminders at: " + LocalDateTime.now());
                checkDueReminders();
            }
        }, 0, 30 * 1000); // Check every 30 seconds
        
        System.out.println("ReminderService initialized successfully");
    }
    
    // Method to set a callback for UI refresh
    public static void setUIRefreshCallback(Consumer<Void> callback) {
        uiRefreshCallback = callback;
    }
    
    // Method to trigger UI refresh
    private void notifyUIRefresh() {
        if (uiRefreshCallback != null) {
            javafx.application.Platform.runLater(() -> {
                uiRefreshCallback.accept(null);
            });
        }
    }
    
    public boolean addReminder(String title, String description, LocalDateTime dueDate) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Validate input
        if (title == null || title.trim().isEmpty() || dueDate == null || dueDate.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        Reminder reminder = new Reminder(currentUser.getId(), title, description, dueDate);
        boolean success = reminderDAO.add(reminder);
        
        if (success) {
            // Add to active reminders
            refreshActiveReminders();
        }
        
        return success;
    }
    
    public boolean updateReminder(int reminderId, String title, String description, LocalDateTime dueDate) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Validate input
        if (title == null || title.trim().isEmpty() || dueDate == null) {
            return false;
        }
        
        // Get the reminder to update
        Reminder reminder = reminderDAO.findById(reminderId, currentUser.getId());
        if (reminder == null) {
            return false;
        }
        
        // Update the reminder
        reminder.setTitle(title);
        reminder.setDescription(description);
        reminder.setDueDate(dueDate);
        
        boolean success = reminderDAO.update(reminder);
        
        if (success) {
            // Refresh active reminders
            refreshActiveReminders();
            
            // Notify UI to refresh
            notifyUIRefresh();
        }
        
        return success;
    }
    
    public boolean markReminderAsCompleted(int reminderId) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Get the reminder to update its status in the notification history
        Reminder reminder = reminderDAO.findById(reminderId, currentUser.getId());
        boolean success = reminderDAO.markAsCompleted(reminderId, currentUser.getId());
        
        if (success) {
            // Update notification history if the reminder was found
            if (reminder != null) {
                NotificationService.getInstance().updateReminderStatus(
                    reminderId, reminder.getTitle(), true
                );
            }
            
            // Refresh active reminders
            refreshActiveReminders();
            
            // Notify UI to refresh
            notifyUIRefresh();
        }
        
        return success;
    }
    
    public boolean deleteReminder(int reminderId) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        boolean success = reminderDAO.delete(reminderId, currentUser.getId());
        
        if (success) {
            // Refresh active reminders
            refreshActiveReminders();
            
            // Notify UI to refresh
            notifyUIRefresh();
        }
        
        return success;
    }
    
    public List<Reminder> getAllReminders() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return reminderDAO.findAllByUserId(currentUser.getId());
    }
    
    public List<Reminder> getActiveReminders() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return reminderDAO.findActiveReminders(currentUser.getId());
    }
    
    private void refreshActiveReminders() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            activeReminders.clear();
            processedReminderIds.clear();
            return;
        }
        
        List<Reminder> newActiveReminders = reminderDAO.findActiveReminders(currentUser.getId());
        
        // Clear processed reminder IDs that are no longer in the active reminders list
        Set<Integer> currentActiveIds = new HashSet<>();
        for (Reminder reminder : newActiveReminders) {
            currentActiveIds.add(reminder.getId());
        }
        
        // Remove processed IDs that are no longer in active reminders
        processedReminderIds.removeIf(id -> !currentActiveIds.contains(id));
        
        // Update active reminders list
        activeReminders = newActiveReminders;
    }
    
    private void checkDueReminders() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        List<Reminder> dueReminders = reminderDAO.findDueReminders(currentUser.getId(), LocalDateTime.now());
        
        for (Reminder reminder : dueReminders) {
            // Skip if we've already processed this reminder
            if (processedReminderIds.contains(reminder.getId())) {
                continue;
            }
            
            // Add to processed set
            processedReminderIds.add(reminder.getId());
            
            // Use NotificationService to show a popup alert
            NotificationService.getInstance().showReminderAlert(
                reminder.getTitle(), 
                reminder.getDescription(),
                reminder.getId()
            );
            
            // We'll now let the NotificationService handle marking it as complete based on user input
            // So we'll remove the auto-mark as completed here
            
            // Log to console as well
            System.out.println("REMINDER: " + reminder.getTitle() + " is due now!");
        }
    }
    
    public void shutdownReminderService() {
        if (reminderTimer != null) {
            reminderTimer.cancel();
            reminderTimer = null;
        }
    }
} 