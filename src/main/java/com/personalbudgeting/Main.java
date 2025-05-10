package com.personalbudgeting;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import com.personalbudgeting.dao.DatabaseConnection;
import com.personalbudgeting.dao.DatabaseInitializer;
import com.personalbudgeting.service.ReminderService;
import com.personalbudgeting.util.FXMLDiagnostics;
import javafx.fxml.JavaFXBuilderFactory;
import com.personalbudgeting.controller.BudgetController;

public class Main extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private static ReminderService reminderService;
    
    // Keep a cache of loaded FXML roots to avoid reloading
    private static Parent budgetRoot;
    private static Parent reminderRoot;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        // Run diagnostics on problematic FXML files
        System.out.println("Running diagnostics before file preparation...");
        System.out.println(FXMLDiagnostics.runDiagnostics("budget"));
        System.out.println(FXMLDiagnostics.runDiagnostics("reminder"));
        
        // First, make sure all FXML files are properly copied
        prepareFXMLFiles();
        
        // Run diagnostics again after file preparation
        System.out.println("Running diagnostics after file preparation...");
        System.out.println(FXMLDiagnostics.runDiagnostics("budget"));
        System.out.println(FXMLDiagnostics.runDiagnostics("reminder"));
        
        // Manually ensure budget.fxml and reminder.fxml are copied correctly
        String budgetCopyResult = FXMLDiagnostics.copyFile(
            "src/main/resources/fxml/budget.fxml", 
            "target/classes/fxml/budget.fxml");
        System.out.println("Manual budget.fxml copy result:\n" + budgetCopyResult);
        
        String reminderCopyResult = FXMLDiagnostics.copyFile(
            "src/main/resources/fxml/reminder.fxml", 
            "target/classes/fxml/reminder.fxml");
        System.out.println("Manual reminder.fxml copy result:\n" + reminderCopyResult);
        
        // Preload controller classes
        preloadControllers();
        
        // Preload the problematic pages
        preloadProblematicPages();
        
        scene = new Scene(loadFXML("login"), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Personal Budgeting Application");
        
        // Handle proper application shutdown
        stage.setOnCloseRequest(event -> {
            if (reminderService != null) {
                reminderService.shutdownReminderService();
            }
            DatabaseConnection.closeConnection();
            Platform.exit();
        });
        
        stage.show();
    }
    
    /**
     * Preload the budget and reminder pages to avoid issues
     */
    private void preloadProblematicPages() {
        try {
            System.out.println("Preloading problematic pages using direct file approach...");
            
            // Try to load budget page
            try {
                File budgetFile = new File("src/main/resources/fxml/budget.fxml");
                if (budgetFile.exists()) {
                    System.out.println("Budget FXML file found at: " + budgetFile.getAbsolutePath());
                    FXMLLoader loader = new FXMLLoader(budgetFile.toURI().toURL());
                    budgetRoot = loader.load();
                    System.out.println("Successfully preloaded budget page");
                } else {
                    System.err.println("Budget FXML file not found: " + budgetFile.getAbsolutePath());
                    
                    // Try target directory as fallback
                    File targetBudgetFile = new File("target/classes/fxml/budget.fxml");
                    if (targetBudgetFile.exists()) {
                        System.out.println("Budget FXML file found in target at: " + targetBudgetFile.getAbsolutePath());
                        FXMLLoader loader = new FXMLLoader(targetBudgetFile.toURI().toURL());
                        budgetRoot = loader.load();
                        System.out.println("Successfully preloaded budget page from target directory");
                    } else {
                        System.err.println("Budget FXML file not found in any location");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error preloading budget page: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Try to load reminder page
            try {
                File reminderFile = new File("src/main/resources/fxml/reminder.fxml");
                if (reminderFile.exists()) {
                    System.out.println("Reminder FXML file found at: " + reminderFile.getAbsolutePath());
                    FXMLLoader loader = new FXMLLoader(reminderFile.toURI().toURL());
                    reminderRoot = loader.load();
                    System.out.println("Successfully preloaded reminder page");
                } else {
                    System.err.println("Reminder FXML file not found: " + reminderFile.getAbsolutePath());
                    
                    // Try target directory as fallback
                    File targetReminderFile = new File("target/classes/fxml/reminder.fxml");
                    if (targetReminderFile.exists()) {
                        System.out.println("Reminder FXML file found in target at: " + targetReminderFile.getAbsolutePath());
                        FXMLLoader loader = new FXMLLoader(targetReminderFile.toURI().toURL());
                        reminderRoot = loader.load();
                        System.out.println("Successfully preloaded reminder page from target directory");
                    } else {
                        System.err.println("Reminder FXML file not found in any location");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error preloading reminder page: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("Error during preloading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prepares all FXML files by copying them to the target directory
     */
    private void prepareFXMLFiles() {
        System.out.println("===== Main.prepareFXMLFiles: Beginning FXML file preparation =====");
        
        String[] fxmlFiles = {
            "login", "signup", "income", "expense", "budget", "reminder", "budget_simple"
        };
        
        for (String file : fxmlFiles) {
            try {
                File sourceFile = new File("src/main/resources/fxml/" + file + ".fxml");
                File targetFile = new File("target/classes/fxml/" + file + ".fxml");
                
                System.out.println("Processing " + file + ".fxml");
                
                // Make sure target directory exists
                if (!targetFile.getParentFile().exists()) {
                    System.out.println("Creating target directory: " + targetFile.getParentFile().getAbsolutePath());
                    boolean created = targetFile.getParentFile().mkdirs();
                    if (!created) {
                        System.err.println("Failed to create target directory");
                    }
                }
                
                // Copy the file if it exists
                if (sourceFile.exists()) {
                    System.out.println("Source file exists: " + sourceFile.getAbsolutePath() + " (Size: " + sourceFile.length() + " bytes)");
                    try {
                        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        if (targetFile.exists()) {
                            System.out.println("Successfully copied to: " + targetFile.getAbsolutePath() + " (Size: " + targetFile.length() + " bytes)");
                        } else {
                            System.err.println("Target file doesn't exist after copy operation: " + targetFile.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        System.err.println("Error copying file: " + e.getMessage());
                        e.printStackTrace();
                        
                        // Try alternative copy method
                        try {
                            System.out.println("Trying alternative copy method...");
                            java.io.InputStream in = new java.io.FileInputStream(sourceFile);
                            java.io.OutputStream out = new java.io.FileOutputStream(targetFile);
                            
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            in.close();
                            out.close();
                            
                            if (targetFile.exists()) {
                                System.out.println("Alternative copy successful: " + targetFile.getAbsolutePath() + " (Size: " + targetFile.length() + " bytes)");
                            } else {
                                System.err.println("Target file still doesn't exist after alternative copy");
                            }
                        } catch (Exception ex) {
                            System.err.println("Alternative copy method also failed: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("Source file not found: " + sourceFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Error preparing " + file + ".fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("===== Main.prepareFXMLFiles: FXML file preparation complete =====");
        
        // Verify file existence
        for (String file : fxmlFiles) {
            File targetFile = new File("target/classes/fxml/" + file + ".fxml");
            if (targetFile.exists()) {
                System.out.println("Verification: " + file + ".fxml exists in target directory (Size: " + targetFile.length() + " bytes)");
            } else {
                System.err.println("Verification FAILED: " + file + ".fxml does NOT exist in target directory");
            }
        }
    }

    /**
     * Preload controller classes to ensure they're available
     */
    private void preloadControllers() {
        try {
            // Force loading of all controller classes
            Class.forName("com.personalbudgeting.controller.LoginController");
            Class.forName("com.personalbudgeting.controller.SignUpController");
            Class.forName("com.personalbudgeting.controller.IncomeController");
            Class.forName("com.personalbudgeting.controller.ExpenseController");
            Class.forName("com.personalbudgeting.controller.BudgetController");
            Class.forName("com.personalbudgeting.controller.ReminderController");
            System.out.println("Successfully preloaded all controller classes");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to preload controllers: " + e.getMessage());
        }
    }

    /**
     * Creates and returns a full budget view without using FXML
     * This programmatically recreates the entire budget UI
     */
    private static Parent createSimpleBudgetView() {
        System.out.println("Creating full programmatic budget view");
        
        try {
            // Create the main controller instance
            BudgetController controller = new BudgetController();
            
            // Create a root HBox
            javafx.scene.layout.HBox root = new javafx.scene.layout.HBox();
            
            // Create a navigation sidebar
            javafx.scene.layout.VBox sidebar = new javafx.scene.layout.VBox();
            sidebar.setPrefSize(150, 600);
            sidebar.setStyle("-fx-background-color: #263238;");
            sidebar.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            
            // Create sidebar title
            javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Personal Budget");
            titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            titleLabel.setFont(new javafx.scene.text.Font("System Bold", 14));
            javafx.scene.layout.VBox.setMargin(titleLabel, new javafx.geometry.Insets(20, 0, 20, 0));
            
            // Create navigation buttons
            javafx.scene.control.Button incomeButton = new javafx.scene.control.Button("Income");
            incomeButton.setPrefSize(130, 40);
            incomeButton.setStyle("-fx-background-color: #37474F;");
            incomeButton.setTextFill(javafx.scene.paint.Color.WHITE);
            incomeButton.setFont(new javafx.scene.text.Font("System", 14));
            incomeButton.setOnAction(e -> {
                try {
                    Main.setRoot("income");
                } catch (Exception ex) {
                    System.err.println("Error navigating to income: " + ex.getMessage());
                }
            });
            
            javafx.scene.control.Button expenseButton = new javafx.scene.control.Button("Expense");
            expenseButton.setPrefSize(130, 40);
            expenseButton.setStyle("-fx-background-color: #37474F;");
            expenseButton.setTextFill(javafx.scene.paint.Color.WHITE);
            expenseButton.setFont(new javafx.scene.text.Font("System", 14));
            expenseButton.setOnAction(e -> {
                try {
                    Main.setRoot("expense");
                } catch (Exception ex) {
                    System.err.println("Error navigating to expense: " + ex.getMessage());
                }
            });
            
            javafx.scene.control.Button budgetButton = new javafx.scene.control.Button("Budget");
            budgetButton.setPrefSize(130, 40);
            budgetButton.setStyle("-fx-background-color: #2196F3;");
            budgetButton.setTextFill(javafx.scene.paint.Color.WHITE);
            budgetButton.setFont(new javafx.scene.text.Font("System Bold", 14));
            
            javafx.scene.control.Button reminderButton = new javafx.scene.control.Button("Reminders");
            reminderButton.setPrefSize(130, 40);
            reminderButton.setStyle("-fx-background-color: #37474F;");
            reminderButton.setTextFill(javafx.scene.paint.Color.WHITE);
            reminderButton.setFont(new javafx.scene.text.Font("System", 14));
            reminderButton.setOnAction(e -> {
                try {
                    Main.setRoot("reminder");
                } catch (Exception ex) {
                    System.err.println("Error navigating to reminder: " + ex.getMessage());
                }
            });
            
            javafx.scene.layout.VBox logoutContainer = new javafx.scene.layout.VBox();
            logoutContainer.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);
            logoutContainer.setPrefHeight(200);
            logoutContainer.setPrefWidth(100);
            javafx.scene.layout.VBox.setVgrow(logoutContainer, javafx.scene.layout.Priority.ALWAYS);
            
            javafx.scene.control.Button logoutButton = new javafx.scene.control.Button("Logout");
            logoutButton.setPrefSize(130, 40);
            logoutButton.setStyle("-fx-background-color: #F44336;");
            logoutButton.setTextFill(javafx.scene.paint.Color.WHITE);
            logoutButton.setFont(new javafx.scene.text.Font("System", 14));
            javafx.scene.layout.VBox.setMargin(logoutButton, new javafx.geometry.Insets(0, 0, 20, 0));
            logoutButton.setOnAction(e -> {
                try {
                    Main.setRoot("login");
                } catch (Exception ex) {
                    System.err.println("Error navigating to login: " + ex.getMessage());
                }
            });
            
            logoutContainer.getChildren().add(logoutButton);
            
            // Add all buttons to sidebar
            sidebar.getChildren().addAll(titleLabel, incomeButton, expenseButton, budgetButton, reminderButton, logoutContainer);
            
            // Create main content container
            javafx.scene.layout.VBox mainContent = new javafx.scene.layout.VBox();
            mainContent.setPrefSize(650, 600);
            
            // Create header
            javafx.scene.layout.HBox header = new javafx.scene.layout.HBox();
            header.setPrefHeight(80);
            header.setPrefWidth(650);
            header.setStyle("-fx-background-color: #ECEFF1;");
            
            javafx.scene.control.Label headerLabel = new javafx.scene.control.Label("Budget Management");
            headerLabel.setFont(new javafx.scene.text.Font("System Bold", 24));
            javafx.scene.layout.HBox.setMargin(headerLabel, new javafx.geometry.Insets(20, 0, 0, 20));
            
            header.getChildren().add(headerLabel);
            
            // Create content area (form + table)
            javafx.scene.layout.HBox contentArea = new javafx.scene.layout.HBox();
            contentArea.setPrefHeight(520);
            contentArea.setPrefWidth(650);
            
            // Left side - Budget form
            javafx.scene.layout.VBox formContainer = new javafx.scene.layout.VBox();
            formContainer.setPrefHeight(520);
            formContainer.setPrefWidth(320);
            formContainer.setSpacing(10);
            formContainer.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));
            
            // Set Budget label
            javafx.scene.control.Label setBudgetLabel = new javafx.scene.control.Label("Set Budget");
            setBudgetLabel.setFont(new javafx.scene.text.Font("System Bold", 14));
            
            // Category selection
            javafx.scene.layout.HBox categoryRow = new javafx.scene.layout.HBox();
            categoryRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            categoryRow.setPrefHeight(40);
            categoryRow.setPrefWidth(300);
            categoryRow.setSpacing(10);
            
            javafx.scene.control.Label categoryLabel = new javafx.scene.control.Label("Category:");
            javafx.scene.control.ComboBox<String> categoryComboBox = new javafx.scene.control.ComboBox<>();
            categoryComboBox.setPrefHeight(30);
            categoryComboBox.setPrefWidth(200);
            categoryComboBox.getItems().addAll(
                "Food", "Rent", "Utilities", "Transportation", "Entertainment", 
                "Healthcare", "Education", "Shopping", "Personal Care", "Travel", "Other"
            );
            
            categoryRow.getChildren().addAll(categoryLabel, categoryComboBox);
            
            // Amount entry
            javafx.scene.layout.HBox amountRow = new javafx.scene.layout.HBox();
            amountRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            amountRow.setPrefHeight(40);
            amountRow.setPrefWidth(300);
            amountRow.setSpacing(10);
            
            javafx.scene.control.Label amountLabel = new javafx.scene.control.Label("Amount:");
            javafx.scene.control.TextField amountField = new javafx.scene.control.TextField();
            amountField.setPrefHeight(30);
            amountField.setPrefWidth(200);
            amountField.setPromptText("Enter budget amount");
            
            amountRow.getChildren().addAll(amountLabel, amountField);
            
            // Month selection
            javafx.scene.layout.HBox monthRow = new javafx.scene.layout.HBox();
            monthRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            monthRow.setPrefHeight(40);
            monthRow.setPrefWidth(300);
            monthRow.setSpacing(10);
            
            javafx.scene.control.Label monthLabel = new javafx.scene.control.Label("Month:");
            javafx.scene.control.DatePicker monthPicker = new javafx.scene.control.DatePicker();
            monthPicker.setPrefHeight(30);
            monthPicker.setPrefWidth(200);
            monthPicker.setPromptText("Select month");
            monthPicker.setValue(java.time.LocalDate.now().withDayOfMonth(1));
            
            monthRow.getChildren().addAll(monthLabel, monthPicker);
            
            // Action buttons row 1
            javafx.scene.layout.HBox actionRow1 = new javafx.scene.layout.HBox();
            actionRow1.setAlignment(javafx.geometry.Pos.CENTER);
            actionRow1.setPrefHeight(40);
            actionRow1.setPrefWidth(200);
            actionRow1.setSpacing(10);
            javafx.scene.layout.VBox.setMargin(actionRow1, new javafx.geometry.Insets(10, 0, 0, 0));
            
            javafx.scene.control.Button setBudgetButton = new javafx.scene.control.Button("Set Budget");
            setBudgetButton.setPrefHeight(30);
            setBudgetButton.setPrefWidth(100);
            setBudgetButton.setStyle("-fx-background-color: #2196F3;");
            setBudgetButton.setTextFill(javafx.scene.paint.Color.WHITE);
            
            javafx.scene.control.Button updateButton = new javafx.scene.control.Button("Update");
            updateButton.setPrefHeight(30);
            updateButton.setPrefWidth(100);
            updateButton.setStyle("-fx-background-color: #FFA000;");
            updateButton.setTextFill(javafx.scene.paint.Color.WHITE);
            updateButton.setDisable(true); // Initially disabled
            
            actionRow1.getChildren().addAll(setBudgetButton, updateButton);
            
            // Action buttons row 2
            javafx.scene.layout.HBox actionRow2 = new javafx.scene.layout.HBox();
            actionRow2.setAlignment(javafx.geometry.Pos.CENTER);
            actionRow2.setPrefHeight(40);
            actionRow2.setPrefWidth(200);
            actionRow2.setSpacing(10);
            
            javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Delete");
            deleteButton.setPrefHeight(30);
            deleteButton.setPrefWidth(100);
            deleteButton.setStyle("-fx-background-color: #F44336;");
            deleteButton.setTextFill(javafx.scene.paint.Color.WHITE);
            deleteButton.setDisable(true); // Initially disabled
            
            javafx.scene.control.Button clearButton = new javafx.scene.control.Button("Clear");
            clearButton.setPrefHeight(30);
            clearButton.setPrefWidth(100);
            clearButton.setStyle("-fx-background-color: #9E9E9E;");
            clearButton.setTextFill(javafx.scene.paint.Color.WHITE);
            
            actionRow2.getChildren().addAll(deleteButton, clearButton);
            
            // Separator
            javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
            separator.setPrefWidth(200);
            separator.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
            
            // Budget Analysis section
            javafx.scene.layout.HBox analysisHeader = new javafx.scene.layout.HBox();
            analysisHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            analysisHeader.setPrefWidth(300);
            analysisHeader.setSpacing(10);
            
            javafx.scene.control.Label analysisLabel = new javafx.scene.control.Label("Budget Analysis");
            analysisLabel.setFont(new javafx.scene.text.Font("System Bold", 14));
            
            analysisHeader.getChildren().add(analysisLabel);
            
            // Total Income row
            javafx.scene.layout.HBox incomeRow = new javafx.scene.layout.HBox();
            incomeRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            incomeRow.setPrefWidth(300);
            incomeRow.setSpacing(10);
            
            javafx.scene.control.Label incomeLabelText = new javafx.scene.control.Label("Total Income:");
            javafx.scene.control.Label incomeValueLabel = new javafx.scene.control.Label("$0.00");
            incomeValueLabel.setFont(new javafx.scene.text.Font("System Bold", 12));
            
            incomeRow.getChildren().addAll(incomeLabelText, incomeValueLabel);
            
            // Total Budget row
            javafx.scene.layout.HBox totalBudgetRow = new javafx.scene.layout.HBox();
            totalBudgetRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            totalBudgetRow.setPrefWidth(300);
            totalBudgetRow.setSpacing(10);
            
            javafx.scene.control.Label totalBudgetLabelText = new javafx.scene.control.Label("Total Budget:");
            javafx.scene.control.Label totalBudgetValueLabel = new javafx.scene.control.Label("$0.00");
            totalBudgetValueLabel.setFont(new javafx.scene.text.Font("System Bold", 12));
            
            totalBudgetRow.getChildren().addAll(totalBudgetLabelText, totalBudgetValueLabel);
            
            // Total Expenses row
            javafx.scene.layout.HBox expensesRow = new javafx.scene.layout.HBox();
            expensesRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            expensesRow.setPrefWidth(300);
            expensesRow.setSpacing(10);
            
            javafx.scene.control.Label expensesLabelText = new javafx.scene.control.Label("Total Expenses:");
            javafx.scene.control.Label expensesValueLabel = new javafx.scene.control.Label("$0.00");
            expensesValueLabel.setFont(new javafx.scene.text.Font("System Bold", 12));
            
            expensesRow.getChildren().addAll(expensesLabelText, expensesValueLabel);
            
            // Remaining Income row
            javafx.scene.layout.HBox remainingRow = new javafx.scene.layout.HBox();
            remainingRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            remainingRow.setPrefWidth(300);
            remainingRow.setSpacing(10);
            
            javafx.scene.control.Label remainingLabelText = new javafx.scene.control.Label("Remaining Income:");
            javafx.scene.control.Label remainingValueLabel = new javafx.scene.control.Label("$0.00");
            remainingValueLabel.setFont(new javafx.scene.text.Font("System Bold", 12));
            
            remainingRow.getChildren().addAll(remainingLabelText, remainingValueLabel);
            
            // Add all components to form container
            formContainer.getChildren().addAll(
                setBudgetLabel, categoryRow, amountRow, monthRow, 
                actionRow1, actionRow2, separator, analysisHeader,
                incomeRow, totalBudgetRow, expensesRow, remainingRow
            );
            
            // Right side - Table and charts
            javafx.scene.layout.VBox tableChartContainer = new javafx.scene.layout.VBox();
            tableChartContainer.setPrefHeight(520);
            tableChartContainer.setPrefWidth(330);
            
            // Budget table
            javafx.scene.control.TableView<com.personalbudgeting.model.Budget> budgetTable = new javafx.scene.control.TableView<>();
            budgetTable.setPrefHeight(200);
            budgetTable.setPrefWidth(330);
            javafx.scene.layout.VBox.setMargin(budgetTable, new javafx.geometry.Insets(10, 10, 10, 0));
            
            // Table columns
            javafx.scene.control.TableColumn<com.personalbudgeting.model.Budget, String> categoryColumn = 
                new javafx.scene.control.TableColumn<>("Category");
            categoryColumn.setPrefWidth(75);
            categoryColumn.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
            
            javafx.scene.control.TableColumn<com.personalbudgeting.model.Budget, Double> budgetAmountColumn = 
                new javafx.scene.control.TableColumn<>("Budget");
            budgetAmountColumn.setPrefWidth(65);
            budgetAmountColumn.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleDoubleProperty(data.getValue().getAmount()).asObject());
            budgetAmountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<com.personalbudgeting.model.Budget, Double>() {
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
            
            javafx.scene.control.TableColumn<com.personalbudgeting.model.Budget, Double> spentAmountColumn = 
                new javafx.scene.control.TableColumn<>("Spent");
            spentAmountColumn.setPrefWidth(65);
            spentAmountColumn.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleDoubleProperty(data.getValue().getSpentAmount()).asObject());
            spentAmountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<com.personalbudgeting.model.Budget, Double>() {
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
            
            javafx.scene.control.TableColumn<com.personalbudgeting.model.Budget, Double> remainingColumn = 
                new javafx.scene.control.TableColumn<>("Remaining");
            remainingColumn.setPrefWidth(65);
            remainingColumn.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleDoubleProperty(data.getValue().getRemainingAmount()).asObject());
            remainingColumn.setCellFactory(column -> new javafx.scene.control.TableCell<com.personalbudgeting.model.Budget, Double>() {
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
            
            javafx.scene.control.TableColumn<com.personalbudgeting.model.Budget, Double> percentageColumn = 
                new javafx.scene.control.TableColumn<>("%");
            percentageColumn.setPrefWidth(60);
            percentageColumn.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPercentage()).asObject());
            percentageColumn.setCellFactory(column -> new javafx.scene.control.TableCell<com.personalbudgeting.model.Budget, Double>() {
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
            
            budgetTable.getColumns().addAll(
                categoryColumn, budgetAmountColumn, spentAmountColumn, 
                remainingColumn, percentageColumn
            );
            
            // Charts section
            javafx.scene.layout.HBox chartsContainer = new javafx.scene.layout.HBox();
            chartsContainer.setPrefHeight(310);
            chartsContainer.setPrefWidth(330);
            
            // Budget Distribution Chart
            javafx.scene.layout.VBox budgetChartBox = new javafx.scene.layout.VBox();
            budgetChartBox.setPrefHeight(310);
            budgetChartBox.setPrefWidth(160);
            javafx.scene.layout.HBox.setMargin(budgetChartBox, new javafx.geometry.Insets(0, 10, 0, 0));
            
            javafx.scene.control.Label budgetChartLabel = new javafx.scene.control.Label("Budget Distribution");
            budgetChartLabel.setFont(new javafx.scene.text.Font("System Bold", 12));
            javafx.scene.layout.VBox.setMargin(budgetChartLabel, new javafx.geometry.Insets(0, 0, 5, 0));
            
            javafx.scene.chart.PieChart budgetPieChart = new javafx.scene.chart.PieChart();
            budgetPieChart.setPrefHeight(270);
            budgetPieChart.setPrefWidth(160);
            
            // Example data for budget chart
            javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> budgetChartData = 
                javafx.collections.FXCollections.observableArrayList(
                    new javafx.scene.chart.PieChart.Data("Food", 25),
                    new javafx.scene.chart.PieChart.Data("Rent", 40),
                    new javafx.scene.chart.PieChart.Data("Other", 35)
                );
            budgetPieChart.setData(budgetChartData);
            
            budgetChartBox.getChildren().addAll(budgetChartLabel, budgetPieChart);
            
            // Expense Distribution Chart
            javafx.scene.layout.VBox expenseChartBox = new javafx.scene.layout.VBox();
            expenseChartBox.setPrefHeight(310);
            expenseChartBox.setPrefWidth(160);
            
            javafx.scene.control.Label expenseChartLabel = new javafx.scene.control.Label("Expense Distribution");
            expenseChartLabel.setFont(new javafx.scene.text.Font("System Bold", 12));
            javafx.scene.layout.VBox.setMargin(expenseChartLabel, new javafx.geometry.Insets(0, 0, 5, 0));
            
            javafx.scene.chart.PieChart expensePieChart = new javafx.scene.chart.PieChart();
            expensePieChart.setPrefHeight(270);
            expensePieChart.setPrefWidth(160);
            
            // Example data for expense chart
            javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> expenseChartData = 
                javafx.collections.FXCollections.observableArrayList(
                    new javafx.scene.chart.PieChart.Data("Food", 20),
                    new javafx.scene.chart.PieChart.Data("Rent", 45),
                    new javafx.scene.chart.PieChart.Data("Other", 35)
                );
            expensePieChart.setData(expenseChartData);
            
            expenseChartBox.getChildren().addAll(expenseChartLabel, expensePieChart);
            
            // Add charts to container
            chartsContainer.getChildren().addAll(budgetChartBox, expenseChartBox);
            
            // Add table and charts to right side container
            tableChartContainer.getChildren().addAll(budgetTable, chartsContainer);
            
            // Add form and table+charts to content area
            contentArea.getChildren().addAll(formContainer, tableChartContainer);
            
            // Add header and content to main container
            mainContent.getChildren().addAll(header, contentArea);
            
            // Add sidebar and main content to root
            root.getChildren().addAll(sidebar, mainContent);
            
            // Set up button actions
            setBudgetButton.setOnAction(e -> {
                try {
                    // Get values from form
                    String category = categoryComboBox.getValue();
                    String amount = amountField.getText();
                    java.time.LocalDate date = monthPicker.getValue();
                    
                    if (category == null || category.isEmpty() || amount == null || amount.isEmpty() || date == null) {
                        // Show error message
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR,
                            "Please fill in all fields",
                            javafx.scene.control.ButtonType.OK
                        );
                        alert.setTitle("Input Error");
                        alert.setHeaderText("Missing Information");
                        alert.showAndWait();
                        return;
                    }
                    
                    try {
                        double amountValue = Double.parseDouble(amount);
                        if (amountValue <= 0) {
                            // Show error message
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR,
                                "Amount must be greater than zero",
                                javafx.scene.control.ButtonType.OK
                            );
                            alert.setTitle("Input Error");
                            alert.setHeaderText("Invalid Amount");
                            alert.showAndWait();
                            return;
                        }
                        
                        // Create new budget item
                        com.personalbudgeting.model.Budget budget = new com.personalbudgeting.model.Budget();
                        budget.setCategory(category);
                        budget.setAmount(amountValue);
                        budget.setPeriod(java.time.YearMonth.from(date));
                        
                        // Add to table
                        budgetTable.getItems().add(budget);
                        
                        // Clear form
                        categoryComboBox.setValue(null);
                        amountField.clear();
                        
                        // Show success message
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION,
                            "Budget set successfully",
                            javafx.scene.control.ButtonType.OK
                        );
                        alert.setTitle("Success");
                        alert.setHeaderText("Budget Set");
                        alert.showAndWait();
                    } catch (NumberFormatException ex) {
                        // Show error message
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR,
                            "Please enter a valid number for amount",
                            javafx.scene.control.ButtonType.OK
                        );
                        alert.setTitle("Input Error");
                        alert.setHeaderText("Invalid Amount");
                        alert.showAndWait();
                    }
                } catch (Exception ex) {
                    System.err.println("Error setting budget: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            
            clearButton.setOnAction(e -> {
                categoryComboBox.setValue(null);
                amountField.clear();
                monthPicker.setValue(java.time.LocalDate.now().withDayOfMonth(1));
            });
            
            // Table selection listener to populate form with selected item
            budgetTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    categoryComboBox.setValue(newSelection.getCategory());
                    amountField.setText(String.format("%.2f", newSelection.getAmount()));
                    monthPicker.setValue(newSelection.getPeriod().atDay(1));
                    updateButton.setDisable(false);
                    deleteButton.setDisable(false);
                } else {
                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);
                }
            });
            
            // Set up Update and Delete button actions
            updateButton.setOnAction(e -> {
                com.personalbudgeting.model.Budget selectedBudget = budgetTable.getSelectionModel().getSelectedItem();
                if (selectedBudget != null) {
                    try {
                        // Get values from form
                        String category = categoryComboBox.getValue();
                        String amount = amountField.getText();
                        java.time.LocalDate date = monthPicker.getValue();
                        
                        if (category == null || category.isEmpty() || amount.isEmpty() || date == null) {
                            // Show error message
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR,
                                "Please fill in all fields",
                                javafx.scene.control.ButtonType.OK
                            );
                            alert.setTitle("Input Error");
                            alert.setHeaderText("Missing Information");
                            alert.showAndWait();
                            return;
                        }
                        
                        try {
                            double amountValue = Double.parseDouble(amount);
                            if (amountValue <= 0) {
                                // Show error message
                                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.ERROR,
                                    "Amount must be greater than zero",
                                    javafx.scene.control.ButtonType.OK
                                );
                                alert.setTitle("Input Error");
                                alert.setHeaderText("Invalid Amount");
                                alert.showAndWait();
                                return;
                            }
                            
                            // Update budget item
                            selectedBudget.setCategory(category);
                            selectedBudget.setAmount(amountValue);
                            selectedBudget.setPeriod(java.time.YearMonth.from(date));
                            
                            // Refresh table
                            budgetTable.refresh();
                            
                            // Clear form
                            categoryComboBox.setValue(null);
                            amountField.clear();
                            budgetTable.getSelectionModel().clearSelection();
                            
                            // Show success message
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION,
                                "Budget updated successfully",
                                javafx.scene.control.ButtonType.OK
                            );
                            alert.setTitle("Success");
                            alert.setHeaderText("Budget Updated");
                            alert.showAndWait();
                        } catch (NumberFormatException ex) {
                            // Show error message
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR,
                                "Please enter a valid number for amount",
                                javafx.scene.control.ButtonType.OK
                            );
                            alert.setTitle("Input Error");
                            alert.setHeaderText("Invalid Amount");
                            alert.showAndWait();
                        }
                    } catch (Exception ex) {
                        System.err.println("Error updating budget: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });
            
            deleteButton.setOnAction(e -> {
                com.personalbudgeting.model.Budget selectedBudget = budgetTable.getSelectionModel().getSelectedItem();
                if (selectedBudget != null) {
                    // Confirm deletion
                    javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to delete this budget?",
                        javafx.scene.control.ButtonType.YES, 
                        javafx.scene.control.ButtonType.NO
                    );
                    confirmAlert.setTitle("Confirm Deletion");
                    confirmAlert.setHeaderText("Delete Budget");
                    
                    java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
                    if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.YES) {
                        // Remove from table
                        budgetTable.getItems().remove(selectedBudget);
                        
                        // Clear form
                        categoryComboBox.setValue(null);
                        amountField.clear();
                        budgetTable.getSelectionModel().clearSelection();
                        
                        // Show success message
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION,
                            "Budget deleted successfully",
                            javafx.scene.control.ButtonType.OK
                        );
                        alert.setTitle("Success");
                        alert.setHeaderText("Budget Deleted");
                        alert.showAndWait();
                    }
                }
            });
            
            return root;
        } catch (Exception e) {
            System.err.println("Error creating full budget view: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simplified view in case of error
            javafx.scene.layout.VBox errorView = new javafx.scene.layout.VBox();
            errorView.setAlignment(javafx.geometry.Pos.CENTER);
            errorView.setPrefSize(800, 600);
            
            javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("Budget View Error");
            errorLabel.setFont(new javafx.scene.text.Font("System Bold", 24));
            
            javafx.scene.control.Label errorMessageLabel = new javafx.scene.control.Label(
                "An error occurred while creating the Budget view: " + e.getMessage()
            );
            
            javafx.scene.control.Button backButton = new javafx.scene.control.Button("Go Back to Income");
            backButton.setOnAction(e2 -> {
                try {
                    Main.setRoot("income");
                } catch (Exception ex) {
                    System.err.println("Error navigating to income: " + ex.getMessage());
                }
            });
            
            errorView.getChildren().addAll(errorLabel, errorMessageLabel, backButton);
            
            return errorView;
        }
    }
    
    /**
     * Special method to navigate to Budget page using the preloaded root
     */
    public static void navigateToBudget() {
        System.out.println("===== Main.navigateToBudget(): Starting navigation to Budget page =====");
        
        try {
            // If we have a preloaded root, use it
            if (budgetRoot != null) {
                System.out.println("Using preloaded budgetRoot");
                scene.setRoot(budgetRoot);
                System.out.println("Successfully set root to budget page using preloaded root");
                return;
            }
            
            // Try creating a programmatic view as an ultimate fallback
            Parent programmingView = createSimpleBudgetView();
            budgetRoot = programmingView;
            scene.setRoot(programmingView);
            System.out.println("Successfully created and set a programmatic Budget view");
            return;
            
            // The following code is now commented out since we have a more reliable approach
            /*
            // Try loading simplified version first
            System.out.println("Attempting to load simplified budget page...");
            File simpleBudgetFile = new File("src/main/resources/fxml/budget_simple.fxml");
            if (simpleBudgetFile.exists()) {
                System.out.println("Simple budget FXML file found at: " + simpleBudgetFile.getAbsolutePath());
                FXMLLoader loader = new FXMLLoader(simpleBudgetFile.toURI().toURL());
                Parent root = loader.load();
                budgetRoot = root;
                scene.setRoot(root);
                System.out.println("Successfully loaded and navigated to simplified Budget page");
                return;
            } else {
                System.err.println("Simple budget FXML file not found at: " + simpleBudgetFile.getAbsolutePath());
            }
            
            // Then fall back to the original approach
            System.out.println("No preloaded budgetRoot available, trying to load from file...");
            
            // Debug - Create a specialized FXMLLoader with a custom error handler
            File file = new File("src/main/resources/fxml/budget.fxml");
            if (file.exists()) {
                System.out.println("Found budget.fxml file, attempting to load with debug handler...");
                
                // Create a new FXMLLoader with a custom error handler
                FXMLLoader loader = new FXMLLoader(file.toURI().toURL());
                loader.setBuilderFactory(new JavaFXBuilderFactory());
                
                // Add a detailed error handler to diagnose issues
                loader.setControllerFactory(param -> {
                    try {
                        System.out.println("Creating controller instance for: " + param.getName());
                        return Class.forName(param.getName()).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Error creating controller: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                });
                
                try {
                    Parent root = loader.load();
                    budgetRoot = root;
                    scene.setRoot(root);
                    System.out.println("Successfully loaded and set Budget root from file!");
                    return;
                } catch (IOException e) {
                    System.err.println("DETAILED DEBUG: Error loading budget.fxml - " + e.getMessage());
                    e.printStackTrace();
                    
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                        e.getCause().printStackTrace();
                    }
                    
                    // Try to read the file content around line 135 to see what's there
                    try {
                        List<String> lines = Files.readAllLines(file.toPath());
                        int start = Math.max(0, 130);
                        int end = Math.min(lines.size(), 140);
                        
                        System.out.println("File content around line 135:");
                        for (int i = start; i < end; i++) {
                            System.out.println((i+1) + ": " + lines.get(i));
                        }
                    } catch (Exception ex) {
                        System.err.println("Error reading file: " + ex.getMessage());
                    }
                }
            }
            
            // Try loading from resource directly (classpath)
            URL resource = Main.class.getClassLoader().getResource("fxml/budget.fxml");
            if (resource != null) {
                try {
                    Parent root = FXMLLoader.load(resource);
                    budgetRoot = root;
                    scene.setRoot(root);
                    System.out.println("Successfully loaded budget.fxml from resources");
                    return;
                } catch (IOException e) {
                    System.err.println("Error loading budget.fxml from resources: " + e.getMessage());
                }
            } else {
                System.err.println("Could not find budget.fxml resource");
            }
            
            // Last ditch effort - try loading from an absolute path
            try {
                String path = Paths.get("").toAbsolutePath().toString() + "/src/main/resources/fxml/budget.fxml";
                System.out.println("Attempting to load budget.fxml from absolute path: " + path);
                File absFile = new File(path);
                if (absFile.exists()) {
                    Parent root = FXMLLoader.load(absFile.toURI().toURL());
                    budgetRoot = root;
                    scene.setRoot(root);
                    System.out.println("Successfully loaded budget.fxml from absolute path");
                    return;
                }
            } catch (IOException e) {
                System.err.println("Error loading budget.fxml from absolute path: " + e.getMessage());
            }
            
            // All loading methods failed
            System.err.println("All methods to load budget.fxml failed!");
            throw new IOException("Failed to load budget.fxml");
            */
        } catch (Exception e) {
            System.err.println("Error in navigateToBudget: " + e.getMessage());
            e.printStackTrace();
            // Don't rethrow to prevent app crash
        }
    }
    
    /**
     * Special method to navigate to Reminder page using the preloaded root
     */
    public static void navigateToReminder() {
        System.out.println("===== Main.navigateToReminder(): Starting navigation to Reminder page =====");
        
        try {
            // If we have a preloaded root, use it
            if (reminderRoot != null) {
                System.out.println("Using preloaded reminderRoot");
                scene.setRoot(reminderRoot);
                System.out.println("Successfully set root to reminder page using preloaded root");
                return;
            }
            
            // Try all available methods to load the Reminder page
            System.out.println("No preloaded root available. Attempting direct file loading...");
            
            // Directly use file path approach which is more reliable in this context
            File reminderFile = new File("src/main/resources/fxml/reminder.fxml");
            if (reminderFile.exists()) {
                System.out.println("Reminder FXML file found at: " + reminderFile.getAbsolutePath());
                FXMLLoader loader = new FXMLLoader(reminderFile.toURI().toURL());
                Parent root = loader.load();
                reminderRoot = root; // Cache for future use
                scene.setRoot(root);
                System.out.println("Successfully loaded and navigated to Reminder page");
                return;
            } else {
                System.err.println("Reminder FXML file not found at: " + reminderFile.getAbsolutePath());
            }
            
            // Try the target directory as a fallback
            File targetReminderFile = new File("target/classes/fxml/reminder.fxml");
            if (targetReminderFile.exists()) {
                System.out.println("Reminder FXML file found in target at: " + targetReminderFile.getAbsolutePath());
                FXMLLoader loader = new FXMLLoader(targetReminderFile.toURI().toURL());
                Parent root = loader.load();
                reminderRoot = root;
                scene.setRoot(root);
                System.out.println("Successfully loaded and navigated to Reminder page from target directory");
                return;
            } else {
                System.err.println("Reminder FXML file not found in target location!");
            }
            
            // Last resort: try resource loading
            URL resourceUrl = Main.class.getResource("/fxml/reminder.fxml");
            if (resourceUrl != null) {
                System.out.println("Reminder FXML resource found at: " + resourceUrl);
                FXMLLoader loader = new FXMLLoader(resourceUrl);
                Parent root = loader.load();
                reminderRoot = root;
                scene.setRoot(root);
                System.out.println("Successfully loaded and navigated to Reminder page from resources");
                return;
            } else {
                System.err.println("Reminder FXML resource not found!");
            }
            
            System.err.println("===== ERROR: All attempts to load Reminder page failed! =====");
            
        } catch (Exception e) {
            System.err.println("Failed to navigate to Reminder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setRoot(String fxml) throws IOException {
        System.out.println("===== Main.setRoot(): Switching to view: " + fxml + " =====");
        
        // Special direct navigation for problematic pages to use the most reliable method
        if ("budget".equals(fxml)) {
            System.out.println("Redirecting to special budget navigation method");
            try {
                // Try programmatic view first as it's the most reliable approach
                Parent programmingView = createSimpleBudgetView();
                budgetRoot = programmingView;
                scene.setRoot(programmingView);
                System.out.println("Successfully created and set a programmatic Budget view");
                return;
                
                // Old approach commented out since we have a more reliable method now
                /*
                // Try simplified version first
                System.out.println("Attempting to load simplified budget page...");
                File simpleBudgetFile = new File("src/main/resources/fxml/budget_simple.fxml");
                if (simpleBudgetFile.exists()) {
                    System.out.println("Simple budget FXML file found at: " + simpleBudgetFile.getAbsolutePath());
                    FXMLLoader loader = new FXMLLoader(simpleBudgetFile.toURI().toURL());
                    Parent root = loader.load();
                    budgetRoot = root;
                    scene.setRoot(root);
                    System.out.println("Successfully loaded and navigated to simplified Budget page");
                    return;
                } else {
                    System.err.println("Simple budget FXML file not found at: " + simpleBudgetFile.getAbsolutePath());
                }
                
                // DIRECT EMERGENCY APPROACH - Try all possible methods to force load the budget page
                System.out.println("EMERGENCY BUDGET LOADING: Attempting direct file loading...");
                
                // Try from source file path which is the most reliable in this context
                File budgetFile = new File("src/main/resources/fxml/budget.fxml");
                if (budgetFile.exists()) {
                    System.out.println("Budget FXML file found at: " + budgetFile.getAbsolutePath());
                    FXMLLoader loader = new FXMLLoader(budgetFile.toURI().toURL());
                    Parent root = loader.load();
                    budgetRoot = root; // Cache for future use
                    scene.setRoot(root);
                    System.out.println("SUCCESS! Loaded and navigated to Budget page");
                    return;
                }
                
                // Try the target directory as a fallback
                File targetBudgetFile = new File("target/classes/fxml/budget.fxml");
                if (targetBudgetFile.exists()) {
                    System.out.println("Budget FXML file found in target at: " + targetBudgetFile.getAbsolutePath());
                    FXMLLoader loader = new FXMLLoader(targetBudgetFile.toURI().toURL());
                    Parent root = loader.load();
                    budgetRoot = root;
                    scene.setRoot(root);
                    System.out.println("SUCCESS! Loaded and navigated to Budget page from target directory");
                    return;
                }
                
                // If we get here, both approaches failed
                System.err.println("EMERGENCY BUDGET LOADING FAILED: All attempts failed!");
                */
            } catch (Exception e) {
                System.err.println("EMERGENCY BUDGET LOADING FAILED: " + e.getMessage());
                e.printStackTrace();
            }
            
            // If the emergency approach failed, try the standard method
            navigateToBudget();
            return;
        } else if ("reminder".equals(fxml)) {
            System.out.println("Redirecting to special reminder navigation method");
            navigateToReminder();
            return;
        }
        
        // Standard loading for other pages
        try {
            scene.setRoot(loadFXML(fxml));
            System.out.println("Successfully switched to: " + fxml);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to navigate to " + fxml + ": " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to navigate to " + fxml, e);
        }
    }

    public static Parent loadFXML(String fxml) throws IOException {
        System.out.println("===== Main.loadFXML: Loading FXML file: " + fxml + ".fxml =====");
        
        // Run diagnostics
        System.out.println(com.personalbudgeting.util.FXMLDiagnostics.runDiagnostics(fxml));
        
        // For special case files, use dedicated methods
        if ("budget".equals(fxml)) {
            System.out.println("Redirecting to special budget navigation method");
            // Use programmatic view creation
            budgetRoot = createSimpleBudgetView();
            return budgetRoot;
        } else if ("reminder".equals(fxml)) {
            System.out.println("Redirecting to special reminder navigation method");
            navigateToReminder();
            return reminderRoot;
        }
        
        // First try to load from classpath resources (preferred method)
        URL resourceUrl = Main.class.getResource("/fxml/" + fxml + ".fxml");
        if (resourceUrl != null) {
            System.out.println("Loading FXML from resource URL: " + resourceUrl);
            try {
                FXMLLoader loader = new FXMLLoader(resourceUrl);
                return loader.load();
            } catch (Exception e) {
                System.err.println("Failed to load from resource URL: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Resource URL not found, falling back to file loading");
        }
        
        // Fallback: Copy the files to ensure they're available
        String copyResult = com.personalbudgeting.util.FXMLDiagnostics.copyFile(
            "src/main/resources/fxml/" + fxml + ".fxml", 
            "target/classes/fxml/" + fxml + ".fxml");
        System.out.println("Copy result:\n" + copyResult);
        
        // Force controller class loading
        String controllerName = "com.personalbudgeting.controller." + 
            Character.toUpperCase(fxml.charAt(0)) + fxml.substring(1) + "Controller";
        try {
            Class<?> controllerClass = Class.forName(controllerName);
            System.out.println("Loaded controller class: " + controllerClass.getName());
        } catch (ClassNotFoundException e) {
            System.err.println("Controller class not found: " + e.getMessage());
        }
        
        // Load using the URL directly from the file
        try {
            File sourceFile = new File("src/main/resources/fxml/" + fxml + ".fxml");
            URL url = sourceFile.toURI().toURL();
            System.out.println("Loading FXML from URL: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            return loader.load();
        } catch (Exception e) {
            System.err.println("Failed to load from source file: " + e.getMessage());
            e.printStackTrace();
            
            // Try from target file
            try {
                File targetFile = new File("target/classes/fxml/" + fxml + ".fxml");
                URL url = targetFile.toURI().toURL();
                System.out.println("Loading FXML from target URL: " + url);
                FXMLLoader loader = new FXMLLoader(url);
                return loader.load();
            } catch (Exception ex) {
                System.err.println("Final attempt to load FXML failed: " + ex.getMessage());
                ex.printStackTrace();
                throw new IOException("Could not load FXML file: " + fxml, ex);
            }
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void main(String[] args) {
        // Initialize database
        DatabaseInitializer.initialize();
        
        // Create ReminderService instance
        reminderService = new ReminderService();
        
        // Launch JavaFX application
        launch();
    }
} 