package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product
{
    private int id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private Integer category_id;
    private String imagePath;
    
    // Rating related fields (transient - not stored directly in database)
    private double averageRating;
    private int ratingCount;

    public Product(String name, String description, double price, int stock, Integer category_id, String imagePath)
    {
        this.category_id = category_id;
        this.description = description;
        this.name = name;
        this.imagePath = imagePath;
        this.price = price;
        this.stock = stock;
    }

    public int getCategoryId() {
        return category_id;
    }
}
