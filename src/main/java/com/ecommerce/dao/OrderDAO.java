package com.ecommerce.dao;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import java.sql.Timestamp;
import java.util.List;

public interface OrderDAO
{
    boolean placeOrder(Order order);
    boolean cancelOrder(int orderId);

    Order getOrderById(int orderId);

    List<Order> getOrdersByUserId(int userId);
    List<Order> getAllOrders();

    boolean updateOrderStatus(int orderId, String status);
    boolean updateDeliveryEstimate(int orderId, Timestamp newDeliveryEstimate);
}
