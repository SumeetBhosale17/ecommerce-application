package com.ecommerce.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ecommerce.dao.SaleDAO;
import com.ecommerce.dao.SaleDAOImpl;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.UserDAOImpl;
import com.ecommerce.model.Sale;
import com.ecommerce.model.User;
import com.ecommerce.service.NotificationService;

public class SaleStatusScheduler
{
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    public void start()
    {
        Runnable task = () -> {
            System.out.println("Running SaleStatusScheduler at " + LocalDateTime.now() + "...");
            updateSaleStatuses();
            checkUpcomingSalesEnd();
        };
        
        scheduler.scheduleAtFixedRate(task, 0, 24, TimeUnit.HOURS);
        System.out.println("SaleStatusScheduler started successfully.");
    }

    public void stop() throws InterruptedException
    {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            System.out.println("SaleStatusScheduler stopped successfully.");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            throw e;
        }
    }
    
    private void updateSaleStatuses()
    {
        try {
            System.out.println("Checking and updating sale statuses...");
            List<Sale> sales = saleDAO.getAllSales();
            int activatedSales = 0;
            int completedSales = 0;
            
            if (sales == null || sales.isEmpty()) {
                System.out.println("No sales found to update");
                return;
            }
            
            LocalDate currentDate = LocalDate.now();
            
            for (Sale sale : sales) {
                if (sale.getStartDate() == null || sale.getEndDate() == null) {
                    System.out.println("Sale #" + sale.getId() + " has null dates - skipping");
                    continue;
                }
                
                LocalDate startDate = sale.getStartDate().toLocalDate();
                LocalDate endDate = sale.getEndDate().toLocalDate();
                
                // Check if a sale should be activated
                if (sale.getStatus().equals("SCHEDULED") && 
                    (currentDate.isEqual(startDate) || currentDate.isAfter(startDate))) {
                    
                    // Activate the sale
                    boolean updated = saleDAO.updateSaleStatus(sale.getId(), "ACTIVE");
                    if (updated) {
                        activatedSales++;
                        System.out.println("Sale #" + sale.getId() + " '" + sale.getName() + "' activated");
                    }
                }
                
                // Check if a sale should be completed
                if (sale.getStatus().equals("ACTIVE") && 
                    (currentDate.isAfter(endDate))) {
                    
                    // Complete the sale
                    boolean updated = saleDAO.updateSaleStatus(sale.getId(), "COMPLETED");
                    if (updated) {
                        completedSales++;
                        System.out.println("Sale #" + sale.getId() + " '" + sale.getName() + "' completed");
                    }
                }
            }
            
            System.out.println("Sale status update completed: " + activatedSales + " sales activated, " + completedSales + " sales completed");
        } catch (Exception e) {
            System.err.println("Error updating sale statuses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void checkUpcomingSalesEnd() {
        try {
            System.out.println("Checking for sales ending soon...");
            List<Sale> activeSales = saleDAO.getActiveSales();
            
            if (activeSales == null || activeSales.isEmpty()) {
                System.out.println("No active sales found to check");
                return;
            }
            
            LocalDate currentDate = LocalDate.now();
            LocalDate tomorrowDate = currentDate.plusDays(1);
            
            for (Sale sale : activeSales) {
                if (sale.getEndDate() == null) continue;
                
                LocalDate endDate = sale.getEndDate().toLocalDate();
                
                // Check if sale ends tomorrow
                if (endDate.isEqual(tomorrowDate)) {
                    System.out.println("Sale #" + sale.getId() + " '" + sale.getName() + "' ending tomorrow - sending notifications");
                    
                    // Notify admin users
                    try {
                        List<User> adminUsers = userDAO.getAdminUsers();
                        if (adminUsers != null) {
                            for (User admin : adminUsers) {
                                String message = "ADMIN ALERT: Sale '" + sale.getName() + 
                                               "' with " + sale.getDiscountPercent() + "% discount is ending tomorrow!";
                                NotificationService.sendNotification(admin, message);
                            }
                            System.out.println("Sent sale ending notifications to " + adminUsers.size() + " admin users");
                        }
                    } catch (Exception e) {
                        System.err.println("Error notifying admin users about ending sale: " + e.getMessage());
                    }
                    
                    // Notify all users
                    try {
                        String message = "LAST CHANCE: Sale '" + sale.getName() + 
                                        "' with " + sale.getDiscountPercent() + "% discount ends tomorrow!";
                        NotificationService.sendSaleEndingNotificationToAllUsers(sale.getName(), sale.getDiscountPercent());
                        System.out.println("Sent sale ending notification to all users");
                    } catch (Exception e) {
                        System.err.println("Error sending notifications to users: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Completed checking for sales ending soon");
        } catch (Exception e) {
            System.err.println("Error checking for upcoming sales end: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
