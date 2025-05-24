package com.ecommerce.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ecommerce.config.AppConfig;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ProductDAOImpl;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.UserDAOImpl;
import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.dao.WishlistDAOImpl;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.service.NotificationService;

public class StockCheckScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final WishlistDAO wishlistDAO = new WishlistDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    
    // Store the last notification time for each product to avoid notification spam
    private final Map<Integer, LocalDateTime> lastNotificationTimeMap = new HashMap<>();
    private final int NOTIFICATION_COOLDOWN_HOURS = 24; // Only notify once per day for each product
    
    public void start() {
        // Run immediately and then every hour
        scheduler.scheduleAtFixedRate(
            this::checkProductStock,
            0,
            1,
            TimeUnit.HOURS
        );
        System.out.println("StockCheckScheduler started successfully. Checking product stock levels hourly.");
    }
    
    public void checkProductStock() {
        try {
            System.out.println("Running stock check at " + LocalDateTime.now() + "...");
            List<Product> products = productDAO.getAllProduct();
            
            if (products == null || products.isEmpty()) {
                System.out.println("No products found to check stock levels.");
                return;
            }
            
            System.out.println("Checking stock levels for " + products.size() + " products...");
            int lowStockCount = 0;
            
            for (Product product : products) {
                if (product.getStock() <= AppConfig.LOW_STOCK_THRESHOLD) {
                    lowStockCount++;
                    
                    // Check if we've already sent a notification recently
                    Integer productId = product.getId();
                    LocalDateTime lastNotified = lastNotificationTimeMap.get(productId);
                    LocalDateTime now = LocalDateTime.now();
                    
                    if (lastNotified == null || Duration.between(lastNotified, now).toHours() >= NOTIFICATION_COOLDOWN_HOURS) {
                        // 1. Notify all admin users
                        int adminNotifications = NotificationService.sendLowStockNotification(product);
                        System.out.println("Sent low stock notifications to " + adminNotifications + " admin users for product: " + product.getName());
                        
                        // 2. Notify users who have this product in their wishlist
                        try {
                            List<User> wishlistUsers = wishlistDAO.getUsersWithProductInWishlist(productId);
                            if (wishlistUsers != null && !wishlistUsers.isEmpty()) {
                                System.out.println("Sending wishlist notifications to " + wishlistUsers.size() + " users for low stock product: " + product.getName());
                                
                                for (User user : wishlistUsers) {
                                    String message = "A product in your wishlist (" + product.getName() + ") is running low on stock! Only " + 
                                                    product.getStock() + " units left. Order soon before it's gone!";
                                    
                                    NotificationService.sendNotification(user, message);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error notifying wishlist users about low stock for product ID " + productId + ": " + e.getMessage());
                        }
                        
                        // Update the last notification time
                        lastNotificationTimeMap.put(productId, now);
                    } else {
                        System.out.println("Skipping notifications for product " + product.getName() + " - already notified within the past " + 
                                          NOTIFICATION_COOLDOWN_HOURS + " hours.");
                    }
                }
            }
            
            System.out.println("Stock check completed. Found " + lowStockCount + " products with low stock levels.");
        } catch (Exception e) {
            System.err.println("Error running stock check: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stop() {
        System.out.println("Shutting down StockCheckScheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            System.out.println("StockCheckScheduler stopped successfully.");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("Error shutting down StockCheckScheduler: " + e.getMessage());
        }
    }
} 