package com.ecommerce.dao;

import com.ecommerce.model.Notification;

import java.util.List;

public interface NotificationDAO
{
    boolean addNotification(Notification notification);
    boolean deleteNotification(int notificationId);
    Notification getNotification(int notificationId);
    
    // Additional methods needed for the UI
    List<Notification> getNotificationsByUserId(int userId);
    int countUnreadNotifications(int userId);
    boolean updateNotification(Notification notification);
}
