package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.dao.CartItemDAO;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartItemDAOImpl implements CartItemDAO {

    @Override
    public boolean addToCart(Product product) {
        // This method is not properly implemented - it should take a user ID
        // For now, we'll assume there's a current user ID being used elsewhere
        System.err.println("Warning: addToCart method is being called without a user ID. This will not work correctly.");
        return false;
    }

    @Override
    public boolean removeFromCart(Product product) {
        // This method is not properly implemented - it should take a user ID
        System.err.println("Warning: removeFromCart method is being called without a user ID. This will not work correctly.");
        return false;
    }

    @Override
    public boolean updateQuantity(int quantity) {
        // This method is not properly implemented - it should take a product ID and user ID
        System.err.println("Warning: updateQuantity method is being called without proper parameters. This will not work correctly.");
        return false;
    }

    @Override
    public List<CartItem> getCartItemsByUserId(int userId) {
        String sql = "SELECT * FROM cart_items WHERE user_id = ?";
        List<CartItem> cartItems = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CartItem cartItem = extractCartItem(rs);
                cartItems.add(cartItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    @Override
    public CartItem getCartItem(int cartItemId) {
        String sql = "SELECT * FROM cart_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cartItemId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractCartItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean clearCartByUserId(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Product getProduct(CartItem item) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getProductId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CartItem extractCartItem(ResultSet rs) throws SQLException {
        CartItem cartItem = new CartItem();
        cartItem.setId(rs.getInt("id"));
        cartItem.setUserId(rs.getInt("user_id"));
        cartItem.setProductId(rs.getInt("product_id"));
        cartItem.setQuantity(rs.getInt("quantity"));
        return cartItem;
    }

    private Product extractProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getInt("stock"));
        return product;
    }

    @Override
    public CartItem getCartItemByUserAndProductId(int userId, int productId) {
        String sql = "SELECT * FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractCartItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateCartItem(CartItem cartItem) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cartItem.getQuantity());
            stmt.setInt(2, cartItem.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addCartItem(CartItem cartItem) {
        // First check if the item already exists in the cart
        CartItem existingItem = getCartItemByUserAndProductId(cartItem.getUserId(), cartItem.getProductId());
        
        if (existingItem != null) {
            // If item exists, update the quantity
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
            return updateCartItem(existingItem);
        } else {
            // Insert new cart item
            String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    
                stmt.setInt(1, cartItem.getUserId());
                stmt.setInt(2, cartItem.getProductId());
                stmt.setInt(3, cartItem.getQuantity());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    // Get the generated ID and set it on the cart item
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        cartItem.setId(generatedKeys.getInt(1));
                    }
                }
                return result > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public boolean removeCartItem(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, cartItemId);
            int rowsAffected = stmt.executeUpdate();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing cart item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
