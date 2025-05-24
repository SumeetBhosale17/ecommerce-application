package com.ecommerce.dao;

import com.ecommerce.model.Rating;

import java.util.List;

public interface RatingDAO {
    boolean addRating(Rating rating);
    boolean updateRating(Rating rating);
    boolean deleteRating(int ratingId);
    
    Rating getRatingById(int ratingId);
    Rating getUserProductRating(int userId, int productId);
    List<Rating> getRatingsByProductId(int productId);
    
    double getAverageRatingForProduct(int productId);
    int getCountOfRatingsForProduct(int productId);
} 