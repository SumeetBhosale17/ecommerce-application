package com.ecommerce.util;

import com.ecommerce.config.DBConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to add the missing created_at column to the notifications table
 */
public class NotificationDAOMigration {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationDAOMigration.class.getName());
    
    /**
     * Add created_at column to notifications table if it doesn't exist
     */
    public static void addCreatedAtColumn() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            
            if (!columnExists(conn, "notifications", "created_at")) {
                LOGGER.info("Adding created_at column to notifications table");
                
                String sql = "ALTER TABLE notifications ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    LOGGER.info("created_at column added successfully");
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error adding created_at column: " + e.getMessage(), e);
                }
            } else {
                LOGGER.info("created_at column already exists in notifications table");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking/adding created_at column: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
    }
    
    /**
     * Check if a column exists in a table
     */
    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metadata = conn.getMetaData();
        try (ResultSet rs = metadata.getColumns(null, null, tableName, columnName)) {
            return rs.next(); // Column exists if resultset has rows
        }
    }
    
    /**
     * Execute this method to run the migration
     */
    public static void main(String[] args) {
        try {
            System.out.println("Adding created_at column to notifications table...");
            addCreatedAtColumn();
            System.out.println("Migration completed");
        } catch (Exception e) {
            System.err.println("Error running migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 