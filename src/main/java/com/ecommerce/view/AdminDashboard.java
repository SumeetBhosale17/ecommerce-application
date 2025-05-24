package com.ecommerce.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.AbstractCellEditor;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.dao.AddressDAOImpl;
import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.dao.CategoryDAOImpl;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.OrderDAOImpl;
import com.ecommerce.dao.OrderItemDAO;
import com.ecommerce.dao.OrderItemDAOImpl;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ProductDAOImpl;
import com.ecommerce.dao.SaleDAO;
import com.ecommerce.dao.SaleDAOImpl;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.UserDAOImpl;
import com.ecommerce.model.Address;
import com.ecommerce.model.Category;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.Sale;
import com.ecommerce.model.User;
import com.ecommerce.model.UserRole;
import com.ecommerce.service.NotificationService;
import com.ecommerce.util.FontManager;
import com.ecommerce.util.ThemeManager;

public class AdminDashboard extends JFrame {
    private User adminUser;
    private JTabbedPane tabbedPane;
    
    // DAOs
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private UserDAO userDAO;
    private OrderDAO orderDAO;
    private SaleDAO saleDAO;
    private OrderItemDAO orderItemDAO;
    private AddressDAO addressDAO;
    
    // Panels
    private JPanel productsPanel;
    private JPanel categoriesPanel;
    private JPanel ordersPanel;
    private JPanel usersPanel;
    private JPanel salesPanel;
    private JPanel reportsPanel;
    private JPanel userManagementPanel;
    private JPanel salesReportPanel;
    private JPanel userProfilePanel;
    private JPanel addressPanel;
    private JPanel addressUpdatePanel;
    
    public AdminDashboard(User adminUser) {
        if (!adminUser.isAdmin()) {
            throw new IllegalArgumentException("Only admin users can access this dashboard.");
        }
        
        this.adminUser = adminUser;
        
        // Initialize DAOs
        productDAO = new ProductDAOImpl();
        categoryDAO = new CategoryDAOImpl();
        userDAO = new UserDAOImpl();
        orderDAO = new OrderDAOImpl();
        saleDAO = new SaleDAOImpl();
        orderItemDAO = new OrderItemDAOImpl();
        addressDAO = new AddressDAOImpl();
        
        // Setup UI
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed pane for different admin functions
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FontManager.getBold(14f));
        tabbedPane.setBackground(ThemeManager.BACKGROUND_COLOR);
        tabbedPane.setForeground(ThemeManager.TEXT_COLOR);
        
        // Create tabs
        productsPanel = createProductsPanel();
        categoriesPanel = createCategoriesPanel();
        ordersPanel = createOrdersPanel();
        usersPanel = createUserManagementPanel();
        salesPanel = createSalesPanel();
        reportsPanel = createSalesReportPanel();
        
        // Add tabs to pane
        tabbedPane.addTab("Products", productsPanel);
        tabbedPane.addTab("Categories", categoriesPanel);
        tabbedPane.addTab("Orders", ordersPanel);
        tabbedPane.addTab("Users", usersPanel);
        tabbedPane.addTab("Sales", salesPanel);
        tabbedPane.addTab("Sales Report", reportsPanel);
        
        // Add tabbed pane to content
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(ThemeManager.PRIMARY_DARK);
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel statusLabel = new JLabel("Logged in as Admin: " + adminUser.getUsername());
        statusLabel.setFont(FontManager.getRegular(14f));
        statusLabel.setForeground(Color.WHITE);
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        add(statusBar, BorderLayout.SOUTH);
        
