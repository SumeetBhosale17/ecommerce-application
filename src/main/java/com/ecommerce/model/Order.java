package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order
{
    private int id;
    private int userId;
    private Timestamp orderDate;
    private double totalAmount;
    private int addressId;
    private OrderStatus orderStatus;
    private Timestamp deliveryEstimate;

    public Order(int user_id, double total_amount, int address_id)
    {
        this.userId = user_id;
        this.totalAmount = total_amount;
        this.addressId = address_id;
    }
}
