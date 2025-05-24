package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Address;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddressDAOImpl implements AddressDAO
{
    @Override
    public boolean addAddress(Address address)
    {
        String query = "INSERT INTO addresses(user_id, address, city, state, pincode) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS))
            {
                stmt.setInt(1, address.getUserId());
                stmt.setString(2, address.getStreet());
                stmt.setString(3, address.getCity());
                stmt.setString(4, address.getState());
                stmt.setString(5, address.getPincode());

                int result = stmt.executeUpdate();
                if (result > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            address.setId(generatedKeys.getInt(1));
                        }
                    }
                }
                return result > 0;
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public boolean removeAddress(int addressId)
    {
        String query = "DELETE FROM addresses WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, addressId);
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public boolean updateAddress(Address address)
    {
        String query = "UPDATE addresses SET address = ?, city = ?, state = ?, pincode = ? WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, address.getStreet());
                stmt.setString(2, address.getCity());
                stmt.setString(3, address.getState());
                stmt.setString(4, address.getPincode());
                stmt.setInt(5, address.getId());
                
                return stmt.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return false;
    }

    @Override
    public Address getAddressById(int addressId)
    {
        String query = "SELECT * FROM addresses WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, addressId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                    {
                        return extractAddressFromResultSet(rs);
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
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public Address getAddressByUserId(int userId)
    {
        String query = "SELECT * FROM addresses WHERE user_id = ? LIMIT 1";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractAddressFromResultSet(rs);
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
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        return null;
    }
    
    @Override
    public List<Address> getAddressesByUserId(int userId) {
        List<Address> addresses = new ArrayList<>();
        String query = "SELECT * FROM addresses WHERE user_id = ?";
        Connection conn = null;
        
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        addresses.add(extractAddressFromResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally
        {
            if (conn != null) {
                DBConnection.releaseConnection(conn);
            }
        }
        
        return addresses;
    }
    
    private Address extractAddressFromResultSet(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setId(rs.getInt("id"));
        address.setUserId(rs.getInt("user_id"));
        address.setStreet(rs.getString("address"));
        address.setCity(rs.getString("city"));
        address.setState(rs.getString("state"));
        address.setPincode(rs.getString("pincode"));
        return address;
    }
}
