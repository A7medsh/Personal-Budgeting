package com.personalbudgeting.util;

import com.personalbudgeting.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FXMLUtil {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    
    /**
     * Load an FXML file using multiple strategies to handle classpath issues
     * 
     * @param fxmlName Name of the FXML file without extension
     * @return Parent node loaded from FXML
     * @throws IOException if loading fails
     */
    public static Parent loadFXML(String fxmlName) throws IOException {
        String fxmlFile = fxmlName + ".fxml";
        
        System.out.println("Loading FXML: " + fxmlFile);
        String controllerName = "com.personalbudgeting.controller." + 
            Character.toUpperCase(fxmlName.charAt(0)) + fxmlName.substring(1) + "Controller";
        
        System.out.println("Looking for controller: " + controllerName);
        
        // Force a check for controller class to ensure it's loaded
        try {
            Class.forName(controllerName);
            System.out.println("Controller class found: " + controllerName);
        } catch (ClassNotFoundException e) {
            System.err.println("Controller class not found: " + e.getMessage());
        }

        // Strategy 1: Try loading from classpath with "/fxml/" prefix
        URL url = Main.class.getResource("/fxml/" + fxmlFile);
        if (url != null) {
            System.out.println("Strategy 1 successful: " + url);
            try {
                FXMLLoader loader = new FXMLLoader(url);
                return loader.load();
            } catch (Exception e) {
                System.err.println("Failed to load with Strategy 1: " + e.getMessage());
            }
        }
        
        // Strategy 2: Try loading from classpath with "fxml/" prefix
        url = Main.class.getClassLoader().getResource("fxml/" + fxmlFile);
        if (url != null) {
            System.out.println("Strategy 2 successful: " + url);
            try {
                FXMLLoader loader = new FXMLLoader(url);
                return loader.load();
            } catch (Exception e) {
                System.err.println("Failed to load with Strategy 2: " + e.getMessage());
            }
        }
        
        // Strategy 3: Try loading from source directory
        File sourceFile = new File("src/main/resources/fxml/" + fxmlFile);
        if (sourceFile.exists()) {
            System.out.println("Strategy 3 successful: " + sourceFile.getAbsolutePath());
            try {
                FXMLLoader loader = new FXMLLoader(sourceFile.toURI().toURL());
                return loader.load();
            } catch (Exception e) {
                System.err.println("Failed to load with Strategy 3: " + e.getMessage());
            }
        }
        
        // Strategy 4: Try loading from target directory
        File targetFile = new File("target/classes/fxml/" + fxmlFile);
        if (targetFile.exists()) {
            System.out.println("Strategy 4 successful: " + targetFile.getAbsolutePath());
            try {
                FXMLLoader loader = new FXMLLoader(targetFile.toURI().toURL());
                return loader.load();
            } catch (Exception e) {
                System.err.println("Failed to load with Strategy 4: " + e.getMessage());
            }
        }
        
        // Strategy 5: Try loading from backup directory
        File backupFile = new File(TEMP_DIR + "/personal_budgeting_fxml/" + fxmlFile);
        if (backupFile.exists()) {
            System.out.println("Strategy 5 successful (backup file): " + backupFile.getAbsolutePath());
            try {
                FXMLLoader loader = new FXMLLoader(backupFile.toURI().toURL());
                return loader.load();
            } catch (Exception e) {
                System.err.println("Failed to load with Strategy 5: " + e.getMessage());
            }
        }
        
        // Strategy 6: Create a temporary file from source and load it
        File sourceFileCopy = new File("src/main/resources/fxml/" + fxmlFile);
        if (sourceFileCopy.exists()) {
            File tempFile = new File(TEMP_DIR, fxmlFile);
            Files.copy(sourceFileCopy.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Strategy 6 successful (temp file): " + tempFile.getAbsolutePath());
            try {
                FXMLLoader loader = new FXMLLoader(tempFile.toURI().toURL());
                return loader.load();
            } catch (Exception e) {
                System.err.println("Failed to load with Strategy 6: " + e.getMessage());
            }
        }
        
        // Strategy 7 (Last resort): Create a new loader with manual controller instantiation
        if (sourceFileCopy.exists()) {
            System.out.println("Strategy 7 - Attempting manual controller instantiation");
            try {
                // Load FXML without controller
                FXMLLoader loader = new FXMLLoader(sourceFileCopy.toURI().toURL());
                loader.setControllerFactory(controllerClass -> {
                    try {
                        System.out.println("Manually creating controller instance for: " + controllerClass.getName());
                        return controllerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Failed to instantiate controller: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                });
                
                return loader.load();
            } catch (Exception e) {
                System.err.println("Failed with Strategy 7: " + e.getMessage());
            }
        }
        
        // If all strategies fail, log available resources
        logAvailableResources(fxmlName);
        
        throw new IOException("Failed to load FXML file: " + fxmlFile);
    }
    
    /**
     * Log available resources to help diagnose FXML loading issues
     */
    private static void logAvailableResources(String fxmlName) {
        System.err.println("Failed to load FXML: " + fxmlName + ".fxml");
        System.err.println("Trying to locate available resources...");
        
        // Check if directories exist
        File srcDir = new File("src/main/resources/fxml");
        File targetDir = new File("target/classes/fxml");
        
        System.err.println("Source directory exists: " + srcDir.exists() + " at " + srcDir.getAbsolutePath());
        System.err.println("Target directory exists: " + targetDir.exists() + " at " + targetDir.getAbsolutePath());
        
        // List files in directories
        if (srcDir.exists()) {
            System.err.println("Files in source directory:");
            for (File file : srcDir.listFiles()) {
                System.err.println(" - " + file.getName());
            }
        }
        
        if (targetDir.exists()) {
            System.err.println("Files in target directory:");
            for (File file : targetDir.listFiles()) {
                System.err.println(" - " + file.getName());
            }
        }
    }
    
    /**
     * Create a copy of the FXML files in a temporary location
     */
    public static void createBackupFXMLFiles() {
        try {
            File srcDir = new File("src/main/resources/fxml");
            if (!srcDir.exists()) {
                System.err.println("Source directory doesn't exist!");
                return;
            }
            
            File backupDir = new File(TEMP_DIR, "personal_budgeting_fxml");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            for (File file : srcDir.listFiles()) {
                if (file.getName().endsWith(".fxml")) {
                    File backupFile = new File(backupDir, file.getName());
                    Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Created backup of " + file.getName() + " at " + backupFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating backup FXML files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Special method to ensure Budget and Reminder pages load properly
     * Call this before attempting to navigate to these pages
     */
    public static void fixBudgetAndReminderPages() {
        try {
            String[] problematicPages = {"budget", "reminder"};
            for (String page : problematicPages) {
                String fxmlFile = page + ".fxml";
                System.out.println("Ensuring " + fxmlFile + " is available...");
                
                // Source file
                File sourceFile = new File("src/main/resources/fxml/" + fxmlFile);
                
                // Ensure target directory exists
                File targetDir = new File("target/classes/fxml/");
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                
                // Copy to target directory
                if (sourceFile.exists()) {
                    File targetFile = new File("target/classes/fxml/" + fxmlFile);
                    Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Copied " + fxmlFile + " to target directory");
                }
                
                // Create a known working backup file
                if (sourceFile.exists()) {
                    File backupDir = new File(TEMP_DIR, "personal_budgeting_fxml");
                    if (!backupDir.exists()) {
                        backupDir.mkdirs();
                    }
                    
                    File backupFile = new File(backupDir, fxmlFile);
                    Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Created backup of " + fxmlFile + " at " + backupFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fixing Budget and Reminder pages: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 