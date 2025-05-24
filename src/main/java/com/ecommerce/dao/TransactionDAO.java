package com.ecommerce.dao;

import com.ecommerce.model.Transaction;
import com.ecommerce.model.TransactionStatus;

import java.util.List;

public interface TransactionDAO
{
    boolean addTransaction(Transaction transaction);
    boolean updateTransactionStatus(int transactionId, TransactionStatus transactionStatus);
    Transaction getTransactionByOrderId(int orderId);
    List<Transaction> getAllTransactions();
}
