package com.personalbudgeting.controller;

import com.personalbudgeting.Main;
import com.personalbudgeting.model.Income;
import com.personalbudgeting.service.IncomeService;
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
import java.util.List;
import java.util.ResourceBundle;

public class IncomeController implements Initializable {
    
    @FXML
    private TextField sourceField;
    
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
    private TableView<Income> incomeTable;
    
    @FXML
    private TableColumn<Income, String> sourceColumn;
    
    @FXML
    private TableColumn<Income, Double> amountColumn;
    
    @FXML
    private TableColumn<Income, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<Income, String> descriptionColumn;
    
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
    
    private IncomeService incomeService;
    private NotificationService notificationService;
    private UserService userService;
    private ObservableList<Income> incomeList;
    private Income selectedIncome;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        incomeService = new IncomeService();
        notificationService = NotificationService.getInstance();
        userService = new UserService();
        incomeList = FXCollections.observableArrayList();
        
        // Initialize date picker to current date
        datePicker.setValue(LocalDate.now());
        
        // Set up table columns
        sourceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSource()));
        amountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        
        // Format the amount column to show 2 decimal places
        amountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Income, Double>() {
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
        dateColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Income, LocalDate>() {
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
        
        // Load income data
        loadIncomeData();
        
        // Add listener for table row selection
        incomeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedIncome = newSelection;
                populateFields(selectedIncome);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                selectedIncome = null;
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        
        // Disable update and delete buttons initially
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    private void loadIncomeData() {
        List<Income> incomes = incomeService.getAllIncome();
        if (incomes != null) {
            incomeList.clear();
            incomeList.addAll(incomes);
            incomeTable.setItems(incomeList);
        }
    }
    
    private void populateFields(Income income) {
        sourceField.setText(income.getSource());
        amountField.setText(String.format("%.2f", income.getAmount()));
        datePicker.setValue(income.getDate());
        descriptionArea.setText(income.getDescription());
    }
    
    private void clearFields() {
        sourceField.clear();
        amountField.clear();
        datePicker.setValue(LocalDate.now());
        descriptionArea.clear();
        incomeTable.getSelectionModel().clearSelection();
        selectedIncome = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    @FXML
    private void handleAddButton(ActionEvent event) {
        String source = sourceField.getText();
        String amountText = amountField.getText();
        LocalDate date = datePicker.getValue();
        String description = descriptionArea.getText();
        
        // Validate input
        if (source.isEmpty() || amountText.isEmpty() || date == null) {
            notificationService.showError("Input Error", "Source, amount, and date are required.");
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
        
        // Add income
        boolean success = incomeService.addIncome(source, amount, date, description);
        
        if (success) {
            notificationService.showInfo("Success", "Income added successfully.");
            clearFields();
            loadIncomeData();
        } else {
            notificationService.showError("Error", "Failed to add income.");
        }
    }
    
    @FXML
    private void handleUpdateButton(ActionEvent event) {
        if (selectedIncome == null) {
            notificationService.showError("Selection Error", "No income selected for update.");
            return;
        }
        
        String source = sourceField.getText();
        String amountText = amountField.getText();
        LocalDate date = datePicker.getValue();
        String description = descriptionArea.getText();
        
        // Validate input
        if (source.isEmpty() || amountText.isEmpty() || date == null) {
            notificationService.showError("Input Error", "Source, amount, and date are required.");
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
        
        // Update income
        boolean success = incomeService.updateIncome(selectedIncome.getId(), source, amount, date, description);
        
        if (success) {
            notificationService.showInfo("Success", "Income updated successfully.");
            clearFields();
            loadIncomeData();
        } else {
            notificationService.showError("Error", "Failed to update income.");
        }
    }
    
    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedIncome == null) {
            notificationService.showError("Selection Error", "No income selected for deletion.");
            return;
        }
        
        boolean success = incomeService.deleteIncome(selectedIncome.getId());
        
        if (success) {
            notificationService.showInfo("Success", "Income deleted successfully.");
            clearFields();
            loadIncomeData();
        } else {
            notificationService.showError("Error", "Failed to delete income.");
        }
    }
    
    @FXML
    private void handleClearButton(ActionEvent event) {
        clearFields();
    }
    
    @FXML
    private void handleIncomeNavButton(ActionEvent event) {
        // Already on income view
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
        try {
            // Use the same direct approach that works for other navigation
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