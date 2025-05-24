package com.ecommerce.dao;

import com.ecommerce.model.Category;

import java.util.List;

public interface CategoryDAO
{
    boolean addCategory(Category category);
    boolean deleteCategory(int categoryId);

    boolean addSubCategory(String name, int parentId);
    boolean deleteSubCategory(int categoryId);

    Category getCategory(int categoryId);
    Category getCategoryById(int categoryId);
    Category getSubCategory(int categoryId, int parentId);

    List<Category> getAllCategories();
    List<Category> getAllSubCategories(int parentId);

    boolean updateCategory(Category category);
}
