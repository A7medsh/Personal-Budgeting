package com.personalbudgeting.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Utility class to diagnose FXML loading issues
 */
public class FXMLDiagnostics {
    
    /**
     * Run diagnostics on FXML files
     * @param fxmlName The name of the FXML file without extension
     * @return Detailed diagnostic information
     */
    public static String runDiagnostics(String fxmlName) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== FXML Diagnostics for: ").append(fxmlName).append(".fxml =====\n");
        
        // Check source directory
        File sourceFile = new File("src/main/resources/fxml/" + fxmlName + ".fxml");
        sb.append("Source file exists: ").append(sourceFile.exists()).append("\n");
        if (sourceFile.exists()) {
            sb.append("  Path: ").append(sourceFile.getAbsolutePath()).append("\n");
            sb.append("  Size: ").append(sourceFile.length()).append(" bytes\n");
            sb.append("  Can read: ").append(sourceFile.canRead()).append("\n");
        }
        
        // Check target directory
        File targetFile = new File("target/classes/fxml/" + fxmlName + ".fxml");
        sb.append("Target file exists: ").append(targetFile.exists()).append("\n");
        if (targetFile.exists()) {
            sb.append("  Path: ").append(targetFile.getAbsolutePath()).append("\n");
            sb.append("  Size: ").append(targetFile.length()).append(" bytes\n");
            sb.append("  Can read: ").append(targetFile.canRead()).append("\n");
        }
        
        // Check classpath resources
        URL resourceUrl = FXMLDiagnostics.class.getResource("/fxml/" + fxmlName + ".fxml");
        sb.append("Resource URL exists: ").append(resourceUrl != null).append("\n");
        if (resourceUrl != null) {
            sb.append("  URL: ").append(resourceUrl.toString()).append("\n");
        }
        
        // Check thread context class loader
        URL contextResourceUrl = Thread.currentThread().getContextClassLoader().getResource("fxml/" + fxmlName + ".fxml");
        sb.append("Thread context resource URL exists: ").append(contextResourceUrl != null).append("\n");
        if (contextResourceUrl != null) {
            sb.append("  URL: ").append(contextResourceUrl.toString()).append("\n");
        }
        
        // List files in the directory
        sb.append("\nFiles in src/main/resources/fxml directory:\n");
        listFilesInDirectory("src/main/resources/fxml", sb);
        
        sb.append("\nFiles in target/classes/fxml directory:\n");
        listFilesInDirectory("target/classes/fxml", sb);
        
        return sb.toString();
    }
    
    private static void listFilesInDirectory(String dirPath, StringBuilder sb) {
        try {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        sb.append("  ").append(file.getName())
                          .append(" (").append(file.length()).append(" bytes)\n");
                    }
                } else {
                    sb.append("  Unable to list files (null result)\n");
                }
            } else {
                sb.append("  Directory does not exist or is not a directory\n");
            }
        } catch (Exception e) {
            sb.append("  Error listing files: ").append(e.getMessage()).append("\n");
        }
    }
    
    /**
     * Copy a file from source to target
     * @param sourcePath The source path
     * @param targetPath The target path
     * @return Result of the operation
     */
    public static String copyFile(String sourcePath, String targetPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("Attempting to copy ").append(sourcePath).append(" to ").append(targetPath).append("\n");
        
        try {
            File sourceFile = new File(sourcePath);
            File targetFile = new File(targetPath);
            
            if (!sourceFile.exists()) {
                return "Source file does not exist: " + sourcePath;
            }
            
            // Create target directory if it doesn't exist
            if (!targetFile.getParentFile().exists()) {
                boolean created = targetFile.getParentFile().mkdirs();
                sb.append("Created parent directories: ").append(created).append("\n");
            }
            
            // Copy the file
            Files.copy(sourceFile.toPath(), targetFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            sb.append("Copy operation completed\n");
            sb.append("Target file exists: ").append(targetFile.exists()).append("\n");
            if (targetFile.exists()) {
                sb.append("Target file size: ").append(targetFile.length()).append(" bytes\n");
            }
            
            return sb.toString();
        } catch (Exception e) {
            sb.append("Error copying file: ").append(e.getMessage()).append("\n");
            return sb.toString();
        }
    }
} 