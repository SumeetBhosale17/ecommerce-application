package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem
{
    private int id;
    private int userId;
    private int productId;
    private int quantity;

    public CartItem(int userId, int productId, int quantity)
    {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
}
