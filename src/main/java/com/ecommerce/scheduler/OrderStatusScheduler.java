package com.ecommerce.scheduler;

import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.OrderDAOImpl;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.UserDAOImpl;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.User;
import com.ecommerce.service.NotificationService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.List;
import java.util.concurrent.*;

public class OrderStatusScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    /** Start the scheduler to run once a day at midnight */
    public void start() {
        long initialDelay = computeDelayToNextMidnight();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(this::updateOrders,
                initialDelay,
                period,
                TimeUnit.MILLISECONDS);
        
        System.out.println("OrderStatusScheduler started. Next run in " + (initialDelay / 1000 / 60) + " minutes.");
    }

    /** The task that updates order statuses and delivery estimates */
    private void updateOrders() {
        try {
            System.out.println("Running order status updates...");
            List<Order> orders = orderDAO.getAllOrders();
            if (orders == null || orders.isEmpty()) {
                System.out.println("No orders found to update");
                return;
            }
            
            Timestamp now = new Timestamp(System.currentTimeMillis());

            for (Order order : orders) {
                // Skip if essential data is missing
                if (order == null || order.getOrderDate() == null) {
                    System.out.println("Skipping order with missing data: " + (order != null ? order.getId() : "null order"));
                    continue;
                }
                
                Timestamp orderDate = order.getOrderDate();
                Timestamp deliveryEstimate = order.getDeliveryEstimate();
                OrderStatus currentStatus = order.getOrderStatus();
                
                // Skip if order status is null or already delivered
                if (currentStatus == null) {
                    System.out.println("Skipping order with null status: ID=" + order.getId());
                    continue;
                }
                
                // Skip orders that are already delivered or cancelled
                if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
                    continue;
                }

                // If the delivery_estimate is not set, update it
                if (deliveryEstimate == null) {
                    Timestamp newDeliveryEstimate = new Timestamp(orderDate.getTime() + TimeUnit.DAYS.toMillis(7));
                    order.setDeliveryEstimate(newDeliveryEstimate);
                    orderDAO.updateDeliveryEstimate(order.getId(), newDeliveryEstimate);
                }
                
                // Determine days since order was placed
                long daysSinceOrder = TimeUnit.MILLISECONDS.toDays(now.getTime() - orderDate.getTime());
                OrderStatus newStatus = null;
                
                // Update the status based on days since order
                if (daysSinceOrder <= 1 && currentStatus == OrderStatus.PENDING) {
                    newStatus = OrderStatus.CONFIRMED;
                } else if (daysSinceOrder > 1 && daysSinceOrder <= 2 && currentStatus == OrderStatus.CONFIRMED) {
                    newStatus = OrderStatus.SHIPPED;
                } else if (daysSinceOrder > 2 && daysSinceOrder <= 4 && currentStatus == OrderStatus.SHIPPED) {
                    newStatus = OrderStatus.OUT_FOR_DELIVERY;
                } else if (daysSinceOrder > 4 && currentStatus == OrderStatus.OUT_FOR_DELIVERY) {
                    newStatus = OrderStatus.DELIVERED;
                }
                
                // If status needs to be updated
                if (newStatus != null && newStatus != currentStatus) {
                    String statusString = newStatus.name().toLowerCase();
                    boolean updated = orderDAO.updateOrderStatus(order.getId(), statusString);
                    
                    if (updated) {
                        // Also update the order object
                        order.setOrderStatus(newStatus);
                        
                        // Send notification to the user
                        try {
                            User user = userDAO.getUserById(order.getUserId());
                            if (user != null) {
                                NotificationService.sendOrderStatusNotification(user, order, newStatus.name());
                                System.out.println("Updated order #" + order.getId() + " status to " + statusString + " and notified user");
                            } else {
                                System.out.println("User not found for order #" + order.getId());
                            }
                        } catch (Exception e) {
                            System.err.println("Error finding user or sending notification for order #" + order.getId() + ": " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println("Order status updates completed.");
        } catch (Exception e) {
            System.err.println("Error updating order statuses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Compute millis until next midnight */
    private long computeDelayToNextMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMid = now.toLocalDate().plusDays(1).atStartOfDay();
        Duration duration = Duration.between(now, nextMid);
        return duration.toMillis();
    }

    /** Shutdown the scheduler (if you ever need to) */
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
