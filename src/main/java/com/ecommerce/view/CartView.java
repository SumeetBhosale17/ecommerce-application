package com.ecommerce.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.ecommerce.dao.CartItemDAO;
import com.ecommerce.dao.CartItemDAOImpl;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ProductDAOImpl;
import com.ecommerce.dao.SaleDAO;
import com.ecommerce.dao.SaleDAOImpl;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.Sale;
import com.ecommerce.model.User;
import com.ecommerce.util.ThemeManager;

public class CartView extends JPanel {
    private final User currentUser;
    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;
    private final SaleDAO saleDAO;
    private final DefaultTableModel cartTableModel;
    private final JTable cartTable;
    private final JLabel totalLabel;
    private double totalAmount = 0.0;
    
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    
    public CartView(User currentUser) {
        this.currentUser = currentUser;
        this.cartItemDAO = new CartItemDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.saleDAO = new SaleDAOImpl();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Your Shopping Cart");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
        
        // Cart Table
        String[] columnNames = {"Product", "Price", "Quantity", "Discount", "Subtotal", "Actions"};
        cartTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only make the quantity column editable
                return column == 2;
            }
        };
        
        cartTable = new JTable(cartTableModel);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Product name
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Price
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Quantity
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Discount
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Subtotal
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Actions
        
        // Set up the action button renderer and editor for the Actions column
        cartTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        cartTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), this));
        
        JScrollPane scrollPane = new JScrollPane(cartTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with total and checkout button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Total amount panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total: Rs. 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalPanel.add(totalLabel);
        
        // Checkout panel
        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearCartButton = ThemeManager.createRoundedButton("Clear Cart", ThemeManager.ACCENT_RED);
        JButton checkoutButton = ThemeManager.createRoundedButton("Proceed to Checkout", ThemeManager.ACCENT_GREEN);
        
        checkoutPanel.add(clearCartButton);
        checkoutPanel.add(Box.createHorizontalStrut(10));
        checkoutPanel.add(checkoutButton);
        
        bottomPanel.add(totalPanel, BorderLayout.NORTH);
        bottomPanel.add(checkoutPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Action listeners
        clearCartButton.addActionListener(e -> clearCart());
        checkoutButton.addActionListener(e -> proceedToCheckout());
        
        // Load cart items
        loadCartItems();
    }
    
    public void loadCartItems() {
        cartTableModel.setRowCount(0);
        totalAmount = 0.0;
        
        List<CartItem> cartItems = cartItemDAO.getCartItemsByUserId(currentUser.getId());
        if (cartItems.isEmpty()) {
            totalLabel.setText("Total: Rs. 0.00");
            return;
        }
        
        // Get active sale discount if any
        Sale activeSale = saleDAO.getActiveSale();
        double discountPercent = (activeSale != null) ? activeSale.getDiscountPercent() : 0.0;
        
        for (CartItem item : cartItems) {
            Product product = productDAO.getProductById(item.getProductId());
            if (product != null) {
                double price = product.getPrice();
                int quantity = item.getQuantity();
                double discount = price * (discountPercent / 100.0) * quantity;
                double subtotal = (price * quantity) - discount;
                
                totalAmount += subtotal;
                
                cartTableModel.addRow(new Object[]{
                    product.getName(),
                    "Rs. " + String.format("%.2f", price),
                    quantity,
                    "Rs. " + String.format("%.2f", discount),
                    "Rs. " + String.format("%.2f", subtotal),
                    "Remove"
                });
            }
        }
        
        totalLabel.setText("Total: Rs. " + String.format("%.2f", totalAmount));
    }
    
    public void updateCartItemQuantity(int row, int quantity) {
        if (row < 0 || row >= cartTableModel.getRowCount()) return;
        
        String productName = (String) cartTableModel.getValueAt(row, 0);
        List<CartItem> cartItems = cartItemDAO.getCartItemsByUserId(currentUser.getId());
        
        for (int i = 0; i < cartItems.size(); i++) {
            if (i == row) {
                CartItem item = cartItems.get(i);
                Product product = productDAO.getProductById(item.getProductId());
                
                if (product != null) {
                    if (quantity <= 0) {
                        removeCartItem(row);
                        return;
                    }
                    
                    if (quantity > product.getStock()) {
                        JOptionPane.showMessageDialog(this, 
                                "Sorry, only " + product.getStock() + " units available.", 
                                "Insufficient Stock", 
                                JOptionPane.ERROR_MESSAGE);
                        loadCartItems(); // Reload to reset displayed quantity
                        return;
                    }
                    
                    item.setQuantity(quantity);
                    cartItemDAO.updateCartItem(item);
                    
                    // Update the row in the table
                    Sale activeSale = saleDAO.getActiveSale();
                    double discountPercent = (activeSale != null) ? activeSale.getDiscountPercent() : 0.0;
                    double price = product.getPrice();
                    double discount = price * (discountPercent / 100.0) * quantity;
                    double subtotal = (price * quantity) - discount;
                    
                    cartTableModel.setValueAt(quantity, row, 2);
                    cartTableModel.setValueAt("Rs. " + String.format("%.2f", discount), row, 3);
                    cartTableModel.setValueAt("Rs. " + String.format("%.2f", subtotal), row, 4);
                    
                    // Recalculate total
                    calculateTotal();
                    break;
                }
            }
        }
    }
    
    public void removeCartItem(int row) {
        if (row < 0 || row >= cartTableModel.getRowCount()) {
            System.out.println("Invalid row index: " + row);
            return;
        }
        
        try {
            // Get the product name from the table to help with identification
            String productName = (String) cartTableModel.getValueAt(row, 0);
            System.out.println("Attempting to remove item: " + productName + " at row " + row);
            
            // Get current cart items
            List<CartItem> cartItems = cartItemDAO.getCartItemsByUserId(currentUser.getId());
            if (cartItems.isEmpty()) {
                System.out.println("Cart is empty, nothing to remove");
                return;
            }
            
            // Safety check - does this row actually exist?
            if (row >= cartItems.size()) {
                System.out.println("Warning: Table row index exceeds cart items size");
                // Fall back to removing by product name
                for (CartItem item : cartItems) {
                    Product product = productDAO.getProductById(item.getProductId());
                    if (product != null && product.getName().equals(productName)) {
                        if (cartItemDAO.removeCartItem(item.getId())) {
                            cartTableModel.removeRow(row);
                            calculateTotal();
                            JOptionPane.showMessageDialog(this, "Item removed from cart", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to remove item from cart", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        return;
                    }
                }
                JOptionPane.showMessageDialog(this, "Could not find item to remove", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get the cart item at the corresponding index
            CartItem itemToRemove = cartItems.get(row);
            int cartItemId = itemToRemove.getId();
            
            // Delete the specific cart item from the database
            boolean removed = cartItemDAO.removeCartItem(cartItemId);
            if (removed) {
                // Remove from the table model
                cartTableModel.removeRow(row);
                calculateTotal();
                JOptionPane.showMessageDialog(this, "Item removed from cart", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("Database removal failed for cart item ID: " + cartItemId);
                
                // Try a fallback approach - remove by product ID
                Product product = productDAO.getProductById(itemToRemove.getProductId());
                if (product != null) {
                    String pName = product.getName();
                    for (int i = 0; i < cartItems.size(); i++) {
                        CartItem item = cartItems.get(i);
                        Product p = productDAO.getProductById(item.getProductId());
                        if (p != null && p.getName().equals(pName)) {
                            if (cartItemDAO.removeCartItem(item.getId())) {
                                cartTableModel.removeRow(row);
                                calculateTotal();
                                JOptionPane.showMessageDialog(this, "Item removed from cart", "Success", JOptionPane.INFORMATION_MESSAGE);
                                return;
                            }
                        }
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Failed to remove item from cart", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Error removing cart item: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            
            // Reload cart to ensure UI consistency
            loadCartItems();
        }
    }
    
    private void calculateTotal() {
        totalAmount = 0.0;
        
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            String subtotalStr = (String) cartTableModel.getValueAt(i, 4);
            subtotalStr = subtotalStr.replace("Rs. ", "").replace(",", "");
            try {
                totalAmount += Double.parseDouble(subtotalStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        
        totalLabel.setText("Total: Rs. " + String.format("%.2f", totalAmount));
    }
    
    private void clearCart() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to clear your cart?", 
                "Confirm Clear Cart", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            cartItemDAO.clearCartByUserId(currentUser.getId());
            loadCartItems();
            JOptionPane.showMessageDialog(this, "Cart cleared successfully", "Cart Cleared", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void proceedToCheckout() {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                    "Your cart is empty. Please add items before checkout.", 
                    "Empty Cart", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            CheckoutDialog checkoutDialog = new CheckoutDialog(SwingUtilities.getWindowAncestor(this), currentUser, totalAmount);
            checkoutDialog.setVisible(true);
            
            // If order was placed successfully, reload cart
            if (checkoutDialog.isOrderPlaced()) {
                loadCartItems();
                JOptionPane.showMessageDialog(this, 
                        "Your order has been placed successfully!", 
                        "Order Confirmation", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "An error occurred during checkout: " + e.getMessage(), 
                    "Checkout Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Custom Button Renderer for action column
    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setBackground(ThemeManager.ACCENT_RED);
            setForeground(Color.WHITE);
            return this;
        }
    }
    
    // Custom Button Editor for action column
    static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;
        private final CartView cartView;
        private int row;
        
        public ButtonEditor(JCheckBox checkBox, CartView cartView) {
            super(checkBox);
            this.cartView = cartView;
            button = ThemeManager.createRoundedButton("Remove", ThemeManager.ACCENT_RED);
            button.addActionListener(e -> {
                fireEditingStopped();
                if ("Remove".equals(label)) {
                    cartView.removeCartItem(row);
                }
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            this.row = row; // Store the row index when the button is clicked
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
} 