package com.ecommerce.view;

import com.ecommerce.dao.*;
import com.ecommerce.model.*;
import com.ecommerce.util.ThemeManager;
import com.ecommerce.utils.ReceiptGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;

public class OrderDetailsDialog extends JDialog {
    private final int orderId;
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final ProductDAO productDAO;
    private final AddressDAO addressDAO;
    private final TransactionDAO transactionDAO;
    
    public OrderDetailsDialog(Window owner, int orderId) {
        super(owner, "Order Details - #" + orderId, ModalityType.APPLICATION_MODAL);
        this.orderId = orderId;
        this.orderDAO = new OrderDAOImpl();
        this.orderItemDAO = new OrderItemDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.addressDAO = new AddressDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
        
        setupUI();
    }
    
    private void setupUI() {
        setSize(700, 500);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        
        // Get order information
        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            JOptionPane.showMessageDialog(this, 
                    "Error loading order details. Order not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = dateFormat.format(order.getOrderDate());
        
        // Get order items
        List<OrderItem> orderItems = orderItemDAO.getItemsByOrderId(orderId);
        
        // Get payment method from transaction
        Transaction transaction = transactionDAO.getTransactionByOrderId(orderId);
        String paymentMethod = "Standard Payment";
        if (transaction != null) {
            paymentMethod = transaction.getMethod().toString();
        }
        
        // Get delivery address
        Address address = addressDAO.getAddressById(order.getAddressId());
        
        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Order header information
        JPanel headerPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        headerPanel.add(new JLabel("Order ID:"));
        headerPanel.add(new JLabel("#" + order.getId()));
        
        headerPanel.add(new JLabel("Date:"));
        headerPanel.add(new JLabel(formattedDate));
        
        headerPanel.add(new JLabel("Status:"));
        String orderStatus = "Processing";
        if (order.getOrderStatus() != null) {
            orderStatus = order.getOrderStatus().toString();
        }
        JLabel statusLabel = new JLabel(orderStatus);
        if ("DELIVERED".equals(orderStatus)) {
            statusLabel.setForeground(new Color(40, 167, 69)); // Green for delivered
        } else if ("CANCELLED".equals(orderStatus)) {
            statusLabel.setForeground(new Color(220, 53, 69)); // Red for cancelled
        } else if ("SHIPPED".equals(orderStatus)) {
            statusLabel.setForeground(new Color(0, 123, 255)); // Blue for shipped
        } else {
            statusLabel.setForeground(new Color(255, 193, 7)); // Orange for processing/pending
        }
        headerPanel.add(statusLabel);
        
        headerPanel.add(new JLabel("Payment Method:"));
        headerPanel.add(new JLabel(paymentMethod));
        
        headerPanel.add(new JLabel("Total Amount:"));
        headerPanel.add(new JLabel("Rs. " + String.format("%.2f", order.getTotalAmount())));
        
        // Add address information if available
        if (address != null) {
            headerPanel.add(new JLabel("Delivery Address:"));
            String addressText = String.format("%s, %s, %s - %s", 
                    address.getStreet(), 
                    address.getCity(), 
                    address.getState(), 
                    address.getPincode());
            headerPanel.add(new JLabel(addressText));
        }
        
        // Order items table
        String[] columnNames = {"Product", "Price", "Quantity", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        JTable itemsTable = new JTable(model);
        itemsTable.setRowHeight(30);
        
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Order Items"));
        
        // Add items to table
        double total = 0.0;
        for (OrderItem item : orderItems) {
            Product product = productDAO.getProductById(item.getProductId());
            if (product != null) {
                double price = item.getPrice();
                int quantity = item.getQuantity();
                double subtotal = price * quantity;
                total += subtotal;
                
                model.addRow(new Object[]{
                    product.getName(),
                    "Rs. " + String.format("%.2f", price),
                    quantity,
                    "Rs. " + String.format("%.2f", subtotal)
                });
            }
        }
        
        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JButton viewReceiptButton = new JButton("View Receipt");
        viewReceiptButton.addActionListener(e -> {
            try {
                File receiptDir = new File("OrderReceipts");
                if (!receiptDir.exists()) {
                    receiptDir.mkdirs();
                }
                
                // Try to find an existing receipt
                final String receiptPrefix = "OrderReceipt_" + orderId + "_";
                File[] files = receiptDir.listFiles((dir, name) -> name.startsWith(receiptPrefix) && name.endsWith(".pdf"));
                
                if (files != null && files.length > 0) {
                    // Sort by last modified (newest first)
                    java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                    
                    // Open the receipt
                    try {
                        Desktop.getDesktop().open(files[0]);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                            "Could not open receipt: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // No receipt found, generate a new one
                    int choice = JOptionPane.showConfirmDialog(this,
                        "Receipt file not found. Do you want to generate a new receipt?",
                        "Receipt Not Found",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        // Get all necessary info
                        String username = "Customer"; // This should ideally come from the user object
                        String city = "", state = "", pincode = "";
                        if (address != null) {
                            city = address.getCity();
                            state = address.getState();
                            pincode = address.getPincode();
                        }
                        
                        // Generate receipt
                        try {
                            String receiptPath = ReceiptGenerator.generateReceipt(order, orderItems, 
                                transaction, username, city, state, pincode);
                            
                            if (receiptPath != null) {
                                File receiptFile = new File(receiptPath);
                                if (receiptFile.exists()) {
                                    Desktop.getDesktop().open(receiptFile);
                                    JOptionPane.showMessageDialog(this,
                                        "Receipt generated successfully!",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(this,
                                        "Receipt was generated but file not found at: " + receiptPath,
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(this,
                                    "Failed to generate receipt.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(this,
                                "Error generating receipt: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error processing receipt: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton downloadReceiptButton = new JButton("Download Receipt");
        downloadReceiptButton.addActionListener(e -> {
            try {
                // Create a file chooser dialog
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Receipt");
                fileChooser.setSelectedFile(new File("OrderReceipt_" + orderId + ".pdf"));
                
                // Show save dialog
                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File destFile = fileChooser.getSelectedFile();
                    
                    // Find most recent receipt or generate new one
                    File receiptDir = new File("OrderReceipts");
                    final String receiptPrefix = "OrderReceipt_" + orderId + "_";
                    File[] files = receiptDir.listFiles((dir, name) -> name.startsWith(receiptPrefix) && name.endsWith(".pdf"));
                    
                    if (files != null && files.length > 0) {
                        // Sort by last modified (newest first)
                        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                        
                        // Copy file to destination
                        java.nio.file.Files.copy(files[0].toPath(), destFile.toPath(), 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        JOptionPane.showMessageDialog(this,
                            "Receipt saved successfully to:\n" + destFile.getAbsolutePath(),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Generate new receipt
                        String username = "Customer";
                        String city = "", state = "", pincode = "";
                        if (address != null) {
                            city = address.getCity();
                            state = address.getState();
                            pincode = address.getPincode();
                        }
                        
                        // Generate receipt
                        String receiptPath = ReceiptGenerator.generateReceipt(order, orderItems, 
                            transaction, username, city, state, pincode);
                        
                        if (receiptPath != null) {
                            File receiptFile = new File(receiptPath);
                            if (receiptFile.exists()) {
                                java.nio.file.Files.copy(receiptFile.toPath(), destFile.toPath(), 
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                
                                JOptionPane.showMessageDialog(this,
                                    "Receipt saved successfully to:\n" + destFile.getAbsolutePath(),
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(this,
                                    "Receipt was generated but file not found at: " + receiptPath,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                "Failed to generate receipt.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error saving receipt: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(viewReceiptButton);
        buttonPanel.add(downloadReceiptButton);
        buttonPanel.add(closeButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
} 