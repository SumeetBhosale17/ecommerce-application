package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {
    private int id;
    private int userId;
    private int productId;
    private Timestamp dateAdded;
    
    public Wishlist(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
        this.dateAdded = new Timestamp(System.currentTimeMillis());
    }
} 