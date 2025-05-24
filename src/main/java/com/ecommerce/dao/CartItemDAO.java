package com.ecommerce.dao;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;

import java.util.List;

public interface CartItemDAO
{
    boolean addToCart(Product product);
    boolean removeFromCart(Product product);
    boolean updateQuantity(int quantity);
    List<CartItem> getCartItemsByUserId(int userId);
    CartItem getCartItem(int cartItemId);
    boolean clearCartByUserId(int userId);
    Product getProduct(CartItem item);
    
    // New methods
    CartItem getCartItemByUserAndProductId(int userId, int productId);
    boolean updateCartItem(CartItem cartItem);
    boolean addCartItem(CartItem cartItem);
    
    /**
     * Removes the cart item with the specified ID
     * @param cartItemId the ID of the cart item to remove
     * @return true if successful, false otherwise
     */
    boolean removeCartItem(int cartItemId);
}
