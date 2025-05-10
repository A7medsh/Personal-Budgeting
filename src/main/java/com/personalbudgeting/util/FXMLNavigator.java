package com.personalbudgeting.util;

import com.personalbudgeting.Main;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * Utility class for handling FXML navigation in a consistent way
 */
public class FXMLNavigator {
    
    /**
     * Navigate to a different view using the most reliable approach
     * @param event The action event that triggered the navigation (not used anymore)
     * @param fxmlFileName The name of the FXML file (without the extension)
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateTo(ActionEvent event, String fxmlFileName) {
        System.out.println("===== FXMLNavigator: Starting navigation to " + fxmlFileName + " page =====");
        try {
            // All navigation is now delegated to Main.setRoot which has the most reliable implementation
            Main.setRoot(fxmlFileName);
            return true;
        } catch (Exception e) {
            System.err.println("===== FXMLNavigator: EXCEPTION in navigation to " + fxmlFileName + " page =====");
            System.err.println("Exception class: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 