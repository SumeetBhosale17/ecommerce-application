package com.ecommerce.service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.config.AppConfig;
import com.ecommerce.dao.CartItemDAO;
import com.ecommerce.dao.CartItemDAOImpl;
import com.ecommerce.dao.NotificationDAO;
import com.ecommerce.dao.NotificationDAOImpl;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.OrderDAOImpl;
import com.ecommerce.dao.OrderItemDAO;
import com.ecommerce.dao.OrderItemDAOImpl;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ProductDAOImpl;
import com.ecommerce.dao.SaleDAO;
import com.ecommerce.dao.SaleDAOImpl;
import com.ecommerce.dao.TransactionDAO;
import com.ecommerce.dao.TransactionDAOImpl;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Notification;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Product;
import com.ecommerce.model.Sale;
import com.ecommerce.model.Transaction;
import com.ecommerce.model.TransactionMethod;
import com.ecommerce.model.TransactionStatus;
import com.ecommerce.scheduler.StockCheckScheduler;
import com.ecommerce.utils.EmailSender;
import com.ecommerce.utils.ReceiptGenerator;

public class OrderService
{
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final CartItemDAO cartItemDAO;
    private final TransactionDAO transactionDAO;
    private final SaleDAO saleDAO;
    private final ProductDAO productDAO;
    private final NotificationDAO notificationDAO;

    public OrderService()
    {
        this.orderDAO = new OrderDAOImpl();
        this.orderItemDAO = new OrderItemDAOImpl();
        this.cartItemDAO = new CartItemDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
        this.saleDAO = new SaleDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.notificationDAO = new NotificationDAOImpl();
    }

    public OrderService(OrderDAO orderDAO, OrderItemDAO orderItemDAO, CartItemDAO cartItemDAO, TransactionDAO transactionDAO, SaleDAO saleDAO, ProductDAO productDAO, NotificationDAO notificationDAO)
    {
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.cartItemDAO = cartItemDAO;
        this.transactionDAO = transactionDAO;
        this.saleDAO = saleDAO;
        this.productDAO = productDAO;
        this.notificationDAO = notificationDAO;
    }

