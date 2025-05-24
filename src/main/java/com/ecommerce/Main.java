package com.ecommerce;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ecommerce.config.AppConfig;
import com.ecommerce.config.DBConnection;
import com.ecommerce.scheduler.OrderStatusScheduler;
import com.ecommerce.scheduler.SaleNotificationScheduler;
import com.ecommerce.scheduler.SaleStatusScheduler;
import com.ecommerce.scheduler.StockCheckScheduler;
import com.ecommerce.util.FontManager;
import com.ecommerce.util.NotificationDAOMigration;
import com.ecommerce.util.ThemeManager;
import com.ecommerce.view.LoginForm;

public class Main {
    private static OrderStatusScheduler orderStatusScheduler;
    private static SaleNotificationScheduler saleNotificationScheduler;
    private static SaleStatusScheduler saleStatusScheduler;
    private static StockCheckScheduler stockCheckScheduler;
    
    public static void main(String[] args) {
        try {
            // Initialize application configuration
            System.out.println("Initializing E-commerce Application...");
            AppConfig.initialize();
            
            // Test database connection
            if (!DBConnection.testConnection()) {
                JOptionPane.showMessageDialog(null, 
                    "Failed to connect to the database. Please check your database configuration.", 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            
            // Run notification table migration
            NotificationDAOMigration.addCreatedAtColumn();
            
            // Ensure receipt directory exists
            File receiptDir = new File("OrderReceipts");
            if (!receiptDir.exists()) {
                boolean created = receiptDir.mkdirs();
                if (created) {
                    System.out.println("Created OrderReceipts directory");
                } else {
                    System.err.println("Failed to create OrderReceipts directory");
                }
            }
            
            // Start schedulers
            startSchedulers();
            
            // Initialize UI components
            SwingUtilities.invokeLater(() -> {
                try {
                    // Create and show splash screen while loading fonts
                    JWindow splashScreen = new JWindow();
                    JPanel splashPanel = new JPanel(new BorderLayout());
                    splashPanel.setBorder(BorderFactory.createLineBorder(new Color(25, 25, 112), 2));
                    
                    JLabel appNameLabel = new JLabel("E-Commerce App");
                    appNameLabel.setFont(new Font("Arial", Font.BOLD, 32));
                    appNameLabel.setForeground(new Color(25, 25, 112));
                    appNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    appNameLabel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
                    
                    JLabel loadingLabel = new JLabel("Loading application...");
                    loadingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    loadingLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
                    
                    splashPanel.add(appNameLabel, BorderLayout.CENTER);
                    splashPanel.add(loadingLabel, BorderLayout.SOUTH);
                    splashPanel.setBackground(Color.WHITE);
                    
                    splashScreen.getContentPane().add(splashPanel);
                    splashScreen.pack();
                    splashScreen.setLocationRelativeTo(null);
                    splashScreen.setVisible(true);
                
                    // Set system look and feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    
                    // Initialize fonts explicitly and verify they're loaded
                    System.out.println("Initializing Caudex fonts...");
                    Font defaultFont = FontManager.getDefault();
                    Font titleFont = FontManager.getTitle();
                    Font boldFont = FontManager.getBold(16f);
                    System.out.println("Default font initialized: " + defaultFont.getFamily() + " " + defaultFont.getSize());
                    System.out.println("Title font initialized: " + titleFont.getFamily() + " " + titleFont.getSize());
                    System.out.println("Bold font initialized: " + boldFont.getFamily() + " " + boldFont.getSize());
                    
                    // Apply custom theme
                    ThemeManager.applyGlobalTheme();
                    
                    // Close splash after a minimum display time and show login form
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            splashScreen.dispose();
                            LoginForm loginForm = new LoginForm();
                            // Force font application on the login form
                            loginForm.setVisible(true);
                        }
                    }, 1500); // minimum 1.5 seconds display
                } catch (Exception e) {
                    System.err.println("Error in UI initialization: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // Add shutdown hook to stop schedulers and close connections
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Application shutting down. Stopping schedulers...");
                stopSchedulers();
                System.out.println("Closing database connections...");
                DBConnection.closeAllConnections();
                System.out.println("All database connections closed.");
            }));
            
            System.out.println("E-commerce Application started successfully");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fatal error starting application: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "An error occurred while starting the application: " + e.getMessage(), 
                "Application Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private static void startSchedulers() {
        try {
            orderStatusScheduler = new OrderStatusScheduler();
            saleNotificationScheduler = new SaleNotificationScheduler();
            saleStatusScheduler = new SaleStatusScheduler();
            // Don't start StockCheckScheduler at application startup
            // We will use it directly from the OrderService when orders are placed
            
            orderStatusScheduler.start();
            saleNotificationScheduler.startScheduler();
            saleStatusScheduler.start();
            // Removed stockCheckScheduler.start();
            
            System.out.println("Schedulers started successfully");
        } catch (Exception e) {
            System.err.println("Error starting schedulers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void stopSchedulers() {
        try {
            if (orderStatusScheduler != null) {
                orderStatusScheduler.stop();
                System.out.println("Order status scheduler stopped");
            }
            if (saleNotificationScheduler != null) {
                saleNotificationScheduler.stop();
                System.out.println("Sale notification scheduler stopped");
            }
            if (saleStatusScheduler != null) {
                saleStatusScheduler.stop();
                System.out.println("SaleStatusScheduler stopped successfully.");
            }
            if (stockCheckScheduler != null) {
                stockCheckScheduler.stop();
                System.out.println("StockCheckScheduler stopped successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error stopping schedulers: " + e.getMessage());
            e.printStackTrace();
        }
    }
}