package com.ecommerce.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration class for application-wide settings
 */
public class AppConfig {
    private static final Properties properties = new Properties();
    private static boolean initialized = false;
    
    // Email config
    public static final String EMAIL_HOST = getEmailHost();
    public static final String EMAIL_PORT = getEmailPort();
    public static final String EMAIL_FROM = getEmailUsername();
    public static final String EMAIL_PASSWORD = getEmailPassword();
    
    // Product config
    public static final int LOW_STOCK_THRESHOLD = 5;
    
    // Database config
    public static final String DB_URL_DEFAULT = "jdbc:mysql://localhost:3306/ecommerce_db";
    public static final String DB_USER_DEFAULT = "root";
    public static final String DB_PASS_DEFAULT = "";
    public static final String DB_DRIVER_DEFAULT = "com.mysql.cj.jdbc.Driver";
    
    /**
     * Initialize configuration from properties file
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Try to load from classpath first
            InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties");
            
            if (input == null) {
                // Try to load from file system as fallback
                input = AppConfig.class.getResourceAsStream("/application.properties");
            }
            
            if (input == null) {
                System.err.println("Warning: application.properties not found in classpath or file system. Using default values.");
                initialized = true;
                return;
            }
            
            try {
                properties.load(input);
                initialized = true;
                System.out.println("Application configuration loaded successfully");
            } finally {
                input.close();
            }
        } catch (IOException e) {
            System.err.println("Error loading application.properties: " + e.getMessage());
            e.printStackTrace();
            // Continue with default values
            initialized = true;
        }
    }
    
    /**
     * Get a property value with a default fallback
     */
    public static String getProperty(String key, String defaultValue) {
        if (!initialized) {
            initialize();
        }
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get a property value
     */
    public static String getProperty(String key) {
        if (!initialized) {
            initialize();
        }
        return properties.getProperty(key);
    }
    
    /**
     * Get database URL
     */
    public static String getDbUrl() {
        return getProperty("db.url", DB_URL_DEFAULT);
    }
    
    /**
     * Get database username
     */
    public static String getDbUsername() {
        return getProperty("db.username", DB_USER_DEFAULT);
    }
    
    /**
     * Get database password
     */
    public static String getDbPassword() {
        return getProperty("db.password", DB_PASS_DEFAULT);
    }
    
    /**
     * Get database driver class
     */
    public static String getDbDriver() {
        return getProperty("db.driver", DB_DRIVER_DEFAULT);
    }
    
    /**
     * Get email host
     */
    public static String getEmailHost() {
        return getProperty("mail.smtp.host", "smtp.gmail.com");
    }
    
    /**
     * Get email port
     */
    public static String getEmailPort() {
        return getProperty("mail.smtp.port", "587");
    }
    
    /**
     * Get email username
     */
    public static String getEmailUsername() {
        return getProperty("mail.username", "");
    }
    
    /**
     * Get email password
     */
    public static String getEmailPassword() {
        return getProperty("mail.password", "");
    }
    
    /**
     * Check if email is configured
     */
    public static boolean isEmailConfigured() {
        String username = getEmailUsername();
        String password = getEmailPassword();
        
        boolean isConfigured = username != null && !username.isEmpty() && 
               !username.equals("your-email@gmail.com") &&
               password != null && !password.isEmpty() && 
               !password.equals("your-app-password");
               
        if (isConfigured) {
            System.out.println("Email configuration detected: Username=" + username);
        } else {
            System.out.println("Email not configured properly. Check mail.username and mail.password in application.properties");
            System.out.println("Current values: Username=" + (username == null ? "null" : (username.isEmpty() ? "empty" : username)));
            System.out.println("Password length=" + (password == null ? "null" : (password.isEmpty() ? "0" : password.length())));
        }
        
        return isConfigured;
    }
} 