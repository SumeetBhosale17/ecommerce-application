package com.ecommerce.dao;

import com.ecommerce.model.OrderItem;

import java.util.List;

public interface OrderItemDAO
{
    boolean addOrderItem(OrderItem item);
    List<OrderItem> getItemsByOrderId(int orderId);

    boolean deleteItem(int orderItemId);
    boolean deleteItemsByOrderId(int orderId);

    boolean updateQuantity(int quantity, int orderItemId);
    boolean updateOrderItem(OrderItem item);

    List<OrderItem> getAllOrderItems();
}
