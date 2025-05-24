package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    private int id;
    private int userId;
    private int productId;
    private int value; // 1-5 stars
    private String comment;
    private Timestamp createdAt;
    
    public Rating(int userId, int productId, int value, String comment) {
        this.userId = userId;
        this.productId = productId;
        this.value = value;
        this.comment = comment;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
} 