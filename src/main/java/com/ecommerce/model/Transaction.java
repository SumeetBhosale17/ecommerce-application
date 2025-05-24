package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction
{
    private int id;
    private int orderId;
    private TransactionMethod method;
    private TransactionStatus status;
    private Timestamp transactionDate;

    public Transaction(int orderId, TransactionMethod method, TransactionStatus status)
    {
        this.method = method;
        this.orderId = orderId;
        this.status = status;

        if (method == TransactionMethod.UPI)
        {
            this.transactionDate = new Timestamp(System.currentTimeMillis());
        }
    }
}
