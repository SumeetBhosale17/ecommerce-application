package com.ecommerce.scheduler;

import com.ecommerce.dao.SaleDAO;
import com.ecommerce.dao.SaleDAOImpl;
import com.ecommerce.model.Sale;
import com.ecommerce.service.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SaleNotificationScheduler
{
    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startScheduler()
    {
        scheduler.scheduleAtFixedRate(this::checkAndSendNotifications, 0, 1, TimeUnit.DAYS);
    }

    private void checkAndSendNotifications()
    {
        System.out.println("Running Sale Notification Scheduler...");

        List<Sale> allSales = saleDAO.getAllSale();
        if (allSales == null || allSales.isEmpty()) {
            System.out.println("No sales found to send notifications for");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        for (Sale sale : allSales)
        {
            // Convert java.sql.Date to LocalDate
            LocalDate startDate = sale.getStartDate().toLocalDate();
            String saleName = sale.getName();

            if (startDate.equals(tomorrow))
            {
                System.out.println("Sending sale notifications for tomorrow: " + saleName);
                NotificationService.sendSaleNotificationToAllUsers(saleName, false);
            }
            else if (startDate.equals(today))
            {
                System.out.println("Sending sale notifications for today: " + saleName);
                NotificationService.sendSaleNotificationToAllUsers(saleName, true);
            }
        }
    }
    
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
