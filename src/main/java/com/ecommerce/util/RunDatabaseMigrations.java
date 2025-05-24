package com.ecommerce.util;

import com.ecommerce.config.AppConfig;
import com.ecommerce.config.DBConnection;

/**
 * Standalone utility to run database migrations
 * This can be executed directly to update the database schema
 */
public class RunDatabaseMigrations {
    public static void main(String[] args) {
        try {
            // Initialize configuration
            System.out.println("Initializing application configuration...");
            AppConfig.initialize();
            
            // Run migrations
            System.out.println("Running database migrations...");
            DatabaseMigrationHelper.runMigrations();
            
            System.out.println("Database migrations completed successfully.");
            
            // Close all connections
            DBConnection.closeAllConnections();
            System.out.println("All database connections closed.");
            
        } catch (Exception e) {
            System.err.println("Error running migrations: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 