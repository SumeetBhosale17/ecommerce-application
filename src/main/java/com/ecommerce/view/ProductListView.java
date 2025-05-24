package com.ecommerce.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.ecommerce.dao.CartItemDAO;
import com.ecommerce.dao.CartItemDAOImpl;
import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.dao.CategoryDAOImpl;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ProductDAOImpl;
import com.ecommerce.dao.SaleDAO;
import com.ecommerce.dao.SaleDAOImpl;
import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.dao.WishlistDAOImpl;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.Sale;
import com.ecommerce.model.User;
import com.ecommerce.model.Wishlist;
import com.ecommerce.util.FontManager;
import com.ecommerce.util.ThemeManager;

public class ProductListView extends JPanel {
    private final User currentUser;
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final CartItemDAO cartItemDAO;
    private final SaleDAO saleDAO;
    private final WishlistDAO wishlistDAO;
    
    private JPanel productCardsPanel;
    private JComboBox<String> categoryFilterCombo;
    private JComboBox<String> sortByCombo;
    private JTextField searchField;
    private JLabel statusLabel;
    private List<Product> allProducts;
    private List<Category> allCategories;
    private Sale activeSale;
    private JComboBox<String> subcategoryFilterCombo;
    private List<Category> allSubcategories;
    
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    
    public ProductListView(User currentUser) {
        this.currentUser = currentUser;
        this.productDAO = new ProductDAOImpl();
        this.categoryDAO = new CategoryDAOImpl();
        this.cartItemDAO = new CartItemDAOImpl();
        this.saleDAO = new SaleDAOImpl();
        this.wishlistDAO = new WishlistDAOImpl();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // North panel with filters and search
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // Center panel with scrollable product cards
        productCardsPanel = new JPanel();
        productCardsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
        productCardsPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(productCardsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.BACKGROUND_COLOR);
        add(scrollPane, BorderLayout.CENTER);
        
        // Status label
        statusLabel = new JLabel("");
        statusLabel.setFont(FontManager.getRegular(14f));
        statusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);
        
