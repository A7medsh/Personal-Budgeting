package com.personalbudgeting.controller;

import com.personalbudgeting.Main;
import com.personalbudgeting.model.Budget;
import com.personalbudgeting.service.BudgetService;
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
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BudgetController implements Initializable {
    
    // Static initialization block to ensure the budget.fxml file is copied correctly
    static {
        try {
            System.out.println("BudgetController static initializer: Ensuring budget.fxml is properly copied");
            File sourceFile = new File("src/main/resources/fxml/budget.fxml");
            File targetFile = new File("target/classes/fxml/budget.fxml");
            
            // Create parent directories if needed
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            
            // Copy file if it exists
            if (sourceFile.exists()) {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Successfully copied budget.fxml to target directory");
            } else {
                System.err.println("Source budget.fxml file not found!");
            }
        } catch (Exception e) {
            System.err.println("Error copying budget.fxml in static initializer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private TextField amountField;
    
    @FXML
    private DatePicker monthPicker;
    
    @FXML
    private Button setBudgetButton;
    
    @FXML
    private Button updateBudgetButton;
    
    @FXML
    private Button deleteBudgetButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private TableView<Budget> budgetTable;
    
    @FXML
    private TableColumn<Budget, String> categoryColumn;
    
    @FXML
    private TableColumn<Budget, Double> budgetAmountColumn;
    
    @FXML
    private TableColumn<Budget, Double> spentAmountColumn;
    
    @FXML
    private TableColumn<Budget, Double> remainingColumn;
    
    @FXML
    private TableColumn<Budget, Double> percentageColumn;
    
    @FXML
    private PieChart budgetPieChart;
    
    @FXML
    private PieChart expensePieChart;
    
    @FXML
    private Label totalIncomeLabel;
    
    @FXML
    private Label totalBudgetLabel;
    
    @FXML
    private Label totalExpensesLabel;
    
    @FXML
    private Label remainingIncomeLabel;
    
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
    
    private BudgetService budgetService;
    private NotificationService notificationService;
    private UserService userService;
    private ObservableList<Budget> budgetList;
    private Budget selectedBudget;
    private YearMonth currentPeriod;
    
    // A wrapper class for the budget table to display additional information
    public class BudgetTableItem {
        private final Budget budget;
        private final double spentAmount;
        private final double remainingAmount;
        private final double percentageSpent;
        
        public BudgetTableItem(Budget budget, double spentAmount) {
            this.budget = budget;
            this.spentAmount = spentAmount;
            this.remainingAmount = budget.getAmount() - spentAmount;
            this.percentageSpent = (spentAmount / budget.getAmount()) * 100;
        }
        
        public Budget getBudget() {
            return budget;
        }
        
        public double getSpentAmount() {
            return spentAmount;
        }
        
        public double getRemainingAmount() {
            return remainingAmount;
        }
        
        public double getPercentageSpent() {
            return percentageSpent;
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        budgetService = new BudgetService();
        notificationService = NotificationService.getInstance();
        userService = new UserService();
        budgetList = FXCollections.observableArrayList();
        
        // Initialize date picker to current month
        monthPicker.setValue(LocalDate.now().withDayOfMonth(1));
        currentPeriod = YearMonth.from(LocalDate.now());
        
        // Set up category combo box
        categoryComboBox.getItems().addAll(CATEGORIES);
        
        // Set up table columns with proper cell value factories
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        budgetAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        spentAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSpentAmount()).asObject());
        remainingColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRemainingAmount()).asObject());
        percentageColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPercentage()).asObject());
        
        // Format the amount columns to show 2 decimal places
        budgetAmountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Budget, Double>() {
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
        
        // Format the spent amount column
        spentAmountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Budget, Double>() {
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
        
        // Format the remaining column
        remainingColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Budget, Double>() {
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
        
        // Format the percentage column
        percentageColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item));
                }
            }
        });
        
        // Load budget data
        loadBudgetData();
        updateBudgetAnalysis();
        
        // Add listener for table row selection
        budgetTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedBudget = newSelection;
                populateFields(selectedBudget);
                updateBudgetButton.setDisable(false);
                deleteBudgetButton.setDisable(false);
            } else {
                selectedBudget = null;
                updateBudgetButton.setDisable(true);
                deleteBudgetButton.setDisable(true);
            }
        });
        
        // Add listener for month picker changes
        monthPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentPeriod = YearMonth.from(newVal);
                loadBudgetData();
                updateBudgetAnalysis();
            }
        });
        
        // Disable update and delete buttons initially
        updateBudgetButton.setDisable(true);
        deleteBudgetButton.setDisable(true);
    }
    
    private void loadBudgetData() {
        List<Budget> budgets = budgetService.getBudgetsForPeriod(currentPeriod);
        if (budgets != null) {
            budgetList.clear();
            budgetList.addAll(budgets);
            budgetTable.setItems(budgetList);
            
            // Update the spent, remaining, and percentage columns with actual data
            Map<String, Object> budgetSummary = budgetService.getBudgetSummary(currentPeriod);
            if (budgetSummary != null) {
                // Get the expenses by category
                Map<String, Double> expensesByCategory = (Map<String, Double>) budgetSummary.get("expensesByCategory");
                
                // Manually update the cell values for the other columns
                if (expensesByCategory != null) {
                    for (Budget budget : budgetList) {
                        double spent = expensesByCategory.getOrDefault(budget.getCategory(), 0.0);
                        double remaining = budget.getAmount() - spent;
                        double percentage = (spent / budget.getAmount()) * 100.0;
                        
                        // The actual update will happen via the cell factories we set up
                        budget.setSpentAmount(spent);
                        budget.setRemainingAmount(remaining);
                        budget.setPercentage(percentage);
                    }
                }
            }
        }
    }
    
    private void updateBudgetAnalysis() {
        Map<String, Object> budgetSummary = budgetService.getBudgetSummary(currentPeriod);
        
        if (budgetSummary != null) {
            // Update summary labels
            double totalIncome = (double) budgetSummary.get("totalIncome");
            double totalBudget = (double) budgetSummary.get("totalBudget");
            double totalExpenses = (double) budgetSummary.get("totalExpenses");
            double savings = (double) budgetSummary.get("savings");
            
            totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
            totalBudgetLabel.setText(String.format("$%.2f", totalBudget));
            totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
            remainingIncomeLabel.setText(String.format("$%.2f", savings));
            
            // Update pie charts
            updateBudgetPieChart(budgetSummary);
            updateExpensePieChart(budgetSummary);
        }
    }
    
    private void updateBudgetPieChart(Map<String, Object> budgetSummary) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        Map<String, Double> budgetsByCategory = (Map<String, Double>) budgetSummary.get("budgetsByCategory");
        
        if (budgetsByCategory != null && !budgetsByCategory.isEmpty()) {
            for (Map.Entry<String, Double> entry : budgetsByCategory.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        } else {
            pieChartData.add(new PieChart.Data("No Budget", 1));
        }
        
        budgetPieChart.setData(pieChartData);
        budgetPieChart.setTitle("Budget Allocation");
    }
    
    private void updateExpensePieChart(Map<String, Object> budgetSummary) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        Map<String, Double> expensesByCategory = (Map<String, Double>) budgetSummary.get("expensesByCategory");
        
        if (expensesByCategory != null && !expensesByCategory.isEmpty()) {
            for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        } else {
            pieChartData.add(new PieChart.Data("No Expenses", 1));
        }
        
        expensePieChart.setData(pieChartData);
        expensePieChart.setTitle("Expense Distribution");
    }
    
    private void populateFields(Budget budget) {
        categoryComboBox.setValue(budget.getCategory());
        amountField.setText(String.format("%.2f", budget.getAmount()));
        monthPicker.setValue(budget.getPeriod().atDay(1));
    }
    
    private void clearFields() {
        categoryComboBox.getSelectionModel().clearSelection();
        amountField.clear();
        monthPicker.setValue(LocalDate.now().withDayOfMonth(1));
        budgetTable.getSelectionModel().clearSelection();
        selectedBudget = null;
        updateBudgetButton.setDisable(true);
        deleteBudgetButton.setDisable(true);
    }
    
    @FXML
    private void handleSetBudgetButton(ActionEvent event) {
        String category = categoryComboBox.getValue();
        String amountText = amountField.getText();
        LocalDate date = monthPicker.getValue();
        
        // Validate input
        if (category == null || category.isEmpty() || amountText.isEmpty() || date == null) {
            notificationService.showError("Input Error", "Category, amount, and month are required.");
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
        
        // Set budget
        boolean success = budgetService.setBudget(category, amount, YearMonth.from(date));
        
        if (success) {
            notificationService.showInfo("Success", "Budget set successfully.");
            clearFields();
            loadBudgetData();
            updateBudgetAnalysis();
        } else {
            notificationService.showError("Error", "Failed to set budget.");
        }
    }
    
    @FXML
    private void handleUpdateBudgetButton(ActionEvent event) {
        if (selectedBudget == null) {
            notificationService.showError("Selection Error", "No budget selected for update.");
            return;
        }
        
        String category = categoryComboBox.getValue();
        String amountText = amountField.getText();
        LocalDate date = monthPicker.getValue();
        
        // Validate input
        if (category == null || category.isEmpty() || amountText.isEmpty() || date == null) {
            notificationService.showError("Input Error", "Category, amount, and month are required.");
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
        
        // Update budget
        selectedBudget.setAmount(amount);
        boolean success = budgetService.setBudget(category, amount, YearMonth.from(date));
        
        if (success) {
            notificationService.showInfo("Success", "Budget updated successfully.");
            clearFields();
            loadBudgetData();
            updateBudgetAnalysis();
        } else {
            notificationService.showError("Error", "Failed to update budget.");
        }
    }
    
    @FXML
    private void handleDeleteBudgetButton(ActionEvent event) {
        if (selectedBudget == null) {
            notificationService.showError("Selection Error", "No budget selected for deletion.");
            return;
        }
        
        boolean success = budgetService.deleteBudget(selectedBudget.getId());
        
        if (success) {
            notificationService.showInfo("Success", "Budget deleted successfully.");
            clearFields();
            loadBudgetData();
            updateBudgetAnalysis();
        } else {
            notificationService.showError("Error", "Failed to delete budget.");
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
        // Already on budget view
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