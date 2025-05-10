package com.personalbudgeting.controller;

import com.personalbudgeting.Main;
import com.personalbudgeting.model.Expense;
import com.personalbudgeting.service.ExpenseService;
import com.personalbudgeting.service.NotificationService;
import com.personalbudgeting.service.UserService;
import com.personalbudgeting.util.FXMLNavigator;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ExpenseController implements Initializable {
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private TextField amountField;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private TableView<Expense> expenseTable;
    
    @FXML
    private TableColumn<Expense, String> categoryColumn;
    
    @FXML
    private TableColumn<Expense, Double> amountColumn;
    
    @FXML
    private TableColumn<Expense, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<Expense, String> descriptionColumn;
    
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
    
    // Predefined expense categories
    private final List<String> CATEGORIES = Arrays.asList(
        "Food", "Rent", "Utilities", "Transportation", "Entertainment", 
        "Healthcare", "Education", "Shopping", "Personal Care", "Travel", "Other"
    );
    
    private ExpenseService expenseService;
    private NotificationService notificationService;
    private UserService userService;
    private ObservableList<Expense> expenseList;
    private Expense selectedExpense;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        expenseService = new ExpenseService();
        notificationService = NotificationService.getInstance();
        userService = new UserService();
        expenseList = FXCollections.observableArrayList();
        
        // Initialize date picker to current date
        datePicker.setValue(LocalDate.now());
        
        // Set up category combo box
        categoryComboBox.getItems().addAll(CATEGORIES);
        
        // Set up table columns
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        amountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        
        // Format the amount column to show 2 decimal places
        amountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Expense, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        // Format the date column
        dateColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Expense, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        
        // Load expense data
        loadExpenseData();
        
        // Add listener for table row selection
        expenseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedExpense = newSelection;
                populateFields(selectedExpense);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                selectedExpense = null;
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        
        // Disable update and delete buttons initially
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    private void loadExpenseData() {
        List<Expense> expenses = expenseService.getAllExpenses();
        if (expenses != null) {
            expenseList.clear();
            expenseList.addAll(expenses);
            expenseTable.setItems(expenseList);
        }
    }
    
    private void populateFields(Expense expense) {
        categoryComboBox.setValue(expense.getCategory());
        amountField.setText(String.format("%.2f", expense.getAmount()));
        datePicker.setValue(expense.getDate());
        descriptionArea.setText(expense.getDescription());
    }
    
    private void clearFields() {
        categoryComboBox.getSelectionModel().clearSelection();
        amountField.clear();
        datePicker.setValue(LocalDate.now());
        descriptionArea.clear();
        expenseTable.getSelectionModel().clearSelection();
        selectedExpense = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    @FXML
    private void handleAddButton(ActionEvent event) {
        String category = categoryComboBox.getValue();
        String amountText = amountField.getText();
        LocalDate date = datePicker.getValue();
        String description = descriptionArea.getText();
        
        // Validate input
        if (category == null || category.isEmpty() || amountText.isEmpty() || date == null) {
            notificationService.showError("Input Error", "Category, amount, and date are required.");
            return;
        }
        
        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                notificationService.showError("Input Error", "Amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            notificationService.showError("Input Error", "Invalid amount format. Please enter a valid number.");
            return;
        }
        
        // Add expense
        boolean success = expenseService.addExpense(category, amount, date, description);
        
        if (success) {
            notificationService.showInfo("Success", "Expense added successfully.");
            clearFields();
            loadExpenseData();
        } else {
            notificationService.showError("Error", "Failed to add expense.");
        }
    }
    
    @FXML
    private void handleUpdateButton(ActionEvent event) {
        if (selectedExpense == null) {
            notificationService.showError("Selection Error", "No expense selected for update.");
            return;
        }
        
        String category = categoryComboBox.getValue();
        String amountText = amountField.getText();
        LocalDate date = datePicker.getValue();
        String description = descriptionArea.getText();
        
        // Validate input
        if (category == null || category.isEmpty() || amountText.isEmpty() || date == null) {
            notificationService.showError("Input Error", "Category, amount, and date are required.");
            return;
        }
        
        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                notificationService.showError("Input Error", "Amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            notificationService.showError("Input Error", "Invalid amount format. Please enter a valid number.");
            return;
        }
        
        // Update expense
        boolean success = expenseService.updateExpense(selectedExpense.getId(), category, amount, date, description);
        
        if (success) {
            notificationService.showInfo("Success", "Expense updated successfully.");
            clearFields();
            loadExpenseData();
        } else {
            notificationService.showError("Error", "Failed to update expense.");
        }
    }
    
    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedExpense == null) {
            notificationService.showError("Selection Error", "No expense selected for deletion.");
            return;
        }
        
        boolean success = expenseService.deleteExpense(selectedExpense.getId());
        
        if (success) {
            notificationService.showInfo("Success", "Expense deleted successfully.");
            clearFields();
            loadExpenseData();
        } else {
            notificationService.showError("Error", "Failed to delete expense.");
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
        // Already on expense view
    }
    
    @FXML
    private void handleBudgetNavButton(ActionEvent event) {
        try {
            // Use the same direct approach that works for income
            Main.setRoot("budget");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to budget view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleReminderNavButton(ActionEvent event) {
        try {
            // Use the same direct approach that works for income
            Main.setRoot("reminder");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to reminder view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogoutButton(ActionEvent event) {
        userService.logout();
        
        try {
            Main.setRoot("login");
        } catch (IOException e) {
            notificationService.showError("Navigation Error", "Error navigating to login view: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 