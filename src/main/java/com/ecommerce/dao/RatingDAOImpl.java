package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Rating;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingDAOImpl implements RatingDAO {

    @Override
    public boolean addRating(Rating rating) {
        // Check if user already rated this product
        Rating existingRating = getUserProductRating(rating.getUserId(), rating.getProductId());
        if (existingRating != null) {
            // Update existing rating instead
            rating.setId(existingRating.getId());
            return updateRating(rating);
        }
        
        String sql = "INSERT INTO ratings (user_id, product_id, value, comment, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, rating.getUserId());
            stmt.setInt(2, rating.getProductId());
            stmt.setInt(3, rating.getValue());
            stmt.setString(4, rating.getComment());
            stmt.setTimestamp(5, rating.getCreatedAt());
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        rating.setId(rs.getInt(1));
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
    public boolean updateRating(Rating rating) {
        String sql = "UPDATE ratings SET value = ?, comment = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, rating.getValue());
            stmt.setString(2, rating.getComment());
            stmt.setInt(3, rating.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteRating(int ratingId) {
        String sql = "DELETE FROM ratings WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ratingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Rating getRatingById(int ratingId) {
        String sql = "SELECT * FROM ratings WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ratingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractRatingFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Rating getUserProductRating(int userId, int productId) {
        String sql = "SELECT * FROM ratings WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractRatingFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Rating> getRatingsByProductId(int productId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE product_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ratings.add(extractRatingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return ratings;
    }

    @Override
    public double getAverageRatingForProduct(int productId) {
        String sql = "SELECT AVG(value) as average FROM ratings WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("average");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public int getCountOfRatingsForProduct(int productId) {
        String sql = "SELECT COUNT(*) as count FROM ratings WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private Rating extractRatingFromResultSet(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getInt("id"));
        rating.setUserId(rs.getInt("user_id"));
        rating.setProductId(rs.getInt("product_id"));
        rating.setValue(rs.getInt("value"));
        rating.setComment(rs.getString("comment"));
        rating.setCreatedAt(rs.getTimestamp("created_at"));
        return rating;
    }
} 