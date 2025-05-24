package com.ecommerce.dao;

import com.ecommerce.config.DBConnection;
import com.ecommerce.model.Transaction;
import com.ecommerce.model.TransactionMethod;
import com.ecommerce.model.TransactionStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO
{
    @Override
    public boolean addTransaction(Transaction transaction)
    {
        String query = "INSERT INTO transactions (order_id, method, status, transaction_date) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
            {
                stmt.setInt(1, transaction.getOrderId());
                stmt.setString(2, transaction.getMethod().name());
                stmt.setString(3, transaction.getStatus().name());
                stmt.setTimestamp(4, transaction.getTransactionDate());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0)
                {
                    return false;
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys())
                {
                    if (generatedKeys.next())
                    {
                        transaction.setId(generatedKeys.getInt(1));
                        return true;
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
        return false;
    }

    @Override
    public boolean updateTransactionStatus(int transactionId, TransactionStatus transactionStatus)
    {
        String query = "UPDATE transactions SET status = ? WHERE id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setString(1, transactionStatus.name());
                stmt.setInt(2, transactionId);
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
    public Transaction getTransactionByOrderId(int orderId)
    {
        String query = "SELECT * FROM transactions WHERE order_id = ?";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query))
            {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery())
                {
                    if (rs.next())
                    {
                        return extractTransactionFromResultSet(rs);
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
    public List<Transaction> getAllTransactions()
    {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions";
        Connection conn = null;
        try
        {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    transactions.add(extractTransactionFromResultSet(rs));
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
        return transactions;
    }

    private Transaction extractTransactionFromResultSet(ResultSet rs) throws SQLException
    {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setOrderId(rs.getInt("order_id"));
        transaction.setMethod(TransactionMethod.valueOf(rs.getString("method")));
        transaction.setStatus(TransactionStatus.valueOf(rs.getString("status").toUpperCase()));
        transaction.setTransactionDate(rs.getTimestamp("transaction_date"));
        return transaction;
    }
}