        // Load initial data
        loadAllData();
        displayProducts(allProducts);
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));
        filterPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(FontManager.getBold(16f));
        
        searchField = new JTextField(25);
        searchField.setFont(FontManager.getRegular(16f));
        searchField.setPreferredSize(new Dimension(searchField.getPreferredSize().width, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    filterProducts();
                }
            }
        });
        
        JButton searchButton = ThemeManager.createStyledButton("Search");
        searchButton.setPreferredSize(new Dimension(100, 40));
        searchButton.addActionListener(e -> filterProducts());
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Dropdown panel
        JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        dropdownPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Category filter
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(FontManager.getBold(16f));
        
        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.setFont(FontManager.getRegular(14f));
        categoryFilterCombo.setPreferredSize(new Dimension(180, 35));
        categoryFilterCombo.addActionListener(e -> {
            updateSubcategoryFilter();
            filterProducts();
        });
        
        // Subcategory filter
        JLabel subcategoryLabel = new JLabel("Subcategory:");
        subcategoryLabel.setFont(FontManager.getBold(16f));
        
        subcategoryFilterCombo = new JComboBox<>();
        subcategoryFilterCombo.setFont(FontManager.getRegular(14f));
        subcategoryFilterCombo.setPreferredSize(new Dimension(180, 35));
        subcategoryFilterCombo.addActionListener(e -> filterProducts());
        
        // Sort options
        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setFont(FontManager.getBold(16f));
        
        sortByCombo = new JComboBox<>(new String[]{
            "Sort By", "Price: Low to High", "Price: High to Low", 
            "Name: A to Z", "Name: Z to A"
        });
        sortByCombo.setFont(FontManager.getRegular(14f));
        sortByCombo.setPreferredSize(new Dimension(180, 35));
        sortByCombo.addActionListener(e -> filterProducts());
        
        // Reset button
        JButton resetButton = ThemeManager.createStyledButton("Reset Filters");
        resetButton.setBackground(ThemeManager.ACCENT_RED);
        resetButton.setPreferredSize(new Dimension(130, 35));
        resetButton.addActionListener(e -> {
            searchField.setText("");
            categoryFilterCombo.setSelectedIndex(0);
            sortByCombo.setSelectedIndex(0);
            filterProducts();
        });
        
        dropdownPanel.add(categoryLabel);
        dropdownPanel.add(categoryFilterCombo);
        dropdownPanel.add(subcategoryLabel);
        dropdownPanel.add(subcategoryFilterCombo);
        dropdownPanel.add(sortLabel);
        dropdownPanel.add(sortByCombo);
        dropdownPanel.add(Box.createHorizontalStrut(15));
        dropdownPanel.add(resetButton);
        
        // Cart button
        JButton viewCartButton = ThemeManager.createStyledButton("View Cart");
        viewCartButton.setBackground(ThemeManager.ACCENT_GREEN);
        viewCartButton.setPreferredSize(new Dimension(130, 40));
        ImageIcon cartIcon = getScaledIcon("/icons/cart.png", 20, 20);
        if (cartIcon != null) {
            viewCartButton.setIcon(cartIcon);
        }
        viewCartButton.addActionListener(e -> openCartView());
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        rightPanel.add(viewCartButton);
        
        // Add to the filter panel
        filterPanel.add(searchPanel, BorderLayout.NORTH);
        filterPanel.add(dropdownPanel, BorderLayout.CENTER);
        filterPanel.add(rightPanel, BorderLayout.EAST);
        
        return filterPanel;
    }
    
    private void loadAllData() {
        // Load products
        allProducts = productDAO.getAllProduct();
        
        // Load categories
        allCategories = categoryDAO.getAllCategories();
        
        // Populate category filter
        categoryFilterCombo.removeAllItems();
        categoryFilterCombo.addItem("All Categories");
        for (Category category : allCategories) {
            Integer parentId = category.getParentId();
            if (parentId == null || parentId == 0 || parentId == -1) {
                categoryFilterCombo.addItem(category.getName());
            }
        }
        
        // Get active sale
        activeSale = saleDAO.getActiveSale();
    }
    
    private void filterProducts() {
        if (allProducts == null || allProducts.isEmpty()) {
            statusLabel.setText("No products available");
            return;
        }
        
        // Get filter values
        String searchText = searchField.getText().toLowerCase();
        String categoryFilter = (String) categoryFilterCombo.getSelectedItem();
        String subcategoryFilter = (String) subcategoryFilterCombo.getSelectedItem();
        String sortOption = (String) sortByCombo.getSelectedItem();
        
        // Filter by search text and category
        List<Product> filteredProducts = allProducts.stream()
                .filter(p -> (searchText.isEmpty() || p.getName().toLowerCase().contains(searchText)
                        || p.getDescription().toLowerCase().contains(searchText)))
                .filter(p -> "All Categories".equals(categoryFilter)
                        || getCategoryNameById(p.getCategory_id()).equals(categoryFilter)
                        || isSubcategoryOf(p.getCategory_id(), categoryFilter))
                .filter(p -> subcategoryFilterCombo.isEnabled() ?
                        ("All Subcategories".equals(subcategoryFilter) || getCategoryNameById(p.getCategory_id()).equals(subcategoryFilter)) : true)
                .collect(Collectors.toList());
        
        // Sort the filtered products
        if ("Price: Low to High".equals(sortOption)) {
            filteredProducts.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        } else if ("Price: High to Low".equals(sortOption)) {
            filteredProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        } else if ("Name: A to Z".equals(sortOption)) {
            filteredProducts.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        } else if ("Name: Z to A".equals(sortOption)) {
            filteredProducts.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
        }
        
        // Display the filtered and sorted products
        displayProducts(filteredProducts);
    }
    
    private String getCategoryNameById(int categoryId) {
        for (Category category : allCategories) {
            if (category.getId() == categoryId) {
                return category.getName();
            }
        }
        return "Unknown";
    }
    
    private void displayProducts(List<Product> products) {
        productCardsPanel.removeAll();
        
        if (products.isEmpty()) {
            statusLabel.setText("No products match your criteria");
            productCardsPanel.add(new JLabel("No products found. Try changing your filters."));
        } else {
            statusLabel.setText("Showing " + products.size() + " products");
            
            for (Product product : products) {
                JPanel productCard = createProductCard(product);
                productCardsPanel.add(productCard);
            }
        }
        
        productCardsPanel.revalidate();
        productCardsPanel.repaint();
    }
    
    private JPanel createProductCard(Product product) {
        // Create a card panel with border and shadow effect
        JPanel card = ThemeManager.createCard();
        card.setLayout(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(250, 350));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Product image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(250, 180));
        imagePanel.setBackground(new Color(240, 240, 240));
        
        // Try direct file loading for product images
        try {
            ImageIcon productIcon = null;
            
            // First check if product has an imagePath set
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                try {
                    productIcon = new ImageIcon(product.getImagePath());
                    // Check if the image loaded successfully
                    if (productIcon.getIconWidth() > 1) {
                        // Scale the image to fit the panel
                        Image img = productIcon.getImage().getScaledInstance(250, 180, Image.SCALE_SMOOTH);
                        productIcon = new ImageIcon(img);
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to load image from path: " + product.getImagePath());
                    productIcon = null;
                }
            }
            
            // If no image from imagePath, try resource paths
            if (productIcon == null || productIcon.getIconWidth() <= 1) {
                // Check for actual resources first
                String[] possiblePaths = {
                    "/product_images/" + product.getId() + ".jpg",
                    "/product_images/" + product.getId() + ".png",
                    "/images/products/product_" + product.getId() + ".jpg",
                    "/images/products/product_" + product.getId() + ".png",
                    "/images/products/" + product.getId() + ".jpg",
                    "/images/products/" + product.getId() + ".png"
                };
                
                for (String path : possiblePaths) {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        productIcon = new ImageIcon(url);
                        Image img = productIcon.getImage().getScaledInstance(250, 180, Image.SCALE_SMOOTH);
                        productIcon = new ImageIcon(img);
                        break;
                    }
                }
            }
            
            // If still no image found, create a placeholder
            if (productIcon == null || productIcon.getIconWidth() <= 1) {
                // Try with static demo data - create a sample image for the product
                BufferedImage sampleImage = new BufferedImage(250, 180, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = sampleImage.createGraphics();
                
                // Create a gradient background
                Color startColor, endColor;
                int categoryId = product.getCategory_id();
                
                // Different colors for different categories
                switch (categoryId % 5) {
                    case 0:
                        startColor = new Color(50, 50, 150);
                        endColor = new Color(100, 100, 255);
                        break;
                    case 1:
                        startColor = new Color(150, 50, 50);
                        endColor = new Color(255, 100, 100);
                        break;
                    case 2:
                        startColor = new Color(50, 150, 50);
                        endColor = new Color(100, 255, 100);
                        break;
                    case 3:
                        startColor = new Color(150, 150, 50);
                        endColor = new Color(255, 255, 100);
                        break;
                    default:
                        startColor = new Color(150, 50, 150);
                        endColor = new Color(255, 100, 255);
                }
                
                GradientPaint gradient = new GradientPaint(0, 0, startColor, 250, 180, endColor);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, 250, 180);
                
                // Draw product initial and name
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.setFont(FontManager.getBold(80f));
                
                String initial = product.getName().substring(0, 1).toUpperCase();
                FontMetrics fm = g2d.getFontMetrics();
                int x = (250 - fm.stringWidth(initial)) / 2;
                int y = 180/2 + fm.getAscent()/2;
                g2d.drawString(initial, x, y);
                
                // Draw product name in smaller font
                g2d.setFont(FontManager.getRegular(14f));
                fm = g2d.getFontMetrics();
                String nameShort = product.getName();
                if (nameShort.length() > 25) {
                    nameShort = nameShort.substring(0, 22) + "...";
                }
                x = (250 - fm.stringWidth(nameShort)) / 2;
                y = 180 - 20;
                g2d.drawString(nameShort, x, y);
                
                g2d.dispose();
                
                productIcon = new ImageIcon(sampleImage);
            }
            
            // Add to panel
            JLabel imageLabel = new JLabel(productIcon);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imagePanel.add(imageLabel, BorderLayout.CENTER);
            
        } catch (Exception e) {
            // Fallback to placeholder with initial
            JLabel placeholderLabel = new JLabel(product.getName().substring(0, 1).toUpperCase());
            placeholderLabel.setFont(FontManager.getBold(48f));
            placeholderLabel.setForeground(ThemeManager.PRIMARY_MEDIUM);
            placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imagePanel.add(placeholderLabel, BorderLayout.CENTER);
        }
        
        // Add click listener to image panel
        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProductDetails(product);
            }
        });
        
        // Product info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(ThemeManager.CARD_COLOR);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // Product name
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(FontManager.getBold(16f));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Category
        JLabel categoryLabel = new JLabel(getCategoryNameById(product.getCategory_id()));
        categoryLabel.setFont(FontManager.getItalic(14f));
        categoryLabel.setForeground(ThemeManager.SECONDARY_TEXT_COLOR);
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Price
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pricePanel.setBackground(ThemeManager.CARD_COLOR);
        pricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        double displayPrice = product.getPrice();
        boolean isOnSale = false;
        
        // Check if product is on sale
        if (activeSale != null) {
            isOnSale = true;
            displayPrice = product.getPrice() * (1 - (activeSale.getDiscountPercent() / 100.0));
        }
        
        JLabel priceLabel = new JLabel("Rs. " + String.format("%.2f", displayPrice));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        priceLabel.setForeground(ThemeManager.ACCENT_RED);
        
        pricePanel.add(priceLabel);
        
        // Original price if on sale
        if (isOnSale) {
            JLabel originalPriceLabel = new JLabel(" Rs. " + String.format("%.2f", product.getPrice()));
            originalPriceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            originalPriceLabel.setForeground(ThemeManager.SECONDARY_TEXT_COLOR);
            
            // Add strikethrough to original price
            originalPriceLabel.setText("<html><strike>" + originalPriceLabel.getText() + "</strike></html>");
            
            pricePanel.add(originalPriceLabel);
            
            // Sale badge
            JLabel saleLabel = new JLabel(activeSale.getDiscountPercent() + "% OFF");
            saleLabel.setFont(new Font("Arial", Font.BOLD, 12));
            saleLabel.setForeground(Color.WHITE);
            saleLabel.setBackground(ThemeManager.ACCENT_RED);
            saleLabel.setOpaque(true);
            saleLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            pricePanel.add(Box.createHorizontalStrut(10));
            pricePanel.add(saleLabel);
        }
        
        // Action buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setBackground(ThemeManager.CARD_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add to cart button
        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.setFont(FontManager.getBold(14f));
        addToCartBtn.setBackground(ThemeManager.ACCENT_ORANGE);
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setBorderPainted(false);
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addToCartBtn.addActionListener(e -> addToCart(product));
        
        // Wishlist button
        JButton wishlistBtn = new JButton();
        wishlistBtn.setToolTipText("Add to Wishlist");
        wishlistBtn.setBackground(ThemeManager.CARD_COLOR);
        wishlistBtn.setBorderPainted(false);
        wishlistBtn.setFocusPainted(false);
        wishlistBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Check if product is in wishlist
        boolean inWishlist = wishlistDAO.isInWishlist(currentUser.getId(), product.getId());
        updateWishlistButton(wishlistBtn, inWishlist);
        
        wishlistBtn.addActionListener(e -> toggleWishlist(product, wishlistBtn));
        
        buttonPanel.add(addToCartBtn);
        buttonPanel.add(wishlistBtn);
        
        // Add components to info panel
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(categoryLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(pricePanel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(buttonPanel);
        
        // Add panels to card
        card.add(imagePanel, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Add hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(ThemeManager.HOVER_BORDER);
                card.setBackground(new Color(250, 250, 255)); // Slightly different background color on hover
                card.repaint(); // Force repaint to show changes
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(ThemeManager.DEFAULT_BORDER);
                card.setBackground(ThemeManager.CARD_COLOR); // Reset to default color
                card.repaint(); // Force repaint to show changes
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                showProductDetails(product);
            }
        });
        
        return card;
    }
    
    private void updateWishlistButton(JButton button, boolean inWishlist) {
        if (inWishlist) {
            button.setText("");
            button.setIcon(getScaledIcon("/icons/heart_filled.png", 24, 24));
        } else {
            button.setText("");
            button.setIcon(getScaledIcon("/icons/heart_empty.png", 24, 24));
        }
        button.setToolTipText(inWishlist ? "Remove from Wishlist" : "Add to Wishlist");
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
    }
    
    private void toggleWishlist(Product product, JButton wishlistButton) {
        boolean isInWishlist = wishlistDAO.isInWishlist(currentUser.getId(), product.getId());
        
        if (isInWishlist) {
            // Remove from wishlist
            boolean success = wishlistDAO.removeFromWishlist(currentUser.getId(), product.getId());
            if (success) {
                updateWishlistButton(wishlistButton, false);
                JOptionPane.showMessageDialog(this, 
                        product.getName() + " removed from your wishlist.", 
                        "Removed from Wishlist", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // Add to wishlist
            Wishlist wishlistItem = new Wishlist(currentUser.getId(), product.getId());
            boolean success = wishlistDAO.addToWishlist(wishlistItem);
            if (success) {
                updateWishlistButton(wishlistButton, true);
                JOptionPane.showMessageDialog(this, 
                        product.getName() + " added to your wishlist.", 
                        "Added to Wishlist", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void addToCart(Product product) {
        if (product.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, 
                    "Sorry, this product is out of stock.", 
                    "Out of Stock", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create a quantity dialog
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, product.getStock(), 1);
        JSpinner quantitySpinner = new JSpinner(spinnerModel);
        
        Object[] message = {
                "Select quantity:", quantitySpinner
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, "Add to Cart", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int quantity = (int) quantitySpinner.getValue();
            
            // Create cart item and add to cart
            CartItem cartItem = new CartItem(currentUser.getId(), product.getId(), quantity);
            boolean success = cartItemDAO.addCartItem(cartItem);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                        product.getName() + " added to your cart.", 
                        "Added to Cart", 
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to add item to cart. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showProductDetails(Product product) {
        try {
            // Get the parent window properly
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow == null) {
                // Fallback if no ancestor window is found
                parentWindow = new JFrame();
            }
            
            // Create and show the details view
            ProductDetailsView detailsView = new ProductDetailsView(
                    parentWindow,
                    currentUser,
                    product);
            detailsView.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error showing product details: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error opening product details. Please try again.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openCartView() {
        // Get the parent container (the frame or card layout container)
        Container parent = this.getParent();
        
        // This relies on the HomePage having a method to switch to the cart view
        if (parent instanceof JFrame) {
            JFrame frame = (JFrame) parent;
            // Create and show a new cart dialog
            CartView cartView = new CartView(currentUser);
            JDialog cartDialog = new JDialog(frame, "Your Shopping Cart", Dialog.ModalityType.APPLICATION_MODAL);
            cartDialog.setContentPane(cartView);
            cartDialog.setSize(800, 600);
            cartDialog.setLocationRelativeTo(frame);
            cartDialog.setVisible(true);
            
            // Refresh product display after cart actions
            loadAllData();
            filterProducts();
        } else if (parent != null) {
            // Try to find methods to switch to cart
            java.lang.reflect.Method method = null;
            try {
                method = parent.getClass().getMethod("showCartPanel");
                method.invoke(parent);
            } catch (Exception e) {
                // Fall back to dialog if method not found
                Window window = SwingUtilities.getWindowAncestor(this);
                CartView cartView = new CartView(currentUser);
                JDialog cartDialog = new JDialog(window, "Your Shopping Cart", Dialog.ModalityType.APPLICATION_MODAL);
                cartDialog.setContentPane(cartView);
                cartDialog.setSize(800, 600);
                cartDialog.setLocationRelativeTo(window);
                cartDialog.setVisible(true);
                
                // Refresh product display after cart actions
                loadAllData();
                filterProducts();
            }
        }
    }
    
    // FlowLayout subclass that fully justifies the layout
    private static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }
        
        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        
        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }
        
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                
                if (targetWidth == 0)
                    targetWidth = Integer.MAX_VALUE;
                
                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;
                
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;
                
                int nmembers = target.getComponentCount();
                
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        
                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }
                        
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight + vgap;
                
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;
                
                return dim;
            }
        }
    }
    
    // Add this method to allow HomePage to set the search text and filter products
    public void setSearchTextAndFilter(String searchText) {
        if (searchField != null) {
            searchField.setText(searchText);
            filterProducts();
        }
    }

    // Helper method to safely get scaled icons
    private ImageIcon getScaledIcon(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            
            // If the resource isn't found in the classpath
            if (url == null) {
                System.out.println("Resource not found at: " + path + ", trying alternative paths");
                
                // For wishlist icons, check specific paths
                if (path.contains("heart") || path.contains("wishlist")) {
                    // Try multiple alternative paths for wishlist icons
                    String[] altPaths = {
                        "/icons/wishlist_empty.png",
                        "/icons/heart_empty.png",
                        "/icons/heart-empty.png",
                        "/icons/favorite_empty.png",
                        "/icons/like_empty.png"
                    };
                    
                    if (path.contains("filled") || path.contains("fill")) {
                        altPaths = new String[] {
                            "/icons/wishlist_filled.png",
                            "/icons/heart_filled.png",
                            "/icons/heart-filled.png",
                            "/icons/favorite_filled.png",
                            "/icons/like_filled.png"
                        };
                    }
                    
                    // Try each alternative path
                    for (String altPath : altPaths) {
                        url = getClass().getResource(altPath);
                        if (url != null) {
                            System.out.println("Found alternative icon at: " + altPath);
                            break;
                        }
                    }
                    
                    // If still null, fall back to default color icon
                    if (url == null) {
                        System.out.println("No wishlist icon found, creating colored icon");
                        return createColoredWishlistIcon(width, height, 
                            path.contains("empty") ? new Color(220, 220, 220) : ThemeManager.ACCENT_RED);
                    }
                } else if (path.contains("product_images") || path.contains("products")) {
                    // For product images, try multiple paths
                    String productId = "";
                    
                    // Extract product ID from path
                    if (path.contains("/")) {
                        productId = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
                    }
                    
                    // Try alternative image locations with different naming patterns
                    String[] altPaths = {
                        "/images/products/product_" + productId + ".jpg",
                        "/images/products/product_" + productId + ".png",
                        "/images/product_" + productId + ".jpg",
                        "/images/product_" + productId + ".png",
                        "/product_images/" + productId + ".jpg",
                        "/product_images/" + productId + ".png",
                        "/images/products/product" + productId + ".jpg",
                        "/images/products/product" + productId + ".png"
                    };
                    
                    // Try each alternative path
                    for (String altPath : altPaths) {
                        url = getClass().getResource(altPath);
                        if (url != null) {
                            System.out.println("Found product image at: " + altPath);
                            break;
                        }
                    }
                    
                    // If still null, try a generic product placeholder
                    if (url == null) {
                        String[] placeholderPaths = {
                            "/images/product_placeholder.jpg",
                            "/images/product_placeholder.png",
                            "/images/placeholder.jpg",
                            "/images/placeholder.png",
                            "/product_images/placeholder.jpg",
                            "/product_images/placeholder.png"
                        };
                        
                        for (String placeholderPath : placeholderPaths) {
                            url = getClass().getResource(placeholderPath);
                            if (url != null) {
                                System.out.println("Using placeholder image: " + placeholderPath);
                                break;
                            }
                        }
                    }
                    
                    // If still null, create a colored rectangle with product initial
                    if (url == null) {
                        System.out.println("No product image or placeholder found, creating letter-based placeholder");
                        return null; // Return null to let calling code create a placeholder with the product initial
                    }
                } else {
                    // For other icons, create a colored rectangle icon
                    System.out.println("Resource not found at: " + path + ", creating colored icon");
                    return createColorIcon(width, height, new Color(240, 240, 240));
                }
            }
            
            // Create and return the scaled icon
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            } else {
                return createColorIcon(width, height, new Color(240, 240, 240));
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + path + " - " + e.getMessage());
            // If any error occurs, return a fallback colored icon
            return createColorIcon(width, height, new Color(240, 240, 240));
        }
    }
    
    private ImageIcon createColorIcon(int width, int height, Color color) {
        // Create a BufferedImage filled with the specified color
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return new ImageIcon(image);
    }
    
    // Create a heart-shaped wishlist icon when images aren't available
    private ImageIcon createColoredWishlistIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int[] xPoints = {width / 2, width / 5, 0, 0, width / 5, width / 2, 4 * width / 5, width, width, 4 * width / 5};
        int[] yPoints = {height, 4 * height / 5, 3 * height / 5, 2 * height / 5, 0, height / 5, 0, 2 * height / 5, 3 * height / 5, 4 * height / 5};
        
        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 10);
        g.dispose();
        
        return new ImageIcon(image);
    }

    // Allow setting category from homepage
    public void setCategoryAndFilter(String categoryName) {
        if (categoryFilterCombo != null) {
            for (int i = 0; i < categoryFilterCombo.getItemCount(); i++) {
                if (categoryFilterCombo.getItemAt(i).equals(categoryName)) {
                    categoryFilterCombo.setSelectedIndex(i);
                    filterProducts();
                    break;
                }
            }
        }
    }

    private void updateSubcategoryFilter() {
        subcategoryFilterCombo.removeAllItems();
        allSubcategories = null;
        String selectedCategory = (String) categoryFilterCombo.getSelectedItem();
        if (selectedCategory == null || selectedCategory.equals("All Categories")) {
            subcategoryFilterCombo.setEnabled(false);
            return;
        }
        // Find the selected category id
        int parentId = -1;
        for (Category cat : allCategories) {
            if (cat.getName().equals(selectedCategory)) {
                parentId = cat.getId();
                break;
            }
        }
        if (parentId == -1) {
            subcategoryFilterCombo.setEnabled(false);
            return;
        }
        // Find subcategories (use regular loop to avoid final requirement)
        allSubcategories = new java.util.ArrayList<>();
        for (Category cat : allCategories) {
            Integer catParentId = cat.getParentId();
            if (catParentId != null && catParentId == parentId) {
                allSubcategories.add(cat);
            }
        }
        if (allSubcategories.isEmpty()) {
            subcategoryFilterCombo.setEnabled(false);
            return;
        }
        subcategoryFilterCombo.setEnabled(true);
        subcategoryFilterCombo.addItem("All Subcategories");
        for (Category sub : allSubcategories) {
            subcategoryFilterCombo.addItem(sub.getName());
        }
    }

    private boolean isSubcategoryOf(int categoryId, String parentCategoryName) {
        Category cat = allCategories.stream().filter(c -> c.getId() == categoryId).findFirst().orElse(null);
        if (cat == null) return false;
        Integer catParentId = cat.getParentId();
        if (catParentId == null || catParentId == 0 || catParentId == -1) return false;
        Category parent = allCategories.stream().filter(c -> c.getId() == catParentId).findFirst().orElse(null);
        return parent != null && parent.getName().equals(parentCategoryName);
    }
} 