package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO
{
    @Override
    public boolean addProduct(Product product)
    {
        String query = "INSERT INTO products (name, description, price, stock, category_id, image_path) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, product.getName());
                stmt.setString(2, product.getDescription());
                stmt.setDouble(3, product.getPrice());
                stmt.setInt(4, product.getStock());
                stmt.setInt(5, product.getCategory_id());
                stmt.setString(6, product.getImagePath());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public boolean deleteProduct(int productId)
    {
        String query = "DELETE FROM products WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, productId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public boolean updateProduct(Product product)
    {
        String query = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, category_id = ?, image_path = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, product.getName());
                stmt.setString(2, product.getDescription());
                stmt.setDouble(3, product.getPrice());
                stmt.setInt(4, product.getStock());
                stmt.setInt(5, product.getCategory_id());
                stmt.setString(6, product.getImagePath());
                stmt.setInt(7, product.getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public Product getProductById(int productId)
    {
        String query = "SELECT * FROM products WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, productId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractProductFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public List<Product> getAllProduct()
    {
        String query = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    products.add(extractProductFromResultSet(rs));
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return products;
    }

    @Override
    public List<Product> getProductsByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE category_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, categoryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        products.add(extractProductFromResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return products;
    }

    @Override
    public List<Product> searchProducts(String keyword)
    {
        String query = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ?";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                String like = "%" + keyword + "%";
                stmt.setString(1, like);
                stmt.setString(2, like);
                try (ResultSet rs = stmt.executeQuery())
                {
                    while (rs.next())
                    {
                        products.add(extractProductFromResultSet(rs));
                    }
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return products;
    }

    @Override
    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice)
    {
        String query = "SELECT * FROM products WHERE price BETWEEN ? AND ?";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setDouble(1, minPrice);
                stmt.setDouble(2, maxPrice);
                try (ResultSet rs = stmt.executeQuery())
                {
                    while (rs.next())
                    {
                        products.add(extractProductFromResultSet(rs));
                    }
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return products;
    }

    @Override
    public List<Product> getProductsSortedByPriceAsc()
    {
        String query = "SELECT * FROM products ORDER BY price";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    products.add(extractProductFromResultSet(rs));
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return products;
    }

    @Override
    public List<Product> getProductSortedByPriceDesc()
    {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products ORDER BY price DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    products.add(extractProductFromResultSet(rs));
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return products;
    }

    @Override
    public List<Product> getSimilarProducts(int productId, int limit)
    {
        List<Product> similarProducts = new ArrayList<>();
        String query = "SELECT p.* FROM products p JOIN products original " +
                "ON p.category_id = original.category_id " +
                "WHERE original.id = ? AND p.id != ? " +
                "ORDER BY RAND() LIMIT ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, productId);
                stmt.setInt(2, productId);
                stmt.setInt(3, limit);
                
                try (ResultSet rs = stmt.executeQuery())
                {
                    while (rs.next())
                    {
                        similarProducts.add(extractProductFromResultSet(rs));
                    }
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return similarProducts;
    }

    @Override
    public boolean updateStock(int productId, int newStock)
    {
        String query = "UPDATE products SET stock = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, newStock);
                stmt.setInt(2, productId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    private Product extractProductFromResultSet(ResultSet rs) throws SQLException
    {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getInt("stock"));
        product.setCategory_id(rs.getInt("category_id"));
        product.setImagePath(rs.getString("image_path"));
        
        // Load ratings data
        RatingDAO ratingDAO = new RatingDAOImpl();
        product.setAverageRating(ratingDAO.getAverageRatingForProduct(product.getId()));
        product.setRatingCount(ratingDAO.getCountOfRatingsForProduct(product.getId()));
        
        return product;
    }
}