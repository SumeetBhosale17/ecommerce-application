package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification
{
    private int id;
    private int userId;
    private String message;
    private Timestamp createdAt;
    private boolean isRead;

    public Notification(int userId, String message, Timestamp createdAt)
    {
        this.createdAt = createdAt;
        this.userId = userId;
        this.message = message;
        this.isRead = false;
    }
}