    public boolean placeOrder(int userId, int addressId, String transactionMethod, String email, String username, String city, String state, String pincode)
    {
        System.out.println("Starting order placement process...");
        try
        {
            // Verify all parameters
            System.out.println("Processing order for User ID: " + userId + ", Address ID: " + addressId);
            System.out.println("Transaction Method: " + transactionMethod);
            System.out.println("Customer Email: " + email + ", Username: " + username);
            System.out.println("Shipping to: " + city + ", " + state + ", " + pincode);
            
            List<CartItem> cartItems = cartItemDAO.getCartItemsByUserId(userId);
            if (cartItems.isEmpty())
            {
                System.out.println("Cart is empty. Cannot place order.");
                return false;
            }
            
            System.out.println("Found " + cartItems.size() + " items in cart");

            // Check for active sale
            Sale activeSale = saleDAO.getActiveSale();
            double discountPercent = 0;
            if (activeSale != null)
            {
                // Use the direct getter methods from the updated Sale class
                discountPercent = activeSale.getDiscountPercent();
                System.out.println("Active sale found: " + activeSale.getName() + 
                         ", Discount: " + discountPercent + "%");
            }

            // Calculate total
            double totalAmount = 0;
            for (CartItem cartItem : cartItems)
            {
                Product product = productDAO.getProductById(cartItem.getProductId());
                if (product == null) {
                    System.out.println("Product not found for cart item: " + cartItem.getId());
                    continue;
                }
                
                double productPrice = product.getPrice();
                double itemDiscount = productPrice * (discountPercent / 100.0);
                double finalPrice = (productPrice - itemDiscount) * cartItem.getQuantity();
                totalAmount += finalPrice;
                
                System.out.println("Item: " + product.getName() + ", Price: " + productPrice + 
                        ", Discount: " + itemDiscount + ", Quantity: " + cartItem.getQuantity() + 
                        ", Final price: " + finalPrice);
            }
            
            System.out.println("Total order amount: " + totalAmount);

            // Create order
            System.out.println("Creating order record in database...");
            Order order = new Order();
            order.setUserId(userId);
            order.setAddressId(addressId);
            order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
            order.setTotalAmount(totalAmount);
            order.setOrderStatus(OrderStatus.PENDING);
            
            // We will let the DAO handle setting the delivery_estimate
            // This avoids issues with database column naming

            boolean orderPlaced = orderDAO.placeOrder(order);
            if (!orderPlaced)
            {
                System.out.println("Failed to place order in database");
                return false;
            }

            int orderId = order.getId();
            System.out.println("Order created successfully with ID: " + orderId);
            
            if (orderId <= 0) {
                System.out.println("Invalid order ID: " + orderId);
                return false;
            }

            try {
                // Create OrderItems and update product stock
                System.out.println("Creating order item records...");
                for (CartItem cartItem : cartItems)
                {
                    Product product = productDAO.getProductById(cartItem.getProductId());
                    if (product == null) {
                        System.out.println("Product not found for cart item: " + cartItem.getId());
                        continue;
                    }
                    
                    double productPrice = product.getPrice();
                    double itemDiscount = productPrice * (discountPercent / 100.0);

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(orderId);
                    orderItem.setProductId(cartItem.getProductId());
                    orderItem.setProductName(product.getName());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(productPrice);
                    
                    try {
                        System.out.println("Adding order item for product: " + product.getName());
                        boolean itemAdded = orderItemDAO.addOrderItem(orderItem);
                        if (!itemAdded) {
                            System.out.println("Failed to add order item for product: " + product.getName());
                        } else {
                            System.out.println("Order item added successfully");
                        }
                    } catch (Exception ex) {
                        System.out.println("Error adding order item: " + ex.getMessage());
                    }

                    // Reduce stock
                    System.out.println("Updating stock for product: " + product.getName() + 
                            " from " + product.getStock() + " to " + (product.getStock() - cartItem.getQuantity()));
                    int newStock = product.getStock() - cartItem.getQuantity();
                    if (newStock < 0) {
                        newStock = 0; // Prevent negative stock
                        System.out.println("Warning: Insufficient stock for product: " + product.getName() + ". Setting to 0.");
                    }
                    
                    boolean stockUpdated = productDAO.updateStock(product.getId(), newStock);
                    if (stockUpdated) {
                        System.out.println("Stock updated successfully for product: " + product.getName());
                        
                        // Check if stock is low and send notification if needed
                        if (newStock <= AppConfig.LOW_STOCK_THRESHOLD) {
                            System.out.println("Product ID " + product.getId() + " has low stock (" + newStock + 
                                             "). Sending notifications.");
                            // Update the product object with new stock before sending notification
                            product.setStock(newStock);
                            NotificationService.sendLowStockNotification(product);
                        }
                    } else {
                        System.out.println("Failed to update stock for product: " + product.getName());
                    }
                }

                // Clear user's cart after order placement
                boolean cleared = cartItemDAO.clearCartByUserId(userId);
                System.out.println("Cart cleared for user ID " + userId + ": " + cleared);

                // Create Transaction
                try {
                    System.out.println("Creating transaction record...");
                    Transaction transaction = new Transaction();
                    transaction.setOrderId(orderId);
                    
                    // Handle different payment method formats correctly
                    if (transactionMethod.equalsIgnoreCase("Pay on Delivery")) {
                        transaction.setMethod(TransactionMethod.PAY_ON_DELIVERY);
                    } else {
                        // For other methods, normalize and convert to enum
                        String normalizedMethod = transactionMethod.toUpperCase().replace(" ", "_");
                        transaction.setMethod(TransactionMethod.valueOf(normalizedMethod));
                    }
                    
                    transaction.setStatus(TransactionStatus.PENDING);
                    transaction.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));
                    boolean transactionCreated = transactionDAO.addTransaction(transaction);
                    if (transactionCreated) {
                        System.out.println("Transaction created successfully");
                    } else {
                        System.out.println("Failed to create transaction record");
                    }
                } catch (Exception ex) {
                    System.out.println("Error creating transaction: " + ex.getMessage());
                    ex.printStackTrace();
                    // Continue processing even if transaction fails
                }

                // Send Notifications
                try {
                    System.out.println("Creating notification for user...");
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setMessage("Your order #" + orderId + " has been placed successfully!");
                    notification.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    notification.setRead(false);
                    boolean notificationAdded = notificationDAO.addNotification(notification);
                    if (notificationAdded) {
                        System.out.println("Notification created successfully");
                    } else {
                        System.out.println("Failed to create notification");
                    }
                } catch (Exception ex) {
                    System.out.println("Error creating notification: " + ex.getMessage());
                    ex.printStackTrace();
                    // Continue processing even if notification fails
                }

                // Generate receipt
                try {
                    System.out.println("Generating receipt...");
                    // Create OrderReceipts directory if it doesn't exist
                    File receiptDir = new File("OrderReceipts");
                    if (!receiptDir.exists()) {
                        boolean dirCreated = receiptDir.mkdirs();
                        if (!dirCreated) {
                            System.out.println("Failed to create OrderReceipts directory");
                            throw new IOException("Could not create OrderReceipts directory");
                        }
                    }
                    
                    // Get the transaction we just created
                    Transaction transaction = transactionDAO.getTransactionByOrderId(orderId);
                    
                    if (transaction == null) {
                        System.out.println("Transaction not found for order ID: " + orderId + ", creating dummy transaction");
                        // Create a dummy transaction if none found
                        transaction = new Transaction();
                        transaction.setId(0);
                        transaction.setOrderId(orderId);
                        
                        // Handle different payment method formats correctly
                        if (transactionMethod.equalsIgnoreCase("Pay on Delivery")) {
                            transaction.setMethod(TransactionMethod.PAY_ON_DELIVERY);
                        } else {
                            // For other methods, normalize and convert to enum
                            String normalizedMethod = transactionMethod.toUpperCase().replace(" ", "_");
                            transaction.setMethod(TransactionMethod.valueOf(normalizedMethod));
                        }
                        
                        transaction.setStatus(TransactionStatus.PENDING);
                        transaction.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));
                    }
                    
