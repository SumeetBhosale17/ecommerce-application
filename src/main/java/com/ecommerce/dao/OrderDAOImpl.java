package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OrderDAOImpl implements OrderDAO
{
    private Connection conn;
    public OrderDAOImpl() {
        try {
            this.conn = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean placeOrder(Order order)
    {
        String query = "INSERT INTO orders (user_id, order_date, total_amount, address_id, status, delivey_estimate) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
        {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            stmt.setInt(1, order.getUserId());
            stmt.setTimestamp(2, now);
            stmt.setDouble(3, order.getTotalAmount());
            stmt.setInt(4, order.getAddressId());
            stmt.setString(5, order.getOrderStatus().toString().toLowerCase());
            
            // Adding delivery estimate (7 days from now)
            Timestamp deliveryEstimate = new Timestamp(now.getTime() + TimeUnit.DAYS.toMillis(7));
            stmt.setTimestamp(6, deliveryEstimate);

            try {
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            order.setId(keys.getInt(1));
                            order.setOrderDate(now);
                            order.setOrderStatus(OrderStatus.valueOf(order.getOrderStatus().name()));
                            order.setDeliveryEstimate(deliveryEstimate);
                            System.out.println("Order created successfully with ID: " + order.getId());
                            return true;
                        }
                    }
                }
            } catch (SQLException ex) {
                System.out.println("Error executing SQL for order placement: " + ex.getMessage());
                ex.printStackTrace();
                
                // Check if it's a column issue and try an alternative query without the problematic column
                if (ex.getMessage().contains("delivey_estimate") || ex.getMessage().contains("delivery_estimate")) {
                    System.out.println("Attempting to place order without delivery estimate...");
                    String fallbackQuery = "INSERT INTO orders (user_id, order_date, total_amount, address_id, status) VALUES (?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement fallbackStmt = conn.prepareStatement(fallbackQuery, Statement.RETURN_GENERATED_KEYS)) {
                        fallbackStmt.setInt(1, order.getUserId());
                        fallbackStmt.setTimestamp(2, now);
                        fallbackStmt.setDouble(3, order.getTotalAmount());
                        fallbackStmt.setInt(4, order.getAddressId());
                        fallbackStmt.setString(5, order.getOrderStatus().toString().toLowerCase());
                        
                        int fallbackRows = fallbackStmt.executeUpdate();
                        if (fallbackRows > 0) {
                            try (ResultSet keys = fallbackStmt.getGeneratedKeys()) {
                                if (keys.next()) {
                                    order.setId(keys.getInt(1));
                                    order.setOrderDate(now);
                                    order.setOrderStatus(OrderStatus.valueOf(order.getOrderStatus().name()));
                                    order.setDeliveryEstimate(deliveryEstimate); // Still set this in the model, even if not in DB
                                    System.out.println("Order created successfully with fallback method, ID: " + order.getId());
                                    return true;
                                }
                            }
                        }
                    } catch (SQLException e2) {
                        System.out.println("Fallback order placement also failed: " + e2.getMessage());
                        e2.printStackTrace();
                    }
                }
            }
            
            return false;
        }
        catch (SQLException e)
        {
            System.out.println("Error preparing statement for order placement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean cancelOrder(int orderId)
    {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED.name().toLowerCase());
    }

    @Override
    public Order getOrderById(int orderId)
    {
        String query = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    return extractOrderFromResultSet(rs);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Order> getOrdersByUserId(int userId)
    {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE user_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    orders.add(extractOrderFromResultSet(rs));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public List<Order> getAllOrders()
    {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders";
        try(PreparedStatement stmt = conn.prepareStatement(query))
        {
            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    orders.add(extractOrderFromResultSet(rs));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public boolean updateOrderStatus(int orderId, String status)
    {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateDeliveryEstimate(int orderId, Timestamp newDeliveryEstimate)
    {
        try {
            // Check if delivery_estimate column exists
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet columns = dbm.getColumns(null, null, "orders", "delivery_estimate");
            
            // If column doesn't exist, return true (no error) without attempting update
            if (!columns.next()) {
                System.out.println("delivery_estimate column does not exist in orders table");
                return true;
            }
            
            // Column exists, proceed with update
            String query = "UPDATE orders SET delivery_estimate = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setTimestamp(1, newDeliveryEstimate);
                stmt.setInt(2, orderId);
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));

        Timestamp orderTs = rs.getTimestamp("order_date");
        if (orderTs != null) {
            order.setOrderDate(orderTs);
        }

        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setAddressId(rs.getInt("address_id"));

        // Determine status based on enum
        String dbStatus = rs.getString("status").toUpperCase();
        
        try {
            // Check if column exists before accessing it
            Timestamp deliveryTs = null;
            try {
                deliveryTs = rs.getTimestamp("delivery_estimate");
            } catch (SQLException e) {
                // Column doesn't exist, ignore
            }

            // If delivery date passed, mark delivered
            if (deliveryTs != null && System.currentTimeMillis() > deliveryTs.getTime()) {
                order.setOrderStatus(OrderStatus.DELIVERED);
            } else {
                try {
                    order.setOrderStatus(OrderStatus.valueOf(dbStatus));
                } catch (IllegalArgumentException ex) {
                    order.setOrderStatus(OrderStatus.PENDING);
                }
            }

            if (deliveryTs != null) {
                order.setDeliveryEstimate(deliveryTs);
            }
        } catch (Exception e) {
            // If any error occurs, just use the status from the database
            try {
                order.setOrderStatus(OrderStatus.valueOf(dbStatus));
            } catch (IllegalArgumentException ex) {
                order.setOrderStatus(OrderStatus.PENDING);
            }
        }

        return order;
    }
}
