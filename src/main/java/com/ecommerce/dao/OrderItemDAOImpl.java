package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAOImpl implements OrderItemDAO
{
    private Connection connection;
    public OrderItemDAOImpl() {
        try {
            this.connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addOrderItem(OrderItem item)
    {
        String query = "INSERT INTO order_items (order_id, product_id, product_name, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setString(3, item.getProductName());
            stmt.setInt(4, item.getQuantity());
            stmt.setDouble(5, item.getPrice());

            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<OrderItem> getItemsByOrderId(int orderId)
    {
        List<OrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, orderId);
            try(ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    items.add(extractOrderItem(rs));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public boolean deleteItem(int orderItemId)
    {
        String query = "DELETE FROM order_items WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, orderItemId);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteItemsByOrderId(int orderId)
    {
        String query = "DELETE FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateQuantity(int quantity, int orderItemId)
    {
        String query = "UPDATE order_items SET quantity = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, quantity);
            stmt.setInt(2, orderItemId);

            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateOrderItem(OrderItem item)
    {
        String query = """
                UPDATE order_items
                    SET product_id = ?,
                        product_name = ?,
                        quantity = ?,
                        price = ?
                    WHERE id = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, item.getProductId());
            stmt.setString(2, item.getProductName());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getPrice());
            stmt.setInt(5, item.getId());

            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<OrderItem> getAllOrderItems() {
        List<OrderItem> orderItems = new ArrayList<>();
        String query = "SELECT * FROM order_items";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("price"));
                orderItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderItems;
    }

    private OrderItem extractOrderItem(ResultSet rs) throws SQLException
    {
        OrderItem item = new OrderItem();
        item.setId(rs.getInt("id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setPrice(rs.getDouble("price"));
        return item;
    }
}
