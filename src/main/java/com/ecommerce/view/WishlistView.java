package com.ecommerce.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.ecommerce.dao.CartItemDAO;
import com.ecommerce.dao.CartItemDAOImpl;
import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.dao.WishlistDAOImpl;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.util.ThemeManager;

public class WishlistView extends JPanel {
    private final User currentUser;
    private final WishlistDAO wishlistDAO;
    private final CartItemDAO cartItemDAO;
    
    private JTable wishlistTable;
    private DefaultTableModel wishlistTableModel;
    private JLabel titleLabel;
    private JLabel emptyWishlistLabel;
    
    private final String[] columnNames = {"Product", "Price", "Stock", "Actions"};
    
    public WishlistView(User currentUser) {
        this.currentUser = currentUser;
        this.wishlistDAO = new WishlistDAOImpl();
        this.cartItemDAO = new CartItemDAOImpl();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("My Wishlist");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Continue shopping button
        JButton continueShoppingButton = ThemeManager.createRoundedButton("Continue Shopping", ThemeManager.PRIMARY_MEDIUM);
        continueShoppingButton.addActionListener(e -> {
            Container parent = getParent();
            if (parent != null) {
                try {
                    java.lang.reflect.Method method = parent.getClass().getMethod("showProductsPanel");
                    method.invoke(parent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(continueShoppingButton);
        titlePanel.add(buttonPanel, BorderLayout.EAST);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Table setup
        wishlistTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only action column is editable
            }
        };
        
        wishlistTable = new JTable(wishlistTableModel);
        wishlistTable.setRowHeight(50);
        wishlistTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        wishlistTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        wishlistTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        wishlistTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        
        // Action buttons in the last column
        wishlistTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        wishlistTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), this));
        
        // Table container
        JScrollPane scrollPane = new JScrollPane(wishlistTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Empty wishlist message
        emptyWishlistLabel = new JLabel("Your wishlist is empty. Browse products to add items to your wishlist.");
        emptyWishlistLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyWishlistLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Content panel that will show either the table or empty message
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(emptyWishlistLabel, BorderLayout.NORTH);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Load wishlist items
        loadWishlistItems();
    }
    
    public void loadWishlistItems() {
        // Clear existing data
        wishlistTableModel.setRowCount(0);
        
        // Get products from wishlist
        List<Product> products = wishlistDAO.getWishlistProductsByUserId(currentUser.getId());
        
        // Show or hide empty wishlist message
        if (products.isEmpty()) {
            emptyWishlistLabel.setVisible(true);
            wishlistTable.setVisible(false);
        } else {
            emptyWishlistLabel.setVisible(false);
            wishlistTable.setVisible(true);
            
            // Add products to table
            for (Product product : products) {
                Object[] row = {
                    product.getName(),
                    "Rs. " + String.format("%.2f", product.getPrice()),
                    product.getStock() > 0 ? product.getStock() + " in stock" : "Out of stock",
                    product.getId() // This will be replaced by buttons in the renderer
                };
                wishlistTableModel.addRow(row);
            }
        }
        
        titleLabel.setText("My Wishlist (" + products.size() + " items)");
    }
    
    public void removeFromWishlist(int productId) {
        boolean success = wishlistDAO.removeFromWishlist(currentUser.getId(), productId);
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Item removed from your wishlist", 
                    "Wishlist Updated", 
                    JOptionPane.INFORMATION_MESSAGE);
            loadWishlistItems();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to remove item from wishlist", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void moveToCart(int productId) {
        // Create a new cart item
        CartItem cartItem = new CartItem(currentUser.getId(), productId, 1);
        boolean addedToCart = cartItemDAO.addCartItem(cartItem);
        
        if (addedToCart) {
            // Remove from wishlist
            boolean removedFromWishlist = wishlistDAO.removeFromWishlist(currentUser.getId(), productId);
            
            if (removedFromWishlist) {
                JOptionPane.showMessageDialog(this, 
                        "Item moved to cart successfully!", 
                        "Item Moved", 
                        JOptionPane.INFORMATION_MESSAGE);
                loadWishlistItems();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Item added to cart but could not be removed from wishlist", 
                        "Partial Success", 
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to add item to cart", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Custom Button Renderer for action column
    static class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton addToCartButton;
        private JButton removeButton;
        
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(true);
            
            addToCartButton = ThemeManager.createRoundedButton("Add to Cart", ThemeManager.ACCENT_GREEN);
            addToCartButton.setPreferredSize(new Dimension(100, 30));
            
            removeButton = ThemeManager.createRoundedButton("Remove", ThemeManager.ACCENT_RED);
            removeButton.setPreferredSize(new Dimension(80, 30));
            
            add(addToCartButton);
            add(removeButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }
    
    // Custom Button Editor for action column
    static class ButtonEditor extends DefaultCellEditor {
        private JButton addToCartButton;
        private JButton removeButton;
        private JPanel panel;
        private int productId;
        private WishlistView wishlistView;
        
        public ButtonEditor(JCheckBox checkBox, WishlistView wishlistView) {
            super(checkBox);
            this.wishlistView = wishlistView;
            
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            
            addToCartButton = ThemeManager.createRoundedButton("Add to Cart", ThemeManager.ACCENT_GREEN);
            addToCartButton.setPreferredSize(new Dimension(100, 30));
            addToCartButton.addActionListener(e -> {
                fireEditingStopped();
                wishlistView.moveToCart(productId);
            });
            
            removeButton = ThemeManager.createRoundedButton("Remove", ThemeManager.ACCENT_RED);
            removeButton.setPreferredSize(new Dimension(80, 30));
            removeButton.addActionListener(e -> {
                fireEditingStopped();
                wishlistView.removeFromWishlist(productId);
            });
            
            panel.add(addToCartButton);
            panel.add(removeButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            productId = (int) value;
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return productId;
        }
    }
} 