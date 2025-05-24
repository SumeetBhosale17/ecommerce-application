package com.ecommerce.util;

import com.ecommerce.config.DBConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to run database migration scripts at application startup
 */
public class DatabaseMigrationHelper {
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrationHelper.class.getName());
    
    /**
     * Run all migration scripts
     */
    public static void runMigrations() {
        LOGGER.info("Running database migrations...");
        
        try {
            // Run create tables if needed
            runMigrationScript("/sql/create_tables.sql");
            
            // Run specific migration scripts
            runMigrationScript("/sql/migrate_sales_table.sql");
            runMigrationScript("/sql/migrate_notifications_table.sql");
            runMigrationScript("/sql/migrate_addresses_table.sql");
            
            LOGGER.info("Database migrations completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running database migrations", e);
        }
    }
    
    /**
     * Execute a SQL script file
     */
    private static void runMigrationScript(String scriptPath) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            
            // Parse the script considering delimiter changes
            List<SqlStatement> statements = parseSqlScript(scriptPath);
            for (SqlStatement statement : statements) {
                if (!statement.sql.trim().isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        LOGGER.fine("Executing SQL with delimiter " + statement.delimiter + ": " + statement.sql);
                        stmt.execute(statement.sql);
                    } catch (SQLException e) {
                        // Log and continue - some statements might fail if changes already applied
                        LOGGER.log(Level.WARNING, "Error executing SQL: " + statement.sql, e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running migration script: " + scriptPath, e);
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
    }
    
    /**
     * Parse SQL script handling delimiter changes
     */
    private static List<SqlStatement> parseSqlScript(String scriptPath) {
        List<SqlStatement> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        String currentDelimiter = ";";
        
        try (InputStream is = DatabaseMigrationHelper.class.getResourceAsStream(scriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            String line;
            boolean inDelimiterChange = false;
            
            Pattern delimiterPattern = Pattern.compile("DELIMITER\\s+([^\\s]+)");
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                // Check for delimiter changes
                if (line.toUpperCase().startsWith("DELIMITER")) {
                    Matcher matcher = delimiterPattern.matcher(line);
                    if (matcher.find()) {
                        // If we have a pending statement, add it
                        if (currentStatement.length() > 0) {
                            statements.add(new SqlStatement(currentStatement.toString(), currentDelimiter));
                            currentStatement = new StringBuilder();
                        }
                        currentDelimiter = matcher.group(1);
                        LOGGER.fine("Delimiter changed to: " + currentDelimiter);
                    }
                    continue;
                }
                
                // Add the line to the current statement
                currentStatement.append(line).append(" ");
                
                // Check if this line ends with the current delimiter
                if (line.endsWith(currentDelimiter)) {
                    // Remove the delimiter from the statement
                    String statementStr = currentStatement.toString();
                    statementStr = statementStr.substring(0, statementStr.length() - currentDelimiter.length()).trim();
                    
                    // Add the statement
                    statements.add(new SqlStatement(statementStr, currentDelimiter));
                    currentStatement = new StringBuilder();
                    
                    // Reset to default delimiter if this was a special delimiter section
                    if (!currentDelimiter.equals(";")) {
                        currentDelimiter = ";";
                        LOGGER.fine("Delimiter reset to default: ;");
                    }
                }
            }
            
            // Add any remaining statement
            if (currentStatement.length() > 0) {
                statements.add(new SqlStatement(currentStatement.toString(), currentDelimiter));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error parsing SQL script: " + scriptPath, e);
        }
        
        return statements;
    }
    
    private static class SqlStatement {
        private final String sql;
        private final String delimiter;
        
        public SqlStatement(String sql, String delimiter) {
            this.sql = sql;
            this.delimiter = delimiter;
        }
    }
} 