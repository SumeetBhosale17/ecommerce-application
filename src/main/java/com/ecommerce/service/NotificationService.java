package com.ecommerce.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.config.AppConfig;
import com.ecommerce.dao.NotificationDAO;
import com.ecommerce.dao.NotificationDAOImpl;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.UserDAOImpl;
import com.ecommerce.model.Notification;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.utils.EmailSender;

public class NotificationService
{
    private static final NotificationDAO notificationDAO = new NotificationDAOImpl();
    private static final UserDAO userDAO = new UserDAOImpl();
    private static final EmailSender emailSender = new EmailSender();
    private static final String EMAIL_FROM = AppConfig.EMAIL_FROM;
    private static final String EMAIL_PASSWORD = AppConfig.EMAIL_PASSWORD;
    
    /**
     * Send a general notification to a user
     */
    public static boolean sendNotification(User user, String message)
    {
        if (user == null || message == null || message.isEmpty()) {
            System.err.println("Cannot send notification: Invalid user or message");
            return false;
        }
        
        boolean success = false;
        
        try {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setMessage(message);
            notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            notification.setRead(false);
    
            success = notificationDAO.addNotification(notification);
            
            if (!success) {
                System.err.println("Failed to add notification to database for user: " + user.getUsername());
            }
    
            // Try to send email, but continue even if it fails
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                try {
                    emailSender.sendEmail(user.getEmail(), "E-commerce Notification", message, null);
                } catch (Exception e) {
                    System.err.println("Failed to send email notification to " + user.getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return success;
    }
    
    /**
     * Send an order status notification to a specific user
     */
    public static boolean sendOrderStatusNotification(User user, Order order, String newStatus)
    {
        if (user == null || order == null || newStatus == null) {
            System.err.println("Cannot send order status notification: Missing required parameters");
            return false;
        }
        
        String message = "Your order #" + order.getId() + " status has been updated to: " + newStatus;
        boolean success = false;
        
        try {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setMessage(message);
            notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            notification.setRead(false);
            
            success = notificationDAO.addNotification(notification);
            
            if (!success) {
                System.err.println("Failed to add order status notification to database for user: " + user.getUsername());
            }
            
            // Try to send email, but continue even if it fails
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                try {
                    emailSender.sendEmail(user.getEmail(), "Order Status Update", message, null);
                } catch (Exception e) {
                    System.err.println("Failed to send order status email notification to " + user.getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending order status notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return success;
    }
    
    /**
     * Send a notification to all users about an upcoming or active sale
     */
    public static void sendSaleNotificationToAllUsers(String saleName, boolean isActive)
    {
        try {
            List<User> users = userDAO.getAllUsers();
            String subject = isActive ? "New Sale Alert!" : "Upcoming Sale Alert!";
            String message = isActive ? 
                "A new sale is now active: " + saleName :
                "A new sale is coming tomorrow: " + saleName;
                
            for (User user : users) {
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    // Add in-app notification
                    Notification notification = new Notification();
                    notification.setUserId(user.getId());
                    notification.setMessage(message);
                    notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    notification.setRead(false);
                    notificationDAO.addNotification(notification);
                    
                    // Send email notification
                    try {
                        emailSender.sendEmail(user.getEmail(), subject, message, null);
                    } catch (Exception e) {
                        System.err.println("Failed to send sale email notification to " + user.getEmail() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending sale notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send a low stock notification to all admin users
     */
    public static int sendLowStockNotification(Product product)
    {
        if (product == null || product.getStock() > AppConfig.LOW_STOCK_THRESHOLD) {
            return 0;
        }
        
        String message = "Low stock alert: " + product.getName() + " has only " + product.getStock() + " units left.";
        int notificationsSent = 0;
        
        try {
            List<User> users = new ArrayList<>();
            try {
                users = userDAO.getAllUsers();
            } catch (Exception e) {
                System.err.println("Failed to retrieve admin users for low stock notification: " + e.getMessage());
                return 0;
            }
            
            for (User user : users) {
                if (user.isAdmin()) {
                    try {
                        Notification notification = new Notification();
                        notification.setUserId(user.getId());
                        notification.setMessage(message);
                        notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                        notification.setRead(false);
                        
                        boolean success = notificationDAO.addNotification(notification);
                        
                        if (success) {
                            notificationsSent++;
                            
                            // Try to send email, but continue even if it fails
                            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                                try {
                                    emailSender.sendEmail(user.getEmail(), "Low Stock Alert", message, null);
                                } catch (Exception e) {
                                    System.err.println("Failed to send low stock email notification to " + user.getEmail() + ": " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error sending low stock notification to admin " + user.getUsername() + ": " + e.getMessage());
                    }
                }
            }
            
            if (notificationsSent > 0) {
                System.out.println("Low stock notifications sent to " + notificationsSent + " admin users");
            }
        } catch (Exception e) {
            System.err.println("Error sending low stock notifications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return notificationsSent;
    }
    
    /**
     * Send notification to a user when a product in their cart is almost out of stock
     */
    public static boolean sendCartItemLowStockNotification(User user, Product product)
    {
        if (user == null || product == null) {
            System.err.println("Cannot send cart item low stock notification: Missing user or product");
            return false;
        }
        
        String message = "Hurry! The product '" + product.getName() + "' in your cart is almost out of stock. Only " + product.getStock() + " left!";
        boolean success = false;
        
        try {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setMessage(message);
            notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            notification.setRead(false);
            
            success = notificationDAO.addNotification(notification);
            
            if (!success) {
                System.err.println("Failed to add cart item low stock notification to database for user: " + user.getUsername());
            }
            
            // Try to send email, but continue even if it fails
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                try {
                    emailSender.sendEmail(user.getEmail(), "Cart Item Low Stock Alert", message, null);
                } catch (Exception e) {
                    System.err.println("Failed to send cart item low stock email notification to " + user.getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending cart item low stock notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return success;
    }
    
    /**
     * Send a notification to all users about a sale that is ending soon
     */
    public static void sendSaleEndingNotificationToAllUsers(String saleName, double discountPercent)
    {
        try {
            List<User> users = userDAO.getAllUsers();
            String subject = "Last Chance! Sale Ending Soon";
            String message = "LAST CHANCE: Sale '" + saleName + "' with " + discountPercent + "% discount ends tomorrow!";
                
            int notificationCount = 0;
            for (User user : users) {
                if (user != null) {
                    // Add in-app notification
                    Notification notification = new Notification();
                    notification.setUserId(user.getId());
                    notification.setMessage(message);
                    notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    notification.setRead(false);
                    boolean added = notificationDAO.addNotification(notification);
                    
                    if (added) {
                        notificationCount++;
                    }
                    
                    // Send email notification
                    if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                        try {
                            emailSender.sendEmail(user.getEmail(), subject, message, null);
                        } catch (Exception e) {
                            System.err.println("Failed to send sale ending email notification to " + user.getEmail() + ": " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("Sent " + notificationCount + " sale ending notifications to users");
        } catch (Exception e) {
            System.err.println("Error sending sale ending notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
