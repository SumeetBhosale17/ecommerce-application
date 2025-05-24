package com.ecommerce.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DBConnection
{
    private static final int POOL_SIZE = 10;
    private static BlockingQueue<Connection> connectionPool;
    private static boolean driverLoaded = false;

    static
    {
        loadDriver();
        initializeConnectionPool();
    }
    
    private static void loadDriver() {
        if (driverLoaded) {
            return;
        }
        
        try {
            // Initialize AppConfig
            AppConfig.initialize();
            
            // Load MySQL JDBC driver
            Class.forName(AppConfig.getDbDriver());
            driverLoaded = true;
            System.out.println("Database driver loaded successfully: " + AppConfig.getDbDriver());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("Database JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeConnectionPool() {
        connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
        try {
            String url = AppConfig.getDbUrl();
            String username = AppConfig.getDbUsername();
            String password = AppConfig.getDbPassword();
            
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(url, username, password);
                connectionPool.offer(conn);
            }
            System.out.println("Database connection pool initialized with " + POOL_SIZE + " connections");
        } catch (SQLException e) {
            System.err.println("Failed to initialize connection pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll();
            if (conn == null || conn.isClosed()) {
                // Create a new connection if pool is empty or connection is closed
                String url = AppConfig.getDbUrl();
                String username = AppConfig.getDbUsername();
                String password = AppConfig.getDbPassword();
                conn = DriverManager.getConnection(url, username, password);
            }
            return conn;
        } catch (SQLException e) {
            System.err.println("Error getting connection from pool: " + e.getMessage());
            throw e;
        }
    }
    
    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    connectionPool.offer(conn);
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection back to pool: " + e.getMessage());
            }
        }
    }
    
    public static void closeAllConnections() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        System.out.println("All database connections closed.");
    }
    
    /**
     * Checks if a valid database connection can be established
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
