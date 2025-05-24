package com.ecommerce.view;

import com.ecommerce.dao.*;
import com.ecommerce.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.List;

public class ProductDetailsView extends JDialog {
    private final User currentUser;
    private final Product product;
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final CartItemDAO cartItemDAO;
    private final SaleDAO saleDAO;
    private final WishlistDAO wishlistDAO;
    private final RatingDAO ratingDAO;
    
    private JPanel ratingsPanel;
    private JButton rateProductButton;
    private JLabel averageRatingLabel;
    private int userRating = 0;
    
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final Color PRICE_COLOR = new Color(177, 39, 4);
    private final Color BUTTON_COLOR = new Color(255, 153, 0);
    private final Color SALE_COLOR = new Color(177, 39, 4);
    
    public ProductDetailsView(Window owner, User currentUser, Product product) {
        super(owner, "Product Details", Dialog.ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.product = product;
        
        // Initialize DAOs
        this.productDAO = new ProductDAOImpl();
        this.categoryDAO = new CategoryDAOImpl();
        this.cartItemDAO = new CartItemDAOImpl();
        this.saleDAO = new SaleDAOImpl();
        this.wishlistDAO = new WishlistDAOImpl();
        this.ratingDAO = new RatingDAOImpl();
        
        // Initialize UI components
        this.averageRatingLabel = new JLabel();
        
        // Set up UI
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(800, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Main content panel with scroll pane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Add product info section
        contentPanel.add(createProductInfoPanel());
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Add ratings and reviews section
        contentPanel.add(createRatingsPanel());
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Add similar products section
        contentPanel.add(createSimilarProductsPanel());
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom button panel
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        loadRatings();
    }
    
    private JPanel createProductInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Product name and wishlist button
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(nameLabel, BorderLayout.WEST);
        
        // Create wishlist button with a safer approach
        JButton wishlistButton = new JButton();
        try {
            boolean inWishlist = wishlistDAO.isInWishlist(currentUser.getId(), product.getId());
            String iconPath = inWishlist ? "/icons/heart_filled.png" : "/icons/heart_empty.png";
            
            // Use the safer getScaledIcon method
            ImageIcon icon = getScaledIcon(iconPath, 30, 30);
            wishlistButton.setIcon(icon);
            wishlistButton.setBorderPainted(false);
            wishlistButton.setContentAreaFilled(false);
            wishlistButton.setFocusPainted(false);
            
            // Use ActionListener instead of lambda to avoid parameter name conflicts
            wishlistButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    toggleWishlist();
                }
            });
        } catch (Exception ex) {
            // Fallback if icon loading fails
            System.err.println("Error loading wishlist icon: " + ex.getMessage());
            wishlistButton.setText(wishlistDAO.isInWishlist(currentUser.getId(), product.getId()) ? 
                "♥" : "♡");
            
            // Use ActionListener instead of lambda to avoid parameter name conflicts
            wishlistButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    toggleWishlist();
                }
            });
        }
        
        topPanel.add(wishlistButton, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Add image panel
        try {
            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setPreferredSize(new Dimension(300, 300));
            imagePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            
            JLabel imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
            
            // Load and display product image
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                try {
                    ImageIcon originalIcon = new ImageIcon(product.getImagePath());
                    Image scaledImage = originalIcon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                } catch (Exception e) {
                    imageLabel.setText("Image not available");
                }
            } else {
                imageLabel.setText("No image available");
            }
            
            imagePanel.add(imageLabel, BorderLayout.CENTER);
            panel.add(imagePanel, BorderLayout.WEST);
        } catch (Exception e) {
            System.err.println("Error creating image panel: " + e.getMessage());
        }
        
        // Product details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(new EmptyBorder(0, 20, 20, 0));
        
        JLabel priceLabel = new JLabel("Rs. " + String.format("%.2f", product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        detailsPanel.add(priceLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        JLabel stockLabel = new JLabel("In Stock: " + product.getStock());
        stockLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        detailsPanel.add(stockLabel);
        detailsPanel.add(Box.createVerticalStrut(20));
        
        JTextArea descriptionArea = new JTextArea(product.getDescription());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(400, 100));
        detailsPanel.add(descriptionScroll);
        detailsPanel.add(Box.createVerticalStrut(20));
        
        // Quantity selector and add to cart button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel quantityLabel = new JLabel("Quantity:");
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, product.getStock(), 1);
        JSpinner quantitySpinner = new JSpinner(spinnerModel);
        
        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.setBackground(new Color(255, 153, 0));
        addToCartButton.setForeground(Color.BLACK);
        addToCartButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Create separate action listener to avoid parameter naming conflicts
        ActionListener cartActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    int quantity = (int) quantitySpinner.getValue();
                    CartItem cartItem = new CartItem(currentUser.getId(), product.getId(), quantity);
                    if (cartItemDAO.addCartItem(cartItem)) {
                        JOptionPane.showMessageDialog(ProductDetailsView.this, 
                            product.getName() + " added to your cart.", 
                            "Added to Cart", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ProductDetailsView.this, 
                            "Failed to add item to cart. Please try again.", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    System.err.println("Error adding to cart: " + ex.getMessage());
                    JOptionPane.showMessageDialog(ProductDetailsView.this, 
                        "An error occurred. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        addToCartButton.addActionListener(cartActionListener);
        
        bottomPanel.add(quantityLabel);
        bottomPanel.add(quantitySpinner);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(addToCartButton);
        
        detailsPanel.add(bottomPanel);
        panel.add(detailsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRatingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Ratings & Reviews"));
        
        // Add rate this product button
        JPanel rateButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rateProductButton = new JButton("Rate This Product");
        rateProductButton.addActionListener(e -> showRatingDialog());
        rateButtonPanel.add(rateProductButton);
        panel.add(rateButtonPanel, BorderLayout.NORTH);
        
        // Panel for displaying ratings
        ratingsPanel = new JPanel();
        ratingsPanel.setLayout(new BoxLayout(ratingsPanel, BoxLayout.Y_AXIS));
        
        JScrollPane ratingsScrollPane = new JScrollPane(ratingsPanel);
        ratingsScrollPane.setPreferredSize(new Dimension(600, 200));
        panel.add(ratingsScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSimilarProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("You May Also Like"));
        
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        // Get similar products
        List<Product> similarProducts = productDAO.getSimilarProducts(product.getId(), 4);
        
        if (similarProducts.isEmpty()) {
            JLabel noProductsLabel = new JLabel("No similar products found");
            cardsPanel.add(noProductsLabel);
        } else {
            for (Product similarProduct : similarProducts) {
                cardsPanel.add(createProductCard(similarProduct));
            }
        }
        
        panel.add(cardsPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setPreferredSize(new Dimension(200, 280));
        card.setBackground(Color.WHITE);
        
        // Sale banner if applicable
        Sale activeSale = saleDAO.getActiveSale();
        if (activeSale != null) {
            JLabel saleLabel = new JLabel("SALE " + activeSale.getDiscountPercent() + "% OFF");
            saleLabel.setOpaque(true);
            saleLabel.setBackground(SALE_COLOR);
            saleLabel.setForeground(Color.WHITE);
            saleLabel.setFont(new Font("Arial", Font.BOLD, 12));
            saleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            saleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            saleLabel.setBorder(new EmptyBorder(3, 5, 3, 5));
            card.add(saleLabel);
        }

        // Product image
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(180, 180));
        imageLabel.setMaximumSize(new Dimension(180, 180));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Load and display product image
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(product.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            imageLabel.setText("No image available");
        }
        
        // Make image clickable
        imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showProductDetails(product);
            }
        });
        
        // Product name (clickable)
        JLabel nameLabel = new JLabel("<html><u>" + product.getName() + "</u></html>");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nameLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showProductDetails(product);
            }
        });
        
        // Price panel
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pricePanel.setBackground(Color.WHITE);
        
        // Show original price with strikethrough if there's a sale
        if (activeSale != null) {
            JLabel originalPriceLabel = new JLabel("<html><strike>Rs. " + String.format("%.2f", product.getPrice()) + "</strike></html>");
            originalPriceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            originalPriceLabel.setForeground(Color.GRAY);
            pricePanel.add(originalPriceLabel);
            
            // Calculate and show sale price
            double salePrice = product.getPrice() * (1 - activeSale.getDiscountPercent() / 100.0);
            JLabel salePriceLabel = new JLabel("Rs. " + String.format("%.2f", salePrice));
            salePriceLabel.setFont(new Font("Arial", Font.BOLD, 16));
            salePriceLabel.setForeground(PRICE_COLOR);
            pricePanel.add(salePriceLabel);
        } else {
            // Show regular price
            JLabel priceLabel = new JLabel("Rs. " + String.format("%.2f", product.getPrice()));
            priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
            priceLabel.setForeground(PRICE_COLOR);
            pricePanel.add(priceLabel);
        }
        
        // Stock status
        JLabel stockLabel = new JLabel("In Stock: " + product.getStock());
        stockLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add components to card
        card.add(Box.createVerticalStrut(10));
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(pricePanel);
        card.add(Box.createVerticalStrut(5));
        card.add(stockLabel);
        card.add(Box.createVerticalStrut(10));
        
        return card;
    }
    
    private void showProductDetails(Product product) {
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
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);
        return panel;
    }
    
    private void loadRatings() {
        ratingsPanel.removeAll();
        
        // Check if user has already rated
        Rating userRating = ratingDAO.getUserProductRating(currentUser.getId(), product.getId());
        if (userRating != null) {
            rateProductButton.setText("Edit Your Rating");
            this.userRating = userRating.getValue();
        }
        
        // Get all ratings for this product
        List<Rating> ratings = ratingDAO.getRatingsByProductId(product.getId());
        
        if (ratings.isEmpty()) {
            JLabel noRatingsLabel = new JLabel("No ratings yet. Be the first to rate this product!");
            noRatingsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            ratingsPanel.add(noRatingsLabel);
        } else {
            // Display ratings
            for (Rating rating : ratings) {
                JPanel ratingPanel = createRatingPanel(rating);
                ratingsPanel.add(ratingPanel);
                ratingsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        // Update average rating
        double avgRating = ratingDAO.getAverageRatingForProduct(product.getId());
        int ratingCount = ratingDAO.getCountOfRatingsForProduct(product.getId());
        averageRatingLabel.setText(String.format("%.1f★ (%d reviews)", avgRating, ratingCount));
        
        ratingsPanel.revalidate();
        ratingsPanel.repaint();
    }
    
    private JPanel createRatingPanel(Rating rating) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)));
        
        // Rating stars
        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (int i = 1; i <= 5; i++) {
            JLabel starLabel = new JLabel(i <= rating.getValue() ? "★" : "☆");
            starLabel.setFont(new Font("Arial", Font.BOLD, 14));
            starLabel.setForeground(new Color(255, 153, 0));
            starsPanel.add(starLabel);
        }
        
        // Add username if available
        UserDAO userDAO = new UserDAOImpl();
        User ratingUser = userDAO.getUserById(rating.getUserId());
        if (ratingUser != null) {
            starsPanel.add(new JLabel(" - " + ratingUser.getUsername()));
        }
        
        // Add date
        if (rating.getCreatedAt() != null) {
            JLabel dateLabel = new JLabel(rating.getCreatedAt().toString());
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            starsPanel.add(Box.createHorizontalStrut(10));
            starsPanel.add(dateLabel);
        }
        
        // Comment
        JTextArea commentArea = new JTextArea(rating.getComment());
        commentArea.setEditable(false);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBackground(panel.getBackground());
        
        panel.add(starsPanel, BorderLayout.NORTH);
        panel.add(commentArea, BorderLayout.CENTER);
        
        // If this is the current user's rating, add edit/delete buttons
        if (rating.getUserId() == currentUser.getId()) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> showRatingDialog());
            
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete your rating?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (ratingDAO.deleteRating(rating.getId())) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Your rating has been deleted.",
                                "Rating Deleted",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadRatings();
                    }
                }
            });
            
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }
        
        return panel;
    }
    
    private void showRatingDialog() {
        // Check if user has already rated
        Rating existingRating = ratingDAO.getUserProductRating(currentUser.getId(), product.getId());
        
        JDialog dialog = new JDialog(this, "Rate Product", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Rate " + product.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Stars panel
        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        starsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton[] starButtons = new JButton[5];
        
        int initialRating = existingRating != null ? existingRating.getValue() : 0;
        
        for (int i = 0; i < 5; i++) {
            final int starValue = i + 1;
            starButtons[i] = new JButton(starValue <= initialRating ? "★" : "☆");
            starButtons[i].setFont(new Font("Arial", Font.BOLD, 24));
            starButtons[i].setForeground(new Color(255, 153, 0));
            starButtons[i].setBorderPainted(false);
            starButtons[i].setContentAreaFilled(false);
            starButtons[i].setFocusPainted(false);
            
            starButtons[i].addActionListener(e -> {
                // Update all stars
                for (int j = 0; j < 5; j++) {
                    starButtons[j].setText(j < starValue ? "★" : "☆");
                }
                userRating = starValue;
            });
            
            starsPanel.add(starButtons[i]);
        }
        
        // Comment
        JLabel commentLabel = new JLabel("Your Review:");
        commentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea commentArea = new JTextArea(5, 30);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        
        if (existingRating != null && existingRating.getComment() != null) {
            commentArea.setText(existingRating.getComment());
        }
        
        JScrollPane commentScrollPane = new JScrollPane(commentArea);
        commentScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton submitButton = new JButton(existingRating != null ? "Update" : "Submit");
        submitButton.addActionListener(e -> {
            if (userRating == 0) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Please select a rating (1-5 stars).",
                        "Rating Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Rating rating = existingRating != null ? existingRating : new Rating();
            rating.setUserId(currentUser.getId());
            rating.setProductId(product.getId());
            rating.setValue(userRating);
            rating.setComment(commentArea.getText().trim());
            
            boolean success;
            if (existingRating != null) {
                success = ratingDAO.updateRating(rating);
            } else {
                rating.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                success = ratingDAO.addRating(rating);
            }
            
            if (success) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Thank you for your rating!",
                        "Rating Submitted",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadRatings();
            } else {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Error submitting your rating. Please try again.",
                        "Submission Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
        
        // Add components to panel
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(starsPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(commentLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(commentScrollPane);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private String getCategoryName(int categoryId) {
        // Get category from database
        for (Category category : categoryDAO.getAllCategories()) {
            if (category.getId() == categoryId) {
                return category.getName();
            }
        }
        return "Unknown";
    }
    
    private ImageIcon getScaledIcon(String iconPath, int width, int height) {
        try {
            // Check if resource exists
            if (getClass().getResource(iconPath) == null) {
                // Create a colored rectangle icon instead
                return createColorIcon(width, height, new Color(255, 153, 0));
            }
            
            // Resource exists, proceed normally
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(iconPath));
            Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            // If any error occurs, return a fallback colored icon
            return createColorIcon(width, height, new Color(255, 153, 0));
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
    
    private void toggleWishlist() {
        boolean isInWishlist = wishlistDAO.isInWishlist(currentUser.getId(), product.getId());
        
        if (isInWishlist) {
            // Remove from wishlist
            boolean success = wishlistDAO.removeFromWishlist(currentUser.getId(), product.getId());
            if (success) {
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
                JOptionPane.showMessageDialog(this, 
                        product.getName() + " added to your wishlist.", 
                        "Added to Wishlist", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
} 