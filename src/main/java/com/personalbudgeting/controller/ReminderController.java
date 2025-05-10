package com.personalbudgeting.controller;

import com.personalbudgeting.Main;
import com.personalbudgeting.model.Reminder;
import com.personalbudgeting.service.NotificationService;
import com.personalbudgeting.service.ReminderService;
import com.personalbudgeting.service.UserService;
import com.personalbudgeting.util.FXMLNavigator;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReminderController implements Initializable {
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private TextField timeField;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private TableView<Reminder> reminderTable;
    
    @FXML
    private TableColumn<Reminder, String> titleColumn;
    
    @FXML
    private TableColumn<Reminder, LocalDateTime> dueDateColumn;
    
    @FXML
    private TableColumn<Reminder, String> descriptionColumn;
    
    @FXML
    private TableColumn<Reminder, Boolean> completedColumn;
    
    @FXML
    private TableColumn<Reminder, Reminder> actionColumn;
    
    @FXML
    private Label upcomingRemindersLabel;
    
    @FXML
    private VBox navBar;
    
    @FXML
    private Button incomeNavButton;
    
    @FXML
    private Button expenseNavButton;
    
    @FXML
    private Button budgetNavButton;
    
    @FXML
    private Button reminderNavButton;
    
    @FXML
    private Button logoutButton;
    
    private ReminderService reminderService;
    private NotificationService notificationService;
    private UserService userService;
    private ObservableList<Reminder> reminderList;
    private Reminder selectedReminder;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reminderService = new ReminderService();
        notificationService = NotificationService.getInstance();
        userService = new UserService();
        reminderList = FXCollections.observableArrayList();
        
        // Register callback for UI refresh when reminders are marked as completed
        ReminderService.setUIRefreshCallback(v -> {
            loadReminderData();
            updateUpcomingRemindersLabel();
        });
        
        // Initialize date picker to current date
        datePicker.setValue(LocalDate.now());
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        // Set up table columns
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        dueDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDueDate()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        completedColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isCompleted()));
        
        // Format the due date column
        dueDateColumn.setCellFactory(column -> new TableCell<Reminder, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        
        // Format the completed column as checkboxes
        completedColumn.setCellFactory(column -> new TableCell<Reminder, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.setDisable(true);
            }
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });
        
        // Create a "Mark as Completed" button in action column
        actionColumn.setCellFactory(createActionCellFactory());
        
        // Load reminder data
        loadReminderData();
        updateUpcomingRemindersLabel();
        
        // Add listener for table row selection
        reminderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedReminder = newSelection;
                populateFields(selectedReminder);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                selectedReminder = null;
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        
        // Disable update and delete buttons initially
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    private Callback<TableColumn<Reminder, Reminder>, TableCell<Reminder, Reminder>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Reminder, Reminder> call(TableColumn<Reminder, Reminder> param) {
                return new TableCell<>() {
                    private final Button completeButton = new Button("Complete");
                    
                    {
                        completeButton.setOnAction(event -> {
                            Reminder reminder = getTableView().getItems().get(getIndex());
                            if (!reminder.isCompleted()) {
                                boolean success = reminderService.markReminderAsCompleted(reminder.getId());
                                if (success) {
                                    notificationService.showInfo("Success", "Reminder marked as completed.");
                                    loadReminderData();
                                    updateUpcomingRemindersLabel();
                                } else {
                                    notificationService.showError("Error", "Failed to mark reminder as completed.");
                                }
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Reminder reminder, boolean empty) {
                        super.updateItem(reminder, empty);
                        if (empty || reminder == null) {
                            setGraphic(null);
                        } else {
                            if (reminder.isCompleted()) {
                                setGraphic(null);
                            } else {
                                setGraphic(completeButton);
                            }
                        }
                    }
                };
            }
        };
    }
    
    private void loadReminderData() {
        List<Reminder> reminders = reminderService.getAllReminders();
        if (reminders != null) {
            reminderList.clear();
            reminderList.addAll(reminders);
            reminderTable.setItems(reminderList);
        }
    }
    
    private void updateUpcomingRemindersLabel() {
        List<Reminder> activeReminders = reminderService.getActiveReminders();
        if (activeReminders != null && !activeReminders.isEmpty()) {
            upcomingRemindersLabel.setText("You have " + activeReminders.size() + " active reminders.");
        } else {
            upcomingRemindersLabel.setText("You have no active reminders.");
        }
    }
    
    private void populateFields(Reminder reminder) {
        titleField.setText(reminder.getTitle());
        descriptionArea.setText(reminder.getDescription());
        datePicker.setValue(reminder.getDueDate().toLocalDate());
        timeField.setText(reminder.getDueDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    }
    
    private void clearFields() {
        titleField.clear();
        descriptionArea.clear();
        datePicker.setValue(LocalDate.now());
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        reminderTable.getSelectionModel().clearSelection();
        selectedReminder = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    @FXML
    private void handleAddButton(ActionEvent event) {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        LocalDate date = datePicker.getValue();
        String timeText = timeField.getText();
        
        // Validate input
        if (title.isEmpty() || date == null || timeText.isEmpty()) {
            notificationService.showError("Input Error", "Title, date, and time are required.");
            return;
        }
        
        // Parse time
        LocalTime time;
        try {
            time = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            notificationService.showError("Input Error", "Invalid time format. Please use HH:mm format (e.g., 14:30).");
            return;
        }
        
        // Create due date
        LocalDateTime dueDate = LocalDateTime.of(date, time);
        
        // Check if due date is in the past
        if (dueDate.isBefore(LocalDateTime.now())) {
            notificationService.showError("Input Error", "Due date must be in the future.");
            return;
        }
        
        // Add reminder
        boolean success = reminderService.addReminder(title, description, dueDate);
        
        if (success) {
            notificationService.showInfo("Success", "Reminder added successfully.");
            clearFields();
            loadReminderData();
            updateUpcomingRemindersLabel();
        } else {
            notificationService.showError("Error", "Failed to add reminder.");
        }
    }
    
    @FXML
    private void handleUpdateButton(ActionEvent event) {
        if (selectedReminder == null) {
            notificationService.showError("Selection Error", "No reminder selected for update.");
            return;
        }
        
        String title = titleField.getText();
        String description = descriptionArea.getText();
        LocalDate date = datePicker.getValue();
        String timeText = timeField.getText();
        
        // Validate input
        if (title.isEmpty() || date == null || timeText.isEmpty()) {
            notificationService.showError("Input Error", "Title, date, and time are required.");
            return;
        }
        
        // Parse time
        LocalTime time;
        try {
            time = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            notificationService.showError("Input Error", "Invalid time format. Please use HH:mm format (e.g., 14:30).");
            return;
        }
        
        // Create due date
        LocalDateTime dueDate = LocalDateTime.of(date, time);
        
        // Update reminder
        boolean success = reminderService.updateReminder(selectedReminder.getId(), title, description, dueDate);
        
        if (success) {
            notificationService.showInfo("Success", "Reminder updated successfully.");
            clearFields();
            loadReminderData();
            updateUpcomingRemindersLabel();
        } else {
            notificationService.showError("Error", "Failed to update reminder.");
        }
    }
    
    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedReminder == null) {
            notificationService.showError("Selection Error", "No reminder selected for deletion.");
            return;
        }
        
        boolean success = reminderService.deleteReminder(selectedReminder.getId());
        
        if (success) {
            notificationService.showInfo("Success", "Reminder deleted successfully.");
            clearFields();
            loadReminderData();
            updateUpcomingRemindersLabel();
        } else {
            notificationService.showError("Error", "Failed to delete reminder.");
        }
    }
    
    @FXML
    private void handleClearButton(ActionEvent event) {
        clearFields();
    }
    
    @FXML
    private void handleIncomeNavButton(ActionEvent event) {
        try {
            Main.setRoot("income");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to income view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExpenseNavButton(ActionEvent event) {
        try {
            Main.setRoot("expense");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to expense view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBudgetNavButton(ActionEvent event) {
        try {
            // Use the same direct approach that works for other navigation
            Main.setRoot("budget");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to budget view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleReminderNavButton(ActionEvent event) {
        // Already on reminder view
    }
    
    @FXML
    private void handleLogoutButton(ActionEvent event) {
        // Shut down reminder service
        reminderService.shutdownReminderService();
        userService.logout();
        
        try {
            Main.setRoot("login");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to login view: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 