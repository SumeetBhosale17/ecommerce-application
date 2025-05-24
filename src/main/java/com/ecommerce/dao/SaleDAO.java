package com.ecommerce.dao;

import java.util.List;

import com.ecommerce.model.Sale;

public interface SaleDAO
{
    boolean addSale(Sale sale);
    boolean updateSale(Sale sale);
    boolean deleteSale(int saleId);
    Sale getSaleById(int saleId);
    List<Sale> getAllSale();
    void updateSaleStatuses();

    Sale getActiveSale();
    
    // Add missing methods that are implemented in SaleDAOImpl
    List<Sale> getAllSales();
    List<Sale> getActiveSales();
    boolean updateSaleStatus(int saleId, String status);
}