                    // Generate receipt locally and email 
                    List<OrderItem> orderItems = orderItemDAO.getItemsByOrderId(orderId);
                    if (orderItems.isEmpty()) {
                        System.out.println("No order items found for order ID: " + orderId);
                        throw new IOException("No order items found for receipt generation");
                    }
                    System.out.println("Found " + orderItems.size() + " order items for receipt");
                    
                    // Get all necessary info for the receipt
                    String receiptPath = ReceiptGenerator.generateReceipt(order, orderItems, transaction, username, city, state, pincode);
                    if (receiptPath == null || receiptPath.isEmpty()) {
                        System.out.println("Failed to generate receipt - no path returned");
                        throw new IOException("Failed to generate receipt");
                    }
                    System.out.println("Receipt generated at: " + receiptPath);
                    
                    // Verify receipt file exists
                    File receiptFileCheck = new File(receiptPath);
                    if (!receiptFileCheck.exists()) {
                        System.out.println("Receipt file not found at path: " + receiptPath);
                        throw new IOException("Receipt file not found");
                    }
                    
                    // Store receipt path in a database or other persistent storage if needed
                    // This would allow retrieving the receipt later
                    
                    // Send Email with receipt attachment
                    System.out.println("Preparing to send order confirmation email to: " + email);
                    try {
                        EmailSender emailSender = new EmailSender();
                        
                        // Email content
                        String emailSubject = "Order Confirmation - Order #" + orderId;
                        StringBuilder emailBody = new StringBuilder();
                        emailBody.append("Dear ").append(username).append(",\n\n");
                        emailBody.append("Thank you for your order! Your order #").append(orderId).append(" has been confirmed.\n\n");
                        emailBody.append("Order Details:\n");
                        emailBody.append("Order Date: ").append(order.getOrderDate()).append("\n");
                        emailBody.append("Total Amount: Rs. ").append(String.format("%.2f", order.getTotalAmount())).append("\n");
                        emailBody.append("Payment Method: ").append(transactionMethod).append("\n\n");
                        emailBody.append("Shipping Address:\n");
                        emailBody.append(city).append(", ").append(state).append(" - ").append(pincode).append("\n\n");
                        emailBody.append("Thank you for shopping with us!\n\n");
                        emailBody.append("ZOROJURO DELIVERY EXPRESS LTD\n");
                        emailBody.append("Customer Support: support@zorojuro.com");
                        
                        // Verify the receipt file exists and is readable
                        File receiptFile = new File(receiptPath);
                        if (!receiptFile.exists() || !receiptFile.canRead()) {
                            System.out.println("Warning: Receipt file is not accessible: " + receiptPath);
                            System.out.println("File exists: " + receiptFile.exists() + ", Can read: " + receiptFile.canRead());
                            // Try to send email without attachment
                            boolean emailSent = emailSender.sendEmail(email, emailSubject, emailBody.toString(), null);
                            if (emailSent) {
                                System.out.println("Email sent successfully (without attachment)");
                            } else {
                                System.out.println("Failed to send email (without attachment)");
                                // Log the error but continue with order processing
                            }
                        } else {
                            // Send with attachment
                            System.out.println("Sending email with receipt attachment: " + receiptPath + " (Size: " + receiptFile.length() + " bytes)");
                            boolean emailSent = emailSender.sendEmail(email, emailSubject, emailBody.toString(), receiptPath);
                            if (emailSent) {
                                System.out.println("Email sent successfully with receipt attachment");
                            } else {
                                System.out.println("Failed to send email with attachment - trying without attachment");
                                // Fallback: try to send without attachment
                                emailSent = emailSender.sendEmail(email, emailSubject, emailBody.toString(), null);
                                if (emailSent) {
                                    System.out.println("Fallback email sent successfully (without attachment)");
                                } else {
                                    System.out.println("Failed to send email even without attachment");
                                    // Log the error but continue with order processing
                                }
                            }
                        }
                    } catch (Exception emailEx) {
                        System.out.println("Error in email sending process: " + emailEx.getMessage());
                        emailEx.printStackTrace();
                        // Don't fail the order if email fails
                    }
                } catch (Exception ex) {
                    System.out.println("Error in receipt generation or email sending: " + ex.getMessage());
                    ex.printStackTrace();
                    // Don't fail the order if receipt/email fails
                    // Just log the error and continue
                }

                System.out.println("Order placement completed successfully");
                
                // Run stock check after order placement
                try {
                    System.out.println("Running stock check after order placement...");
                    StockCheckScheduler stockChecker = new StockCheckScheduler();
                    stockChecker.checkProductStock();  // Run a single check without starting the scheduler
                    System.out.println("Stock check completed after order placement");
                } catch (Exception ex) {
                    System.out.println("Error running stock check after order: " + ex.getMessage());
                    ex.printStackTrace();
                    // Don't fail the order if stock check fails
                }
                
                return true;
            } catch (Exception ex) {
                System.out.println("Error in order processing: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }
        catch (Exception e)
        {
            System.out.println("Error placing order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
