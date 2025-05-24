package com.ecommerce.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Wishlist;

public class WishlistDAOImpl implements WishlistDAO {

    @Override
    public boolean addToWishlist(Wishlist wishlistItem) {
        // Check if already in wishlist
        if (isInWishlist(wishlistItem.getUserId(), wishlistItem.getProductId())) {
            return true; // Already in wishlist
        }
        
        String sql = "INSERT INTO wishlist (user_id, product_id, date_added) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, wishlistItem.getUserId());
            stmt.setInt(2, wishlistItem.getProductId());
            stmt.setTimestamp(3, wishlistItem.getDateAdded());
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        wishlistItem.setId(generatedKeys.getInt(1));
                    }
                }
            }
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeFromWishlist(int userId, int productId) {
        String sql = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isInWishlist(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Wishlist> getWishlistByUserId(int userId) {
        List<Wishlist> wishlistItems = new ArrayList<>();
        String sql = "SELECT * FROM wishlist WHERE user_id = ? ORDER BY date_added DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Wishlist item = new Wishlist();
                    item.setId(rs.getInt("id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setDateAdded(rs.getTimestamp("date_added"));
                    wishlistItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return wishlistItems;
    }

    @Override
    public List<Product> getWishlistProductsByUserId(int userId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.* FROM products p " +
                     "JOIN wishlist w ON p.id = w.product_id " +
                     "WHERE w.user_id = ? " +
                     "ORDER BY w.date_added DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getDouble("price"));
                    product.setStock(rs.getInt("stock"));
                    product.setCategory_id(rs.getInt("category_id"));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return products;
    }

    @Override
    public List<User> getUsersWithProductInWishlist(int productId) {
        List<User> users = new ArrayList<>();
        String query = "SELECT u.* FROM users u " +
                       "JOIN wishlist w ON u.id = w.user_id " +
                       "WHERE w.product_id = ?";
                       
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setContact(rs.getString("contact"));
                
                // Convert string role to UserRole enum
                String roleStr = rs.getString("role");
                if (roleStr != null && roleStr.equalsIgnoreCase("ADMIN")) {
                    user.setRole(com.ecommerce.model.UserRole.ADMIN);
                } else {
                    user.setRole(com.ecommerce.model.UserRole.USER);
                }
                
                users.add(user);
            }
            
            System.out.println("Found " + users.size() + " users with product ID " + productId + " in their wishlist");
            return users;
        } catch (SQLException e) {
            System.err.println("Error getting users with product in wishlist: " + e.getMessage());
            e.printStackTrace();
            return users;
        }
    }
} 