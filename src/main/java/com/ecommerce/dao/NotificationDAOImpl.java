package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO
{
    @Override
    public boolean addNotification(Notification notification)
    {
        // Check if created_at field exists
        boolean hasCreatedAt = checkIfColumnExists("notifications", "created_at");
        
        String sql;
        if (hasCreatedAt) {
            sql = "INSERT INTO notifications (user_id, message, created_at, is_read) VALUES (?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, ?)";
        }
        
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql))
            {
                stmt.setInt(1, notification.getUserId());
                stmt.setString(2, notification.getMessage());
                
                if (hasCreatedAt) {
                    stmt.setTimestamp(3, notification.getCreatedAt());
                    stmt.setBoolean(4, notification.isRead());
                } else {
                    stmt.setBoolean(3, notification.isRead());
                }
                
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public boolean deleteNotification(int notificationId)
    {
        String sql = "DELETE FROM notifications WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql))
            {
                stmt.setInt(1, notificationId);
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public Notification getNotification(int notificationId)
    {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql))
            {
                stmt.setInt(1, notificationId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractNotificationFromResultSet(rs);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public List<Notification> getNotificationsByUserId(int userId)
    {
        List<Notification> notifications = new ArrayList<>();
        boolean hasCreatedAt = checkIfColumnExists("notifications", "created_at");
        
        String sql;
        if (hasCreatedAt) {
            sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        } else {
            sql = "SELECT * FROM notifications WHERE user_id = ?";
        }
        
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql))
            {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    while (rs.next())
                    {
                        notifications.add(extractNotificationFromResultSet(rs));
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return notifications;
    }

    @Override
    public int countUnreadNotifications(int userId)
    {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql))
            {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return rs.getInt(1);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return 0;
    }

    @Override
    public boolean updateNotification(Notification notification)
    {
        String sql = "UPDATE notifications SET message = ?, is_read = ? WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql))
            {
                stmt.setString(1, notification.getMessage());
                stmt.setBoolean(2, notification.isRead());
                stmt.setInt(3, notification.getId());
                
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    private Notification extractNotificationFromResultSet(ResultSet rs) throws SQLException
    {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setMessage(rs.getString("message"));
        
        try {
            notification.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException e) {
            // Column doesn't exist or is null, set current timestamp
            notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        
        notification.setRead(rs.getBoolean("is_read"));
        return notification;
    }
    
    private boolean checkIfColumnExists(String tableName, String columnName) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            DatabaseMetaData dbMeta = conn.getMetaData();
            ResultSet rs = dbMeta.getColumns(null, null, tableName, columnName);
            return rs.next(); // Column exists if resultset has rows
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
    }
}
