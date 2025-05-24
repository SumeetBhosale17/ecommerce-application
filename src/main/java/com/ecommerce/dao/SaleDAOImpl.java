package com.ecommerce.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Sale;

public class SaleDAOImpl implements SaleDAO
{
    @Override
    public boolean addSale(Sale sale)
    {
        String query = "INSERT INTO sales (name, discount_percent, start_date, end_date, is_active, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, sale.getName());
                stmt.setDouble(2, sale.getDiscountPercent());
                
                stmt.setDate(3, sale.getSqlStartDate());
                stmt.setDate(4, sale.getSqlEndDate());
                
                stmt.setBoolean(5, sale.isActive());
                stmt.setString(6, sale.getStatus());

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
    public boolean updateSale(Sale sale)
    {
        String query = "UPDATE sales SET name = ?, discount_percent = ?, start_date = ?, end_date = ?, is_active = ?, status = ? WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, sale.getName());
                stmt.setDouble(2, sale.getDiscountPercent());
                
                stmt.setDate(3, sale.getSqlStartDate());
                stmt.setDate(4, sale.getSqlEndDate());
                
                stmt.setBoolean(5, sale.isActive());
                stmt.setString(6, sale.getStatus());
                stmt.setInt(7, sale.getId());

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
    public boolean deleteSale(int saleId)
    {
        String query = "DELETE FROM sales WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, saleId);
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
    public Sale getSaleById(int saleId)
    {
        String query = "SELECT * FROM sales WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, saleId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractSaleFromResultSet(rs);
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
    public List<Sale> getAllSale()
    {
        List<Sale> sales = new ArrayList<>();
        String query = "SELECT * FROM sales";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    sales.add(extractSaleFromResultSet(rs));
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
        return sales;
    }

    private Sale extractSaleFromResultSet(ResultSet rs) throws SQLException
    {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        double discountPercent = rs.getDouble("discount_percent");
        Date startDate = rs.getDate("start_date");
        Date endDate = rs.getDate("end_date");
        boolean isActive = rs.getBoolean("is_active");
        String status = null;
        
        try {
            status = rs.getString("status");
        } catch (SQLException e) {
            status = isActive ? "ACTIVE" : "COMPLETED";
        }
        
        return new Sale(id, name, discountPercent, startDate, endDate, isActive, status);
    }

    @Override
    public void updateSaleStatuses()
    {
        String query = "UPDATE sales " +
                "SET is_active = CASE " +
                "WHEN CURRENT_DATE BETWEEN start_date AND end_date THEN true " +
                "ELSE false END, " +
                "status = CASE " +
                "WHEN CURRENT_DATE < start_date THEN 'SCHEDULED' " +
                "WHEN CURRENT_DATE BETWEEN start_date AND end_date THEN 'ACTIVE' " +
                "WHEN CURRENT_DATE > end_date THEN 'COMPLETED' " +
                "ELSE status END";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                int rowsUpdated = stmt.executeUpdate();
                System.out.println(rowsUpdated + " sale statuses updated based on the date.");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error Updating Sale Statuses." + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                DBConnection.releaseConnection(conn);
            }
        }
    }

    @Override
    public Sale getActiveSale()
    {
        String query = "SELECT * FROM sales WHERE is_active = true LIMIT 1";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractSaleFromResultSet(rs);
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
    public List<Sale> getAllSales() {
        return getAllSale();
    }
    
    @Override
    public List<Sale> getActiveSales() {
        List<Sale> activeSales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE is_active = true";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    activeSales.add(extractSaleFromResultSet(rs));
                }
            }
            
            return activeSales;
        } catch (SQLException e) {
            System.err.println("Error getting active sales: " + e.getMessage());
            e.printStackTrace();
            return activeSales;
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
    }
    
    @Override
    public boolean updateSaleStatus(int saleId, String status) {
        String sql = "UPDATE sales SET status = ?, is_active = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, status);
                stmt.setBoolean(2, "ACTIVE".equals(status));
                stmt.setInt(3, saleId);
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating sale status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
    }
}
