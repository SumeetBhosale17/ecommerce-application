package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO
{
    @Override
    public boolean addCategory(Category category)
    {
        String query = "INSERT INTO categories (name, parent_id) VALUES (?, ?)";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, category.getName());
                if (category.getParentId() == null) {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(2, category.getParentId());
                }
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public boolean deleteCategory(int categoryId)
    {
        String query = "DELETE FROM categories WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, categoryId);
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public boolean addSubCategory(String name, int parentId)
    {
        String query = "INSERT INTO categories (name, parent_id) VALUES (?, ?)";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, name);
                stmt.setInt(2, parentId);
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public boolean deleteSubCategory(int categoryId)
    {
        String query = "DELETE FROM categories WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, categoryId);
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public Category getCategory(int categoryId)
    {
        String query = "SELECT * FROM categories WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, categoryId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractCategoryFromResultSet(rs);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public Category getSubCategory(int categoryId, int parentId)
    {
        String query = "SELECT * FROM categories WHERE id = ? AND parent_id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, categoryId);
                stmt.setInt(2, parentId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractCategoryFromResultSet(rs);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public List<Category> getAllCategories()
    {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM categories";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    categories.add(extractCategoryFromResultSet(rs));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return categories;
    }

    @Override
    public List<Category> getAllSubCategories(int parentId)
    {
        String query = "SELECT * FROM categories WHERE parent_id = ?";
        List<Category> subCategories = new ArrayList<>();
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, parentId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    while (rs.next())
                    {
                        subCategories.add(extractCategoryFromResultSet(rs));
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return subCategories;
    }

    @Override
    public Category getCategoryById(int categoryId)
    {
        String query = "SELECT * FROM categories WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, categoryId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractCategoryFromResultSet(rs);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public boolean updateCategory(Category category)
    {
        String query = "UPDATE categories SET name = ?, parent_id = ? WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, category.getName());
                if (category.getParentId() == null) {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(2, category.getParentId());
                }
                stmt.setInt(3, category.getId());
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    private Category extractCategoryFromResultSet(ResultSet rs) throws SQLException
    {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        int parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) {
            category.setParentId(parentId);
        }
        return category;
    }
}
