package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem
{
    private int id;
    private int orderId;
    private int productId;
    private String productName;
    private int quantity;
    private double price;

    public OrderItem(int order_id, int product_id, int quantity, double price)
    {
        this.orderId = order_id;
        this.price = price;
        this.quantity = quantity;
        this.productId = product_id;
    }

    public OrderItem(int orderId, int productId, String productName, int quantity, double price)
    {
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.productId = productId;
        this.productName = productName;
    }
}
