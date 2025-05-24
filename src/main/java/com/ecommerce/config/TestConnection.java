package com.ecommerce.config;

import java.sql.Connection;

public class TestConnection
{
    public static void main(String[] args)
    {
        try (Connection conn = DBConnection.getConnection())
        {
            System.out.println("✅ Connection successful: " + conn);
        }
        catch (Exception e)
        {
            System.out.println("❌ Connection failed:");
            e.printStackTrace();
        }
    }
}
