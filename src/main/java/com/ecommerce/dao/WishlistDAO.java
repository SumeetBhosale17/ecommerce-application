package com.ecommerce.dao;

import java.util.List;

import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Wishlist;

public interface WishlistDAO {
    boolean addToWishlist(Wishlist wishlistItem);
    boolean removeFromWishlist(int userId, int productId);
    boolean isInWishlist(int userId, int productId);
    List<Wishlist> getWishlistByUserId(int userId);
    List<Product> getWishlistProductsByUserId(int userId);
    List<User> getUsersWithProductInWishlist(int productId);
} 