        // Apply fonts to all components
        SwingUtilities.invokeLater(() -> {
            FontManager.applyFontToAllComponents(this);
        });
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = ThemeManager.createGradientPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Title
        JLabel titleLabel = new JLabel("E-commerce Admin Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // User info and logout panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + adminUser.getUsername());
        welcomeLabel.setFont(FontManager.getBold(20f));
        welcomeLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = ThemeManager.createAdminButton("Logout", ThemeManager.ACCENT_RED);
        logoutButton.setPreferredSize(new Dimension(100, 40));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to logout?", 
                    "Confirm Logout", 
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });
        
        userPanel.add(welcomeLabel);
        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(logoutButton);
        
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Title and action bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(ThemeManager.BACKGROUND_COLOR);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Product Management");
        titleLabel.setFont(FontManager.getBold(24f));
        titleLabel.setForeground(ThemeManager.PRIMARY_DARK);
        actionBar.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(FontManager.getRegular(14f));
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.putClientProperty("JTextField.placeholderText", "Search products...");
        
        JButton searchButton = ThemeManager.createAdminButton("Search", ThemeManager.PRIMARY_MEDIUM);
        searchButton.setIcon(getScaledIcon("/icons/search.png", 16, 16));
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        actionBar.add(searchPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JButton addButton = ThemeManager.createAdminButton("Add Product", ThemeManager.PRIMARY_MEDIUM);
        addButton.setIcon(getScaledIcon("/icons/add.png", 16, 16));
        addButton.setPreferredSize(new Dimension(150, 35));
        
        JButton editButton = ThemeManager.createAdminButton("Edit", ThemeManager.PRIMARY_MEDIUM);
        editButton.setIcon(getScaledIcon("/icons/edit.png", 16, 16));
        editButton.setPreferredSize(new Dimension(100, 35));
        
        JButton deleteButton = ThemeManager.createAdminButton("Delete", ThemeManager.ACCENT_RED);
        deleteButton.setIcon(getScaledIcon("/icons/delete.png", 16, 16));
        deleteButton.setPreferredSize(new Dimension(100, 35));
        
        JButton refreshButton = ThemeManager.createAdminButton("Refresh", ThemeManager.ACCENT_GREEN);
        refreshButton.setIcon(getScaledIcon("/icons/refresh.png", 16, 16));
        refreshButton.setPreferredSize(new Dimension(100, 35));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        actionBar.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(actionBar, BorderLayout.NORTH);
        
        // Product list table with enhanced styling
        String[] columnNames = {"ID", "Name", "Price", "Stock", "Category", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only make Actions column editable
            }
        };
        
        JTable productTable = new JTable(model);
        productTable.setRowHeight(40);
        productTable.setFillsViewportHeight(true);
        productTable.setFont(FontManager.getRegular(14f));
        productTable.setSelectionBackground(ThemeManager.PRIMARY_LIGHT);
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setShowGrid(true);
        productTable.setGridColor(new Color(230, 230, 230));
        productTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style header
        JTableHeader header = productTable.getTableHeader();
        header.setFont(FontManager.getBold(14f));
        header.setBackground(ThemeManager.PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        
        // Set column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        productTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Price
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Stock
        productTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Category
        productTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Actions
        
        // Add custom renderer and editor for Actions column
        productTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        productTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor());
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Wrap table in a card panel
        JPanel tableCard = ThemeManager.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(tableCard, BorderLayout.CENTER);
        
        // Load products
        loadProducts(model);
        
        // Add action listeners
        addButton.addActionListener(e -> showAddProductDialog(model));
        editButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                int productId = (int) productTable.getValueAt(selectedRow, 0);
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    showEditProductDialog(product, model);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to edit.");
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                int productId = (int) productTable.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this product?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (productDAO.deleteProduct(productId)) {
                        loadProducts(model);
                        JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete product.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            }
        });
        
        refreshButton.addActionListener(e -> loadProducts(model));
        
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            if (!searchText.isEmpty()) {
                model.setRowCount(0);
                List<Product> products = productDAO.getAllProduct();
                for (Product product : products) {
                    if (product.getName().toLowerCase().contains(searchText)) {
                        String categoryName = "Unknown";
                        Category category = categoryDAO.getCategoryById(product.getCategory_id());
                        if (category != null) {
                            categoryName = category.getName();
                        }
                        
                        model.addRow(new Object[]{
                            product.getId(),
                            product.getName(),
                            String.format("Rs. %.2f", product.getPrice()),
                            product.getStock(),
                            categoryName,
                            "Actions"
                        });
                    }
                }
            } else {
                loadProducts(model);
            }
        });
        
        return panel;
    }
    
    private void loadProducts(DefaultTableModel model) {
        model.setRowCount(0);
        List<Product> products = productDAO.getAllProduct();
        for (Product product : products) {
            String categoryName = "Unknown";
            Category category = categoryDAO.getCategoryById(product.getCategory_id());
            if (category != null) {
                categoryName = category.getName();
            }
            
            model.addRow(new Object[]{
                product.getId(),
                product.getName(),
                String.format("Rs. %.2f", product.getPrice()),
                product.getStock(),
                categoryName,
                "Actions"
            });
        }
    }
    
    private void showAddProductDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(ThemeManager.CARD_COLOR);
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(ThemeManager.CARD_COLOR);
        JLabel titleLabel = new JLabel("Add New Product");
        titleLabel.setFont(FontManager.getBold(20f));
        titleLabel.setForeground(ThemeManager.PRIMARY_DARK);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Form panel with GridBagLayout for better control
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeManager.CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setFont(FontManager.getRegular(14f));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridy = 1;
        JTextField nameField = new JTextField(20);
        nameField.setFont(FontManager.getRegular(14f));
        nameField.setPreferredSize(new Dimension(nameField.getPreferredSize().width, 35));
        formPanel.add(nameField, gbc);
        
        // Price field
        gbc.gridy = 2;
        JLabel priceLabel = new JLabel("Price (Rs.):");
        priceLabel.setFont(FontManager.getRegular(14f));
        formPanel.add(priceLabel, gbc);
        
        gbc.gridy = 3;
        JTextField priceField = new JTextField(20);
        priceField.setFont(FontManager.getRegular(14f));
        priceField.setPreferredSize(new Dimension(priceField.getPreferredSize().width, 35));
        formPanel.add(priceField, gbc);
        
        // Stock field
        gbc.gridy = 4;
        JLabel stockLabel = new JLabel("Stock Quantity:");
        stockLabel.setFont(FontManager.getRegular(14f));
        formPanel.add(stockLabel, gbc);
        
        gbc.gridy = 5;
        JTextField stockField = new JTextField(20);
        stockField.setFont(FontManager.getRegular(14f));
        stockField.setPreferredSize(new Dimension(stockField.getPreferredSize().width, 35));
        formPanel.add(stockField, gbc);
        
        // Category field
        gbc.gridy = 6;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(FontManager.getRegular(14f));
        formPanel.add(categoryLabel, gbc);
        
        gbc.gridy = 7;
        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.setFont(FontManager.getRegular(14f));
        categoryCombo.setPreferredSize(new Dimension(categoryCombo.getPreferredSize().width, 35));
        
        // Populate category dropdown
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category category : categories) {
            categoryCombo.addItem(category.getName() + " (ID: " + category.getId() + ")");
        }
        formPanel.add(categoryCombo, gbc);
        
        // Description field
        gbc.gridy = 8;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(FontManager.getRegular(14f));
        formPanel.add(descLabel, gbc);
        
        gbc.gridy = 9;
        JTextArea descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(FontManager.getRegular(14f));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(descScrollPane.getPreferredSize().width, 100));
        formPanel.add(descScrollPane, gbc);
        
        // Image selection
        gbc.gridy = 10;
        JLabel imageLabel = new JLabel("Product Image:");
        imageLabel.setFont(FontManager.getRegular(14f));
        formPanel.add(imageLabel, gbc);
        
        gbc.gridy = 11;
        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.setBackground(ThemeManager.CARD_COLOR);
        JButton imageButton = ThemeManager.createAdminButton("Select Image", ThemeManager.PRIMARY_MEDIUM);
        imageButton.setIcon(getScaledIcon("/icons/image.png", 16, 16));
        JLabel selectedImageLabel = new JLabel("No image selected");
        selectedImageLabel.setFont(FontManager.getRegular(14f));
        imagePanel.add(imageButton, BorderLayout.WEST);
        imagePanel.add(selectedImageLabel, BorderLayout.CENTER);
        formPanel.add(imagePanel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ThemeManager.CARD_COLOR);
        
        JButton saveButton = ThemeManager.createAdminButton("Save", ThemeManager.ACCENT_GREEN);
        saveButton.setIcon(getScaledIcon("/icons/save.png", 16, 16));
        JButton cancelButton = ThemeManager.createAdminButton("Cancel", ThemeManager.PRIMARY_MEDIUM);
        cancelButton.setIcon(getScaledIcon("/icons/cancel.png", 16, 16));
        
        String[] selectedImagePath = new String[1];
        
        imageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif"));
            
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                selectedImagePath[0] = fileChooser.getSelectedFile().getAbsolutePath();
                selectedImageLabel.setText("Selected: " + fileChooser.getSelectedFile().getName());
            }
        });
        
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a product name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double price;
                try {
                    price = Double.parseDouble(priceField.getText().trim());
                    if (price <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid price greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int stock;
                try {
                    stock = Integer.parseInt(stockField.getText().trim());
                    if (stock < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid stock quantity (0 or greater).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String description = descriptionArea.getText().trim();
                if (description.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a product description.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String categoryStr = (String) categoryCombo.getSelectedItem();
                int categoryId = Integer.parseInt(categoryStr.substring(categoryStr.lastIndexOf("ID: ") + 4, categoryStr.length() - 1));
                
                Product product = new Product();
                product.setName(name);
                product.setPrice(price);
                product.setStock(stock);
                product.setDescription(description);
                product.setCategory_id(categoryId);
                if (selectedImagePath[0] != null) {
                    product.setImagePath(selectedImagePath[0]);
                }
                
                if (productDAO.addProduct(product)) {
                    JOptionPane.showMessageDialog(dialog, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadProducts(model);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add product.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showEditProductDialog(Product product, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Edit Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        JTextField nameField = new JTextField(product.getName());
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()));
        JTextField stockField = new JTextField(String.valueOf(product.getStock()));
        JTextArea descriptionArea = new JTextArea(product.getDescription());
        JComboBox<String> categoryCombo = new JComboBox<>();
        
        // Populate category dropdown
        List<Category> categories = categoryDAO.getAllCategories();
        int selectedIndex = 0;
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            categoryCombo.addItem(category.getName() + " (ID: " + category.getId() + ")");
            if (category.getId() == product.getCategory_id()) {
                selectedIndex = i;
            }
        }
        categoryCombo.setSelectedIndex(selectedIndex);
        
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryCombo);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descriptionArea));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = ThemeManager.createAdminButton("Save", ThemeManager.ACCENT_GREEN);
        JButton cancelButton = ThemeManager.createAdminButton("Cancel", ThemeManager.PRIMARY_MEDIUM);
        
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                String description = descriptionArea.getText().trim();
                
                String categoryStr = (String) categoryCombo.getSelectedItem();
                int categoryId = Integer.parseInt(categoryStr.substring(categoryStr.lastIndexOf("ID: ") + 4, categoryStr.length() - 1));
                
                product.setName(name);
                product.setPrice(price);
                product.setStock(stock);
                product.setDescription(description);
                product.setCategory_id(categoryId);
                
                if (productDAO.updateProduct(product)) {
                    JOptionPane.showMessageDialog(dialog, "Product updated successfully!");
                    loadProducts(model);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update product.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format. Please check price and stock values.");
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private JPanel createCategoriesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Title and action bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(ThemeManager.BACKGROUND_COLOR);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Category Management");
        titleLabel.setFont(FontManager.getBold(20f));
        titleLabel.setForeground(ThemeManager.PRIMARY_DARK);
        actionBar.add(titleLabel, BorderLayout.WEST);
        
        // Table for categories
        String[] columnNames = {"ID", "Name", "Parent Category"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable categoryTable = new JTable(model);
        categoryTable.setRowHeight(30);
        categoryTable.setFillsViewportHeight(true);
        categoryTable.setFont(FontManager.getRegular(14f));
        categoryTable.setSelectionBackground(ThemeManager.PRIMARY_LIGHT);
        categoryTable.setSelectionForeground(Color.BLACK);
        categoryTable.setShowGrid(true);
        categoryTable.setGridColor(new Color(230, 230, 230));
        
        // Style header
        JTableHeader header = categoryTable.getTableHeader();
        header.setFont(FontManager.getBold(14f));
        header.setBackground(ThemeManager.PRIMARY_DARK);
        header.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JButton addButton = ThemeManager.createAdminButton("Add Category", ThemeManager.PRIMARY_MEDIUM);
        addButton.setIcon(getScaledIcon("/icons/add.png", 16, 16));
        
        JButton editButton = ThemeManager.createAdminButton("Edit", ThemeManager.PRIMARY_MEDIUM);
        editButton.setIcon(getScaledIcon("/icons/edit.png", 16, 16));
        
        JButton deleteButton = ThemeManager.createAdminButton("Delete", ThemeManager.ACCENT_RED);
        deleteButton.setIcon(getScaledIcon("/icons/delete.png", 16, 16));
        
        JButton refreshButton = ThemeManager.createAdminButton("Refresh", ThemeManager.ACCENT_GREEN);
        refreshButton.setIcon(getScaledIcon("/icons/refresh.png", 16, 16));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        actionBar.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(actionBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load categories
        loadCategories(model);
        
        // Add action listeners
        addButton.addActionListener(e -> showAddCategoryDialog(model));
        
        editButton.addActionListener(e -> {
            int selectedRow = categoryTable.getSelectedRow();
            if (selectedRow >= 0) {
                int categoryId = (int) categoryTable.getValueAt(selectedRow, 0);
                Category category = categoryDAO.getCategoryById(categoryId);
                if (category != null) {
                    showEditCategoryDialog(category, model);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a category to edit.");
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = categoryTable.getSelectedRow();
            if (selectedRow >= 0) {
                int categoryId = (int) categoryTable.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to delete this category?", 
                        "Confirm Delete", 
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (categoryDAO.deleteCategory(categoryId)) {
                        model.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(this, "Category deleted successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete category.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a category to delete.");
            }
        });
        
        refreshButton.addActionListener(e -> loadCategories(model));
        
        return panel;
    }
    
    private void loadCategories(DefaultTableModel model) {
        model.setRowCount(0);
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category category : categories) {
            String parentName = "None";
            Integer parentId = category.getParentId();
            if (parentId != null && parentId > 0) {
                Category parent = categoryDAO.getCategoryById(parentId);
                if (parent != null) {
                    parentName = parent.getName();
                }
            }
            model.addRow(new Object[]{
                category.getId(),
                category.getName(),
                parentName
            });
        }
    }
    
    private void showAddCategoryDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Add New Category", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JTextField nameField = new JTextField();
        JComboBox<String> parentCombo = new JComboBox<>();
        parentCombo.addItem("None");
        
        // Populate parent category dropdown
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category category : categories) {
            parentCombo.addItem(category.getName() + " (ID: " + category.getId() + ")");
        }
        
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Parent Category:"));
        formPanel.add(parentCombo);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = ThemeManager.createAdminButton("Save", ThemeManager.ACCENT_GREEN);
        JButton cancelButton = ThemeManager.createAdminButton("Cancel", ThemeManager.PRIMARY_MEDIUM);
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a category name.");
                return;
            }
            
            Category category = new Category();
            category.setName(name);
            
            String parentStr = (String) parentCombo.getSelectedItem();
            if (!parentStr.equals("None")) {
                int parentId = Integer.parseInt(parentStr.substring(parentStr.lastIndexOf("ID: ") + 4, parentStr.length() - 1));
                category.setParentId(parentId);
            }
            
            if (categoryDAO.addCategory(category)) {
                JOptionPane.showMessageDialog(dialog, "Category added successfully!");
                loadCategories(model);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add category.");
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditCategoryDialog(Category category, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Edit Category", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JTextField nameField = new JTextField(category.getName());
        JComboBox<String> parentCombo = new JComboBox<>();
        parentCombo.addItem("None");
        
        // Populate parent category dropdown
        List<Category> categories = categoryDAO.getAllCategories();
        int selectedIndex = 0;
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            if (cat.getId() != category.getId()) { // Don't allow self as parent
                parentCombo.addItem(cat.getName() + " (ID: " + cat.getId() + ")");
                if (category.getParentId() != null && category.getParentId() == cat.getId()) {
                    selectedIndex = i;
                }
            }
        }
        parentCombo.setSelectedIndex(selectedIndex);
        
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Parent Category:"));
        formPanel.add(parentCombo);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = ThemeManager.createAdminButton("Save", ThemeManager.ACCENT_GREEN);
        JButton cancelButton = ThemeManager.createAdminButton("Cancel", ThemeManager.PRIMARY_MEDIUM);
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a category name.");
                return;
            }
            
            category.setName(name);
            
            String parentStr = (String) parentCombo.getSelectedItem();
            if (parentStr.equals("None")) {
                category.setParentId(null);
            } else {
                int parentId = Integer.parseInt(parentStr.substring(parentStr.lastIndexOf("ID: ") + 4, parentStr.length() - 1));
                category.setParentId(parentId);
            }
            
            if (categoryDAO.updateCategory(category)) {
                JOptionPane.showMessageDialog(dialog, "Category updated successfully!");
                loadCategories(model);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update category.");
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Title and action bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(ThemeManager.BACKGROUND_COLOR);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Order Management");
        titleLabel.setFont(FontManager.getBold(20f));
        titleLabel.setForeground(ThemeManager.PRIMARY_DARK);
        actionBar.add(titleLabel, BorderLayout.WEST);
        
        // Table for orders
        String[] columnNames = {"Order ID", "User", "Date", "Total", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable orderTable = new JTable(model);
        orderTable.setRowHeight(30);
        orderTable.setFillsViewportHeight(true);
        orderTable.setFont(FontManager.getRegular(14f));
        orderTable.setSelectionBackground(ThemeManager.PRIMARY_LIGHT);
        orderTable.setSelectionForeground(Color.WHITE);
        orderTable.setShowGrid(true);
        orderTable.setGridColor(new Color(230, 230, 230));
        
        // Style header
        JTableHeader header = orderTable.getTableHeader();
        header.setFont(FontManager.getBold(14f));
        header.setBackground(ThemeManager.PRIMARY_DARK);
        header.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JButton viewButton = ThemeManager.createStyledButton("View Details");
        viewButton.setIcon(getScaledIcon("/icons/view.png", 16, 16));
        
        JButton updateStatusButton = ThemeManager.createStyledButton("Update Status");
        updateStatusButton.setIcon(getScaledIcon("/icons/edit.png", 16, 16));
        
        JButton refreshButton = ThemeManager.createStyledButton("Refresh");
        refreshButton.setIcon(getScaledIcon("/icons/refresh.png", 16, 16));
        refreshButton.setBackground(ThemeManager.ACCENT_GREEN);
        
        buttonPanel.add(viewButton);
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(refreshButton);
        actionBar.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(actionBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load orders
        loadOrders(model);
        
        // Button actions would be implemented here
        
        return panel;
    }
    
    private void loadOrders(DefaultTableModel model) {
        model.setRowCount(0);
        List<Order> orders = orderDAO.getAllOrders();
        for (Order order : orders) {
            String username = "Unknown";
            try {
                User user = userDAO.findById(order.getUserId());
                if (user != null) {
                    username = user.getUsername();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error retrieving user for order: " + e.getMessage());
            }
            
            model.addRow(new Object[]{
                order.getId(),
                username,
                order.getOrderDate(),
                String.format("Rs. %.2f", order.getTotalAmount()),
                order.getOrderStatus()
            });
        }
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Title and action bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(ThemeManager.BACKGROUND_COLOR);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(FontManager.getBold(20f));
        titleLabel.setForeground(ThemeManager.PRIMARY_DARK);
        actionBar.add(titleLabel, BorderLayout.WEST);
        
        // Table with users
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Username", "Email", "Role"}, 0);
        JTable userTable = new JTable(model);
        userTable.setRowHeight(30);
        userTable.setFillsViewportHeight(true);
        userTable.setFont(FontManager.getRegular(14f));
        userTable.setSelectionBackground(ThemeManager.PRIMARY_LIGHT);
        userTable.setSelectionForeground(Color.WHITE);
        userTable.setShowGrid(true);
        userTable.setGridColor(new Color(230, 230, 230));
        
        // Style header
        JTableHeader header = userTable.getTableHeader();
        header.setFont(FontManager.getBold(14f));
        header.setBackground(ThemeManager.PRIMARY_DARK);
        header.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JButton editButton = ThemeManager.createStyledButton("Edit User");
        editButton.setIcon(getScaledIcon("/icons/edit.png", 16, 16));
        editButton.setForeground(Color.WHITE);
        
        JButton deleteButton = ThemeManager.createStyledButton("Delete User");
        deleteButton.setIcon(getScaledIcon("/icons/delete.png", 16, 16));
        deleteButton.setBackground(ThemeManager.ACCENT_RED);
        deleteButton.setForeground(Color.WHITE);
        
        JButton refreshButton = ThemeManager.createStyledButton("Refresh");
        refreshButton.setIcon(getScaledIcon("/icons/refresh.png", 16, 16));
        refreshButton.setBackground(ThemeManager.ACCENT_GREEN);
        refreshButton.setForeground(Color.WHITE);
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        actionBar.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(actionBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        loadUsers(model);
        
        // Add action listeners (retain existing functionality)
        deleteButton.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                int userId = (int) model.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (userDAO.deleteUser(userId)) {
                        model.removeRow(row);
                    }
                }
            }
        });
        
        editButton.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                int userId = (int) model.getValueAt(row, 0);
                String username = (String) model.getValueAt(row, 1);
                String email = (String) model.getValueAt(row, 2);
                String role = (String) model.getValueAt(row, 3);
                
                JPanel editPanel = new JPanel(new GridLayout(3, 2, 10, 10));
                editPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                JTextField usernameField = new JTextField(username);
                JTextField emailField = new JTextField(email);
                JComboBox<String> roleCombo = new JComboBox<>(new String[]{"USER", "ADMIN"});
                roleCombo.setSelectedItem(role.toUpperCase());
                
                editPanel.add(new JLabel("Username:"));
                editPanel.add(usernameField);
                editPanel.add(new JLabel("Email:"));
                editPanel.add(emailField);
                editPanel.add(new JLabel("Role:"));
                editPanel.add(roleCombo);
                
                // Customized option pane with proper styling
                JOptionPane optionPane = new JOptionPane(
                    editPanel,
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION
                );
                
                JDialog dialog = optionPane.createDialog(this, "Edit User");
                dialog.setVisible(true);
                
                if (optionPane.getValue() != null && (Integer)optionPane.getValue() == JOptionPane.OK_OPTION) {
                    try {
                        User user = new User();
                        user.setId(userId);
                        user.setUsername(usernameField.getText());
                        user.setEmail(emailField.getText());
                        user.setRole(UserRole.valueOf((String) roleCombo.getSelectedItem()));
                        if (userDAO.updateUser(user)) {
                            model.setValueAt(user.getUsername(), row, 1);
                            model.setValueAt(user.getEmail(), row, 2);
                            model.setValueAt(user.getRole().toString(), row, 3);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error updating user: " + ex.getMessage());
                    }
                }
            }
        });
        
        refreshButton.addActionListener(e -> loadUsers(model));
        
        return panel;
    }

    private void loadUsers(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<User> users = userDAO.getAllUsers();
            for (User user : users) {
                model.addRow(new Object[]{user.getId(), user.getUsername(), user.getEmail(), user.getRole()});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage());
        }
    }

    private JPanel createSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Title and action bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(ThemeManager.BACKGROUND_COLOR);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Sales Management");
        titleLabel.setFont(FontManager.getBold(20f));
        titleLabel.setForeground(ThemeManager.PRIMARY_DARK);
        actionBar.add(titleLabel, BorderLayout.WEST);
        
        // Table for sales
        String[] columnNames = {"ID", "Name", "Discount %", "Start Date", "End Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable saleTable = new JTable(model);
        saleTable.setRowHeight(30);
        saleTable.setFillsViewportHeight(true);
        saleTable.setFont(FontManager.getRegular(14f));
        saleTable.setSelectionBackground(ThemeManager.PRIMARY_LIGHT);
        saleTable.setSelectionForeground(Color.WHITE);
        saleTable.setShowGrid(true);
        saleTable.setGridColor(new Color(230, 230, 230));
        
        // Style header
        JTableHeader header = saleTable.getTableHeader();
        header.setFont(FontManager.getBold(14f));
        header.setBackground(ThemeManager.PRIMARY_DARK);
        header.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(saleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JButton addButton = ThemeManager.createAdminButton("Create Sale", ThemeManager.PRIMARY_MEDIUM);
        addButton.setIcon(getScaledIcon("/icons/add.png", 16, 16));
        
        JButton endButton = ThemeManager.createAdminButton("End Sale", ThemeManager.ACCENT_RED);
        endButton.setIcon(getScaledIcon("/icons/delete.png", 16, 16));

        JButton refreshButton = ThemeManager.createAdminButton("Refresh", ThemeManager.ACCENT_GREEN);
        refreshButton.setIcon(getScaledIcon("/icons/refresh.png", 16, 16));
        refreshButton.setForeground(Color.WHITE);
        
        buttonPanel.add(addButton);
        buttonPanel.add(endButton);
        buttonPanel.add(refreshButton);
        actionBar.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(actionBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load sales
        loadSales(model);
        
        // Preserve existing action listeners for this panel
        addButton.addActionListener(e -> showAddSaleDialog(model));
        
        endButton.addActionListener(e -> {
            int selectedRow = saleTable.getSelectedRow();
            if (selectedRow >= 0) {
                int saleId = (int) saleTable.getValueAt(selectedRow, 0);
                Sale sale = saleDAO.getSaleById(saleId);
                if (sale != null && sale.isActive()) {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                            "Are you sure you want to end this sale now?", 
                            "Confirm End Sale", 
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        sale.setActive(false);
                        if (saleDAO.updateSale(sale)) {
                            JOptionPane.showMessageDialog(this, "Sale ended successfully.");
                            loadSales(model);
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to end sale.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "This sale is already inactive.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a sale to end.");
            }
        });
        
        refreshButton.addActionListener(e -> loadSales(model));
        
        return panel;
    }
    
    private void loadSales(DefaultTableModel model) {
        model.setRowCount(0);
        List<Sale> sales = saleDAO.getAllSale();
        for (Sale sale : sales) {
            model.addRow(new Object[]{
                sale.getId(),
                sale.getName(),
                sale.getDiscountPercent() + "%",
                sale.getStartDate(),
                sale.getEndDate(),
                sale.isActive() ? "Active" : "Inactive"
            });
        }
    }
    
    private void showAddSaleDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Create New Sale", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        JTextField nameField = new JTextField();
        JTextField discountField = new JTextField();
        JTextField startDateField = new JTextField(LocalDate.now().toString());
        JTextField endDateField = new JTextField(LocalDate.now().plusDays(7).toString());
        JCheckBox activeCheckbox = new JCheckBox();
        activeCheckbox.setSelected(true);
        
        formPanel.add(new JLabel("Sale Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Discount % (0-100):"));
        formPanel.add(discountField);
        formPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        formPanel.add(startDateField);
        formPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        formPanel.add(endDateField);
        formPanel.add(new JLabel("Active:"));
        formPanel.add(activeCheckbox);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = ThemeManager.createAdminButton("Save", ThemeManager.ACCENT_GREEN);
        JButton cancelButton = ThemeManager.createAdminButton("Cancel", ThemeManager.PRIMARY_MEDIUM);
        
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double discount = Double.parseDouble(discountField.getText().trim());
                LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
                LocalDate endDate = LocalDate.parse(endDateField.getText().trim());
                boolean isActive = activeCheckbox.isSelected();
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a sale name.");
                    return;
                }
                
                if (discount < 0 || discount > 100) {
                    JOptionPane.showMessageDialog(dialog, "Discount must be between 0 and 100.");
                    return;
                }
                
                if (endDate.isBefore(startDate)) {
                    JOptionPane.showMessageDialog(dialog, "End date must be after start date.");
                    return;
                }
                
                Sale sale = new Sale();
                sale.setName(name);
                sale.setDiscountPercent(discount);
                sale.setStartDateFromLocalDate(startDate);
                sale.setEndDateFromLocalDate(endDate);
                sale.setActive(isActive);
                
                if (saleDAO.addSale(sale)) {
                    JOptionPane.showMessageDialog(dialog, "Sale created successfully!");
                    
                    // Send notification to all users
                    if (isActive && startDate.equals(LocalDate.now())) {
                        NotificationService.sendSaleNotificationToAllUsers(name, true);
                    } else if (startDate.equals(LocalDate.now().plusDays(1))) {
                        NotificationService.sendSaleNotificationToAllUsers(name, false);
                    }
                    
                    loadSales(model);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create sale.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid discount percentage format.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private JPanel createSalesReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Title and action bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(ThemeManager.BACKGROUND_COLOR);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Sales Report");
        titleLabel.setFont(FontManager.getBold(20f));
        titleLabel.setForeground(ThemeManager.PRIMARY_DARK);
        actionBar.add(titleLabel, BorderLayout.WEST);
        
        // Button panel for export
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JButton exportButton = ThemeManager.createStyledButton("Export to PDF", ThemeManager.ACCENT_ORANGE);
        exportButton.setIcon(getScaledIcon("/icons/pdf.png", 16, 16));
        exportButton.setForeground(Color.WHITE);
        
        JButton refreshButton = ThemeManager.createStyledButton("Refresh", ThemeManager.ACCENT_GREEN);
        refreshButton.setIcon(getScaledIcon("/icons/refresh.png", 16, 16));
        refreshButton.setForeground(Color.WHITE);
        
        buttonPanel.add(exportButton);
        buttonPanel.add(refreshButton);
        actionBar.add(buttonPanel, BorderLayout.EAST);
        
        // Report table
        DefaultTableModel model = new DefaultTableModel(new String[]{"Product", "Category", "Quantity Sold", "Total Sales"}, 0);
        JTable salesTable = new JTable(model);
        salesTable.setRowHeight(30);
        salesTable.setFillsViewportHeight(true);
        salesTable.setFont(FontManager.getRegular(14f));
        salesTable.setSelectionBackground(ThemeManager.PRIMARY_LIGHT);
        salesTable.setSelectionForeground(Color.WHITE);
        salesTable.setShowGrid(true);
        salesTable.setGridColor(new Color(230, 230, 230));
        
        // Style header
        JTableHeader header = salesTable.getTableHeader();
        header.setFont(FontManager.getBold(14f));
        header.setBackground(ThemeManager.PRIMARY_DARK);
        header.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        panel.add(actionBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load sales report data
        loadSalesReport(model);
        
        // Add export functionality
        exportButton.addActionListener(e -> exportSalesReportToPDF(salesTable));
        
        refreshButton.addActionListener(e -> loadSalesReport(model));
        
        return panel;
    }
    
    // New method to export sales report to PDF
    private void exportSalesReportToPDF(JTable table) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF Report");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents", "pdf"));
            
            // Default filename with current date
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String defaultFileName = "SalesReport_" + dateFormat.format(new java.util.Date()) + ".pdf";
            fileChooser.setSelectedFile(new java.io.File(defaultFileName));
            
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String fileName = fileChooser.getSelectedFile().getAbsolutePath();
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    fileName += ".pdf";
                }
                
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fileName));
                document.open();
                
                // Add title
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Sales Report", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                
                // Add date
                com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);
                com.itextpdf.text.Paragraph datePara = new com.itextpdf.text.Paragraph("Generated on: " + dateFormat.format(new java.util.Date()), dateFont);
                datePara.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(datePara);
                document.add(new com.itextpdf.text.Paragraph(" ")); // Blank line
                
                // Create table
                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(table.getColumnCount());
                pdfTable.setWidthPercentage(100);
                
                // Add header row
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(table.getColumnName(i), headerFont));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(25, 25, 112)); // Dark blue
                    cell.setPadding(5);
                    cell.setBorderColor(new com.itextpdf.text.BaseColor(200, 200, 200));
                    pdfTable.addCell(cell);
                }
                
                // Add data rows
                com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        String value = table.getValueAt(i, j) != null ? table.getValueAt(i, j).toString() : "";
                        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(value, cellFont));
                        cell.setHorizontalAlignment(j == 0 || j == 1 ? com.itextpdf.text.Element.ALIGN_LEFT : com.itextpdf.text.Element.ALIGN_RIGHT);
                        cell.setPadding(5);
                        pdfTable.addCell(cell);
                    }
                }
                
                document.add(pdfTable);
                document.close();
                
                // Show success message
                JOptionPane.showMessageDialog(this, "Sales report exported successfully to:\n" + fileName, "Export Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Open the file
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File(fileName));
                } catch (Exception ex) {
                    // Silent exception - opening the file is optional
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exporting report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadSalesReport(DefaultTableModel model) {
        model.setRowCount(0);
        List<OrderItem> orderItems = orderItemDAO.getAllOrderItems();
        Map<String, Integer> productSales = new HashMap<>();
        Map<String, Double> productRevenue = new HashMap<>();
        Map<String, String> productCategories = new HashMap<>(); // Store product to category mapping
        Map<Integer, List<OrderItem>> orderItemsByOrder = new HashMap<>();
        Map<Integer, Order> orderMap = new HashMap<>();
        
        // Group items by order
        for (OrderItem item : orderItems) {
            orderItemsByOrder.computeIfAbsent(item.getOrderId(), k -> new java.util.ArrayList<>()).add(item);
        }
        
        // Get all orders for mapping
        List<Order> allOrders = orderDAO.getAllOrders();
        for (Order order : allOrders) {
            orderMap.put(order.getId(), order);
        }
        
        for (OrderItem item : orderItems) {
            Product product = productDAO.getProductById(item.getProductId());
            if (product != null) {
                String productName = product.getName();
                
                // Get category name
                String categoryName = "Unknown";
                if (product.getCategoryId() > 0) {
                    Category category = categoryDAO.getCategoryById(product.getCategoryId());
                    if (category != null) {
                        categoryName = category.getName();
                        System.out.println("Product: " + productName + ", Category ID: " + product.getCategoryId() + ", Category Name: " + categoryName);
                    } else {
                        System.out.println("Category not found for ID: " + product.getCategoryId());
                    }
                } else {
                    System.out.println("Product has no category ID: " + productName);
                }
                
                // Store category for this product
                productCategories.put(productName, categoryName);
                
                // Discounted revenue calculation
                Order order = orderMap.get(item.getOrderId());
                List<OrderItem> itemsInOrder = orderItemsByOrder.get(item.getOrderId());
                double orderOriginalTotal = 0;
                if (itemsInOrder != null) {
                    for (OrderItem oi : itemsInOrder) {
                        orderOriginalTotal += oi.getPrice() * oi.getQuantity();
                    }
                }
                
                double itemOriginal = item.getPrice() * item.getQuantity();
                double discountedRevenue = itemOriginal;
                if (order != null && orderOriginalTotal > 0) {
                    discountedRevenue = (itemOriginal / orderOriginalTotal) * order.getTotalAmount();
                }
                
                productSales.put(productName, productSales.getOrDefault(productName, 0) + item.getQuantity());
                productRevenue.put(productName, productRevenue.getOrDefault(productName, 0.0) + discountedRevenue);
            }
        }
        
        for (String productName : productSales.keySet()) {
            String categoryName = productCategories.getOrDefault(productName, "Unknown");
            model.addRow(new Object[]{
                productName, 
                categoryName, 
                productSales.get(productName), 
                String.format("Rs. %.2f", productRevenue.get(productName))
            });
        }
    }

    private JPanel createUserProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(4, 2));
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();
        JButton updateButton = new JButton("Update Profile");
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(updateButton, BorderLayout.SOUTH);
        updateButton.addActionListener(e -> {
            try {
                User user = new User();
                user.setId(adminUser.getId());
                user.setUsername(usernameField.getText());
                user.setEmail(emailField.getText());
                user.setAddress(addressField.getText());
                if (userDAO.updateUser(user)) {
                    JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage());
            }
        });
        return panel;
    }

    private JPanel createAddressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(3, 2));
        JTextField streetField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField zipField = new JTextField();
        JButton addButton = new JButton("Add Address");
        formPanel.add(new JLabel("Street:"));
        formPanel.add(streetField);
        formPanel.add(new JLabel("City:"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("ZIP:"));
        formPanel.add(zipField);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.SOUTH);
        addButton.addActionListener(e -> {
            Address address = new Address();
            address.setUserId(adminUser.getId());
            address.setStreet(streetField.getText());
            address.setCity(cityField.getText());
            address.setZip(zipField.getText());
            if (addressDAO.addAddress(address)) {
                JOptionPane.showMessageDialog(this, "Address added successfully!");
            }
        });
        return panel;
    }

    private JPanel createAddressUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(3, 2));
        JTextField streetField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField zipField = new JTextField();
        JButton updateButton = new JButton("Update Address");
        formPanel.add(new JLabel("Street:"));
        formPanel.add(streetField);
        formPanel.add(new JLabel("City:"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("ZIP:"));
        formPanel.add(zipField);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(updateButton, BorderLayout.SOUTH);
        updateButton.addActionListener(e -> {
            Address address = new Address();
            address.setUserId(adminUser.getId());
            address.setStreet(streetField.getText());
            address.setCity(cityField.getText());
            address.setZip(zipField.getText());
            if (addressDAO.updateAddress(address)) {
                JOptionPane.showMessageDialog(this, "Address updated successfully!");
            }
        });
        return panel;
    }

    // Helper method to get scaled icons
    private ImageIcon getScaledIcon(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                // If icon not found, return a colored icon as fallback
                return createColorIcon(width, height, new Color(150, 150, 150));
            }
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            // Return colored rectangle if icon loading fails
            return createColorIcon(width, height, new Color(150, 150, 150));
        }
    }
    
    // Helper method to create a colored icon
    private ImageIcon createColorIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return new ImageIcon(image);
    }

    // Custom renderer for buttons in table
    private class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton viewButton;
        private JButton editButton;
        private JButton deleteButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setBackground(Color.WHITE);

            viewButton = new JButton("👁");
            viewButton.setFont(FontManager.getRegular(14f));
            viewButton.setFocusPainted(false);
            viewButton.setBorderPainted(false);
            viewButton.setContentAreaFilled(false);
            viewButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            editButton = new JButton("✏️");
            editButton.setFont(FontManager.getRegular(14f));
            editButton.setFocusPainted(false);
            editButton.setBorderPainted(false);
            editButton.setContentAreaFilled(false);
            editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            deleteButton = new JButton("🗑️");
            deleteButton.setFont(FontManager.getRegular(14f));
            deleteButton.setFocusPainted(false);
            deleteButton.setBorderPainted(false);
            deleteButton.setContentAreaFilled(false);
            deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            add(viewButton);
            add(editButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Custom editor for buttons in table
    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton viewButton;
        private JButton editButton;
        private JButton deleteButton;
        private int row;

        public ButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setBackground(Color.WHITE);

            viewButton = new JButton("👁");
            viewButton.setFont(FontManager.getRegular(14f));
            viewButton.setFocusPainted(false);
            viewButton.setBorderPainted(false);
            viewButton.setContentAreaFilled(false);
            viewButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            editButton = new JButton("✏️");
            editButton.setFont(FontManager.getRegular(14f));
            editButton.setFocusPainted(false);
            editButton.setBorderPainted(false);
            editButton.setContentAreaFilled(false);
            editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            deleteButton = new JButton("🗑️");
            deleteButton.setFont(FontManager.getRegular(14f));
            deleteButton.setFocusPainted(false);
            deleteButton.setBorderPainted(false);
            deleteButton.setContentAreaFilled(false);
            deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            panel.add(viewButton);
            panel.add(editButton);
            panel.add(deleteButton);

            viewButton.addActionListener(e -> {
                fireEditingStopped();
                JTable table = (JTable) panel.getParent().getParent();
                int productId = (int) table.getValueAt(row, 0);
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    showProductDetails(product);
                }
            });

            editButton.addActionListener(e -> {
                fireEditingStopped();
                JTable table = (JTable) panel.getParent().getParent();
                int productId = (int) table.getValueAt(row, 0);
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    showEditProductDialog(product, (DefaultTableModel) table.getModel());
                }
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                JTable table = (JTable) panel.getParent().getParent();
                int productId = (int) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(AdminDashboard.this,
                    "Are you sure you want to delete this product?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (productDAO.deleteProduct(productId)) {
                        loadProducts((DefaultTableModel) table.getModel());
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Product deleted successfully!");
                    } else {
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Failed to delete product.");
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }

    private void showProductDetails(Product product) {
        JDialog dialog = new JDialog(this, "Product Details", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Product image
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            ImageIcon imageIcon = new ImageIcon(product.getImagePath());
            Image image = imageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridheight = 5;
            detailsPanel.add(imageLabel, gbc);
        }

        // Product details
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;

        JLabel nameLabel = new JLabel("Name: " + product.getName());
        nameLabel.setFont(FontManager.getBold(16f));
        detailsPanel.add(nameLabel, gbc);

        gbc.gridy = 1;
        JLabel priceLabel = new JLabel("Price: Rs. " + String.format("%.2f", product.getPrice()));
        priceLabel.setFont(FontManager.getRegular(14f));
        detailsPanel.add(priceLabel, gbc);

        gbc.gridy = 2;
        JLabel stockLabel = new JLabel("Stock: " + product.getStock());
        stockLabel.setFont(FontManager.getRegular(14f));
        detailsPanel.add(stockLabel, gbc);

        gbc.gridy = 3;
        String categoryName = "Unknown";
        Category category = categoryDAO.getCategoryById(product.getCategory_id());
        if (category != null) {
            categoryName = category.getName();
        }
        JLabel categoryLabel = new JLabel("Category: " + categoryName);
        categoryLabel.setFont(FontManager.getRegular(14f));
        detailsPanel.add(categoryLabel, gbc);

        gbc.gridy = 4;
        JLabel descLabel = new JLabel("Description: " + product.getDescription());
        descLabel.setFont(FontManager.getRegular(14f));
        detailsPanel.add(descLabel, gbc);

        JButton closeButton = ThemeManager.createAdminButton("Close", ThemeManager.PRIMARY_MEDIUM);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);

        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}