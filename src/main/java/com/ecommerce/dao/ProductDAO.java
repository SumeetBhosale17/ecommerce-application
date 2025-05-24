package com.ecommerce.dao;

import com.ecommerce.model.Product;

import java.util.List;

public interface ProductDAO
{
    boolean addProduct(Product product);
    boolean deleteProduct(int productId);
    boolean updateProduct(Product product);
    boolean updateStock(int productId, int newStock);

    Product getProductById(int productId);

    List<Product> getAllProduct();
    List<Product> getProductsByCategoryId(int categoryId);
    List<Product> searchProducts(String keyword);

    List<Product> getProductsByPriceRange(double minPrice, double maxPrice);
    List<Product> getProductsSortedByPriceAsc();
    List<Product> getProductSortedByPriceDesc();

    List<Product> getSimilarProducts(int productId, int limit);
}