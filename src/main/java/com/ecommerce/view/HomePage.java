package com.ecommerce.view;

import com.ecommerce.dao.NotificationDAO;
import com.ecommerce.dao.NotificationDAOImpl;
import com.ecommerce.model.Notification;
import com.ecommerce.model.User;
import com.ecommerce.view.CartView;
import com.ecommerce.view.WishlistView;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.UserDAOImpl;
import com.ecommerce.dao.AddressDAO;
import com.ecommerce.dao.AddressDAOImpl;
import com.ecommerce.model.Address;
import com.ecommerce.dao.SaleDAO;
import com.ecommerce.dao.SaleDAOImpl;
import com.ecommerce.model.Sale;
import com.ecommerce.util.FontManager;
import com.ecommerce.util.ThemeManager;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.OrderDAOImpl;
import com.ecommerce.model.Order;
import com.ecommerce.view.OrderDetailsDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomePage extends JFrame {
    private final User currentUser;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel appTitleLabel;
    private JButton productsButton;
    private JButton cartButton;
    private JButton notificationsButton;
    private JLabel notificationBadge;
    private Timer notificationTimer;
    private NotificationDAO notificationDAO;
    private JMenuBar menuBar;
    private JMenu accountMenu;
    private JMenuItem wishlistMenuItem;
    private JMenuItem ordersMenuItem;
    private JMenuItem accountMenuItem;
    private JMenuItem logoutMenuItem;
    private ProductListView productsContentPanel;
    private final UserDAO userDAO = new UserDAOImpl();
    private AddressDAO addressDAO;
    private final SaleDAO saleDAO = new SaleDAOImpl();

    public HomePage(User currentUser) {
        this.currentUser = currentUser;
        this.notificationDAO = new NotificationDAOImpl();
        
        // Initialize AddressDAO safely
        try {
            this.addressDAO = new AddressDAOImpl();
        } catch (Exception e) {
            System.err.println("Error initializing AddressDAO: " + e.getMessage());
            // Continue without addressDAO
        }
        
        setTitle("E-Commerce Application - Welcome " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        createTopPanel();
        createContentPanel();
        createFooter();
        startNotificationTimer();
        showHomePanel();
        
        // Apply fonts immediately after components are created
        setFontsOnAllComponents();
        
        // Schedule a delayed refresh to ensure fonts are applied after rendering
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(100); // Small delay to ensure components are rendered
                refreshUI();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ThemeManager.PRIMARY_DARK);
        topPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left: App title (clickable)
        appTitleLabel = new JLabel("E-Commerce App");
        appTitleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        appTitleLabel.setForeground(Color.WHITE);
        appTitleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        appTitleLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
        appTitleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showHomePanel();
            }
        });
        topPanel.add(appTitleLabel, BorderLayout.WEST);

        // Center: Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBackground(ThemeManager.PRIMARY_DARK);
        
        JTextField searchField = new JTextField(40);
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(500, 40));
        
        JButton searchButton = ThemeManager.createStyledButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton.setPreferredSize(new Dimension(120, 40));
        searchButton.setBackground(ThemeManager.ACCENT_ORANGE);
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        // Right: Notification icon and user account dropdown
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightPanel.setBackground(ThemeManager.PRIMARY_DARK);

        // Notification icon with badge
        notificationsButton = new JButton();
        notificationsButton.setToolTipText("Notifications");
        notificationsButton.setBackground(ThemeManager.PRIMARY_DARK);
        notificationsButton.setBorderPainted(false);
        notificationsButton.setFocusPainted(false);
        notificationsButton.setIcon(getScaledIcon("/icons/notification.png", 35, 35));
        notificationsButton.addActionListener(e -> showNotificationsPanel());

        notificationBadge = new JLabel("0");
        notificationBadge.setOpaque(true);
        notificationBadge.setBackground(ThemeManager.ACCENT_RED);
        notificationBadge.setForeground(Color.WHITE);
        notificationBadge.setFont(new Font("Arial", Font.BOLD, 12));
        notificationBadge.setHorizontalAlignment(SwingConstants.CENTER);
        notificationBadge.setVerticalAlignment(SwingConstants.CENTER);
        notificationBadge.setPreferredSize(new Dimension(20, 20));
        notificationBadge.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        notificationBadge.setVisible(false);

        JPanel notificationPanel = new JPanel(new BorderLayout());
        notificationPanel.setBackground(ThemeManager.PRIMARY_DARK);
        notificationPanel.add(notificationsButton, BorderLayout.CENTER);
        notificationPanel.add(notificationBadge, BorderLayout.EAST);
        rightPanel.add(notificationPanel);

        // User account dropdown (JMenuBar)
        menuBar = new JMenuBar();
        menuBar.setBackground(ThemeManager.PRIMARY_DARK);
        menuBar.setBorderPainted(false);
        
        accountMenu = new JMenu();
        accountMenu.setIcon(getScaledIcon("/icons/profile.png", 35, 35));
        accountMenu.setForeground(Color.WHITE);
        accountMenu.setBackground(ThemeManager.PRIMARY_DARK);
        accountMenu.setBorderPainted(false);
        accountMenu.setOpaque(true);
        
        // Style menu items
        wishlistMenuItem = new JMenuItem("Wishlist");
        wishlistMenuItem.setFont(new Font("Arial", Font.PLAIN, 14));
        wishlistMenuItem.setBackground(ThemeManager.CARD_COLOR);
        wishlistMenuItem.setForeground(ThemeManager.TEXT_COLOR);
        wishlistMenuItem.addActionListener(e -> showWishlistPanel());
        
        ordersMenuItem = new JMenuItem("My Orders");
        ordersMenuItem.setFont(new Font("Arial", Font.PLAIN, 14));
        ordersMenuItem.setBackground(ThemeManager.CARD_COLOR);
        ordersMenuItem.setForeground(ThemeManager.TEXT_COLOR);
        ordersMenuItem.addActionListener(e -> showOrdersPanel());
        
        accountMenuItem = new JMenuItem("My Account");
        accountMenuItem.setFont(new Font("Arial", Font.PLAIN, 14));
        accountMenuItem.setBackground(ThemeManager.CARD_COLOR);
        accountMenuItem.setForeground(ThemeManager.TEXT_COLOR);
        accountMenuItem.addActionListener(e -> showAccountPanel());
        
        logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutMenuItem.setBackground(ThemeManager.CARD_COLOR);
        logoutMenuItem.setForeground(ThemeManager.TEXT_COLOR);
        logoutMenuItem.addActionListener(e -> {
            stopNotificationTimer();
            dispose();
            new LoginForm().setVisible(true);
        });
        
        accountMenu.add(wishlistMenuItem);
        accountMenu.add(ordersMenuItem);
        accountMenu.add(accountMenuItem);
        accountMenu.addSeparator();
        accountMenu.add(logoutMenuItem);
        menuBar.add(accountMenu);
        rightPanel.add(menuBar);

        // Cart button (scaled icon)
        JButton cartButton = new JButton();
        cartButton.setToolTipText("Cart");
        cartButton.setBackground(ThemeManager.PRIMARY_DARK);
        cartButton.setBorderPainted(false);
        cartButton.setFocusPainted(false);
        cartButton.setIcon(getScaledIcon("/icons/cart.png", 35, 35));
        cartButton.addActionListener(e -> showCartPanel());
        rightPanel.add(cartButton);

        topPanel.add(rightPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Make search bar work: when searching, switch to products panel and filter
        searchButton.addActionListener(e -> {
            showProductsPanel();
            if (productsContentPanel != null) {
                productsContentPanel.setSearchTextAndFilter(searchField.getText());
            }
        });
        
        // Also add action listener for pressing Enter in the search field
        searchField.addActionListener(e -> {
            showProductsPanel();
            if (productsContentPanel != null) {
                productsContentPanel.setSearchTextAndFilter(searchField.getText());
            }
        });
    }
    
    private void createContentPanel() {
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        
        // Add different content panels
        JPanel homeContentPanel = createHomeContentPanel();
        productsContentPanel = new ProductListView(currentUser);
        CartView cartContentPanel = new CartView(currentUser);
        WishlistView wishlistContentPanel = new WishlistView(currentUser);
        JPanel ordersContentPanel = createOrdersPanel();
        JPanel notificationsContentPanel = createNotificationsPanel();
        JPanel accountContentPanel = createAccountPanel();
        
        contentPanel.add(homeContentPanel, "HOME");
        contentPanel.add(productsContentPanel, "PRODUCTS");
        contentPanel.add(cartContentPanel, "CART");
        contentPanel.add(wishlistContentPanel, "WISHLIST");
        contentPanel.add(ordersContentPanel, "ORDERS");
        contentPanel.add(notificationsContentPanel, "NOTIFICATIONS");
        contentPanel.add(accountContentPanel, "ACCOUNT");
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHomeContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        // Check for active sales and add banner if any
        Sale activeSale = saleDAO.getActiveSale();
        if (activeSale != null) {
            JPanel saleBannerPanel = createSaleBanner(activeSale);
            panel.add(saleBannerPanel, BorderLayout.NORTH);
        }
        
        // Welcome section
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        welcomePanel.setBackground(new Color(245, 245, 245));
        
        JLabel welcomeLabel = new JLabel("Welcome to our E-Commerce Store");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subLabel = new JLabel("Discover amazing products at great prices");
        subLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton shopNowButton = new JButton("Shop Now");
        shopNowButton.setFont(new Font("Arial", Font.BOLD, 16));
        shopNowButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        shopNowButton.setBackground(new Color(255, 153, 0));
        shopNowButton.setForeground(Color.BLACK);
        shopNowButton.setPreferredSize(new Dimension(150, 45));
        shopNowButton.setMaximumSize(new Dimension(150, 45));
        shopNowButton.addActionListener(e -> showProductsPanel());
        
        // Add components to welcome panel
        welcomePanel.add(Box.createVerticalGlue());
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(20));
        welcomePanel.add(subLabel);
        welcomePanel.add(Box.createVerticalStrut(30));
        welcomePanel.add(shopNowButton);
        welcomePanel.add(Box.createVerticalGlue());
        
        // Featured sections
        JPanel featuredPanel = new JPanel();
        featuredPanel.setLayout(new BoxLayout(featuredPanel, BoxLayout.Y_AXIS));
        featuredPanel.setBorder(new EmptyBorder(20, 30, 30, 30));
        featuredPanel.setBackground(Color.WHITE);
        
        // Add title for featured categories
        JLabel categoriesTitle = new JLabel("Featured Categories");
        categoriesTitle.setFont(new Font("Arial", Font.BOLD, 24));
        categoriesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        featuredPanel.add(categoriesTitle);
        featuredPanel.add(Box.createVerticalStrut(20));
        
        // Create a panel for category cards with horizontal layout
        JPanel categoriesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        categoriesPanel.setBackground(Color.WHITE);
        
        categoriesPanel.add(createCategoryCard("Electronics", e -> showProductsPanel()));
        categoriesPanel.add(createCategoryCard("Clothing", e -> showProductsPanel()));
        categoriesPanel.add(createCategoryCard("Pet Supplies", e -> showProductsPanel()));
        
        featuredPanel.add(categoriesPanel);
        
        panel.add(welcomePanel, BorderLayout.CENTER);
        panel.add(featuredPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSaleBanner(Sale sale) {
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(220, 20, 60)); // Crimson red for urgency
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Create a panel for the sale content with a horizontal box layout
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        
        // Use a standard character instead of emoji for better compatibility
        JLabel saleIconLabel = new JLabel("HOT");
        saleIconLabel.setFont(new Font("Arial", Font.BOLD, 24));
        saleIconLabel.setForeground(Color.WHITE);
        
        // Sale title and details
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        
        // Use simple ASCII characters to avoid encoding problems
        JLabel titleLabel = new JLabel(sale.getName() + "!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        // Format the end date properly to avoid encoding issues
        String endDateStr = "";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            endDateStr = sdf.format(sale.getEndDate());
        } catch (Exception e) {
            endDateStr = sale.getEndDate().toString();
        }
        
        JLabel detailsLabel = new JLabel(String.format("Save %.0f%% on selected items! Ends on %s", 
                sale.getDiscountPercent(), endDateStr));
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        detailsLabel.setForeground(Color.WHITE);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(detailsLabel);
        
        // Shop now button
        JButton shopButton = new JButton("Shop Sale");
        shopButton.setFont(new Font("Arial", Font.BOLD, 14));
        shopButton.setBackground(Color.WHITE);
        shopButton.setForeground(new Color(220, 20, 60));
        shopButton.addActionListener(e -> showProductsPanel());
        
        // Add components to the banner
        contentPanel.add(saleIconLabel);
        contentPanel.add(Box.createHorizontalStrut(20));
        contentPanel.add(textPanel);
        contentPanel.add(Box.createHorizontalGlue());
        contentPanel.add(shopButton);
        
        bannerPanel.add(contentPanel, BorderLayout.CENTER);
        
        return bannerPanel;
    }
    
    private JPanel createCategoryCard(String category, ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setPreferredSize(new Dimension(250, 300)); // Increased size for better visibility
        
        // Category name at the top
        JLabel nameLabel = new JLabel(category);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setBorder(new EmptyBorder(15, 10, 15, 10));
        
        // Image panel in the center
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(250, 200));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Load category-specific image
        String imagePath = "/images/categories/" + category.toLowerCase().replace(" & ", "_").replace(" ", "_") + ".jpg";
        ImageIcon icon = getScaledIcon(imagePath, 250, 200);
        if (icon != null) {
            imageLabel.setIcon(icon);
        } else {
            // If no image is found, show a colored panel with category name
            JPanel fallbackPanel = new JPanel(new BorderLayout());
            fallbackPanel.setPreferredSize(new Dimension(250, 200));
            
            // Set different background colors for different categories
            Color bgColor;
            switch (category.toLowerCase()) {
                case "electronics":
                    bgColor = new Color(70, 130, 180); // Steel Blue
                    break;
                case "clothing":
                    bgColor = new Color(218, 112, 214); // Orchid
                    break;
                case "pet supplies":
                    bgColor = new Color(60, 179, 113); // Medium Sea Green
                    break;
                default:
                    bgColor = new Color(176, 196, 222); // Light Steel Blue
            }
            fallbackPanel.setBackground(bgColor);
            
            JLabel fallbackLabel = new JLabel(category);
            fallbackLabel.setFont(new Font("Arial", Font.BOLD, 24));
            fallbackLabel.setForeground(Color.WHITE);
            fallbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
            fallbackPanel.add(fallbackLabel, BorderLayout.CENTER);
            
            imageLabel.setLayout(new BorderLayout());
            imageLabel.add(fallbackPanel, BorderLayout.CENTER);
        }
        
        // Browse button at the bottom
        JButton browseButton = new JButton("Browse " + category);
        browseButton.setFont(new Font("Arial", Font.BOLD, 14));
        browseButton.setBackground(new Color(255, 153, 0));
        browseButton.setForeground(Color.BLACK);
        browseButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        browseButton.addActionListener(e -> {
            showProductsPanel();
            if (productsContentPanel != null) {
                productsContentPanel.setCategoryAndFilter(category);
            }
        });
        
        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(browseButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("ORDERS");
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("My Orders");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create a table to display orders
        String[] columnNames = {"Order ID", "Date", "Total", "Status", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only make the Actions column editable
                return column == 4;
            }
        };
        
        JTable orderTable = new JTable(model);
        orderTable.setRowHeight(30);
        orderTable.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Set up the action button renderer and editor for the Actions column
        orderTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        orderTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), this));
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load orders
        OrderDAO orderDAO = new OrderDAOImpl();
        List<Order> orders = orderDAO.getOrdersByUserId(currentUser.getId());
        
        if (orders.isEmpty()) {
            // If no orders, show a message
            JPanel noOrdersPanel = new JPanel();
            noOrdersPanel.setLayout(new BoxLayout(noOrdersPanel, BoxLayout.Y_AXIS));
            
            JLabel noOrdersLabel = new JLabel("You don't have any orders yet.");
            noOrdersLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noOrdersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JButton shopButton = new JButton("Start Shopping");
            shopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            shopButton.addActionListener(e -> showProductsPanel());
            
            noOrdersPanel.add(Box.createVerticalGlue());
            noOrdersPanel.add(noOrdersLabel);
            noOrdersPanel.add(Box.createVerticalStrut(20));
            noOrdersPanel.add(shopButton);
            noOrdersPanel.add(Box.createVerticalGlue());
            
            panel.remove(scrollPane);
            panel.add(noOrdersPanel, BorderLayout.CENTER);
        } else {
            // If there are orders, populate the table
            for (Order order : orders) {
                model.addRow(new Object[]{
                    order.getId(),
                    order.getOrderDate(),
                    "Rs. " + String.format("%.2f", order.getTotalAmount()),
                    order.getOrderStatus(),
                    "View Details"
                });
            }
        }
        
        // Refresh button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh Orders");
        refreshButton.addActionListener(e -> refreshOrdersPanel());
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshOrdersPanel() {
        try {
            // Re-create the orders panel with fresh data
            contentPanel.remove(contentPanel.getComponent(contentPanel.getComponentCount() - 1)); // Remove the current orders panel
            JPanel ordersPanel = createOrdersPanel(); // Create fresh panel
            contentPanel.add(ordersPanel, "orders"); // Add with "orders" identifier
            
            // Force revalidate and repaint
            contentPanel.revalidate();
            contentPanel.repaint();
            
            // Show the updated panel
            cardLayout.show(contentPanel, "orders");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Error refreshing orders: " + e.getMessage(), 
                    "Refresh Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Add these inner classes to handle the View Details button
    // Custom Button Renderer for action column
    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    
    // Custom Button Editor for action column
    static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;
        private final HomePage homePage;
        private int row;
        private JTable table;  // Add a table reference
        
        public ButtonEditor(JCheckBox checkBox, HomePage homePage) {
            super(checkBox);
            this.homePage = homePage;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            this.row = row; // Store the row index when the button is clicked
            this.table = table; // Store the table reference
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                if ("View Details".equals(label)) {
                    // Get the order ID from the first column of the selected row
                    int orderId = Integer.parseInt(table.getValueAt(row, 0).toString());
                    showOrderDetails(orderId);
                }
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
        
        private void showOrderDetails(int orderId) {
            // Create and show order details dialog
            OrderDetailsDialog dialog = new OrderDetailsDialog(homePage, orderId);
            dialog.setVisible(true);
        }
    }
    
    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // This will be populated with actual notifications
        JPanel notificationsListPanel = new JPanel();
        notificationsListPanel.setLayout(new BoxLayout(notificationsListPanel, BoxLayout.Y_AXIS));
        
        refreshNotificationsPanel(notificationsListPanel);
        
        JScrollPane scrollPane = new JScrollPane(notificationsListPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton markAllReadButton = new JButton("Mark All as Read");
        markAllReadButton.addActionListener(e -> {
            // Mark all notifications as read
            List<Notification> notifications = notificationDAO.getNotificationsByUserId(currentUser.getId());
            for (Notification notification : notifications) {
                notification.setRead(true);
                notificationDAO.updateNotification(notification);
            }
            refreshNotificationsPanel(notificationsListPanel);
            updateNotificationBadge();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(markAllReadButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshNotificationsPanel(JPanel notificationsPanel) {
        notificationsPanel.removeAll();
        
        List<Notification> notifications = notificationDAO.getNotificationsByUserId(currentUser.getId());
        
        if (notifications.isEmpty()) {
            JLabel noNotificationsLabel = new JLabel("You don't have any notifications.");
            noNotificationsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            notificationsPanel.add(noNotificationsLabel);
        } else {
            for (Notification notification : notifications) {
                JPanel notificationCard = createNotificationCard(notification);
                notificationsPanel.add(notificationCard);
                notificationsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        notificationsPanel.revalidate();
        notificationsPanel.repaint();
    }
    
    private JPanel createNotificationCard(Notification notification) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        if (!notification.isRead()) {
            card.setBackground(new Color(240, 248, 255)); // Light blue for unread
        }
        
        JLabel messageLabel = new JLabel(notification.getMessage());
        JLabel timeLabel = new JLabel(notification.getCreatedAt().toString());
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        
        JButton markReadButton = new JButton("Mark as Read");
        markReadButton.addActionListener(e -> {
            notification.setRead(true);
            notificationDAO.updateNotification(notification);
            card.setBackground(null);
            markReadButton.setEnabled(false);
            updateNotificationBadge();
        });
        markReadButton.setEnabled(!notification.isRead());
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(messageLabel, BorderLayout.CENTER);
        infoPanel.add(timeLabel, BorderLayout.SOUTH);
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(markReadButton, BorderLayout.EAST);
        
        return card;
    }
    
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(ThemeManager.BACKGROUND_COLOR);
        
        // Page title with gradient panel
        JPanel titlePanel = ThemeManager.createGradientPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("My Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Content panel with cards for each section
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ThemeManager.BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // User profile card
        JPanel profilePanel = ThemeManager.createCard();
        profilePanel.setLayout(new BorderLayout(20, 10));
        profilePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Profile icon and title in a header panel
        JPanel profileHeaderPanel = new JPanel(new BorderLayout(10, 0));
        profileHeaderPanel.setBackground(ThemeManager.CARD_COLOR);
        
        JLabel profileIcon = new JLabel(getScaledIcon("/icons/profile.png", 32, 32));
        JLabel profileTitle = new JLabel("Profile Information");
        profileTitle.setFont(new Font("Arial", Font.BOLD, 20));
        profileTitle.setForeground(ThemeManager.PRIMARY_DARK);
        
        profileHeaderPanel.add(profileIcon, BorderLayout.WEST);
        profileHeaderPanel.add(profileTitle, BorderLayout.CENTER);
        
        // Profile info panel with grid layout
        JPanel profileInfoPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        profileInfoPanel.setBackground(ThemeManager.CARD_COLOR);
        profileInfoPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel usernameValueLabel = new JLabel(currentUser.getUsername());
        usernameValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Email field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel emailValueLabel = new JLabel(currentUser.getEmail());
        emailValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Contact field if available
        JLabel contactLabel = new JLabel("Contact:");
        contactLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel contactValueLabel = new JLabel(currentUser.getContact() != null ? currentUser.getContact() : "Not provided");
        contactValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Account type field
        JLabel accountTypeLabel = new JLabel("Account Type:");
        accountTypeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel accountTypeValueLabel = new JLabel(currentUser.isAdmin() ? "Administrator" : "Customer");
        accountTypeValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Add fields to the info panel
        profileInfoPanel.add(usernameLabel);
        profileInfoPanel.add(usernameValueLabel);
        profileInfoPanel.add(emailLabel);
        profileInfoPanel.add(emailValueLabel);
        profileInfoPanel.add(contactLabel);
        profileInfoPanel.add(contactValueLabel);
        profileInfoPanel.add(accountTypeLabel);
        profileInfoPanel.add(accountTypeValueLabel);
        
        // Button panel
        JPanel profileButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        profileButtonPanel.setBackground(ThemeManager.CARD_COLOR);
        
        JButton editProfileButton = ThemeManager.createStyledButton("Edit Profile");
        editProfileButton.addActionListener(e -> showEditAccountDialog());
        profileButtonPanel.add(editProfileButton);
        
        // Assemble profile panel
        profilePanel.add(profileHeaderPanel, BorderLayout.NORTH);
        profilePanel.add(profileInfoPanel, BorderLayout.CENTER);
        profilePanel.add(profileButtonPanel, BorderLayout.SOUTH);
        
        // Address panel
        JPanel addressPanel = ThemeManager.createCard();
        addressPanel.setLayout(new BorderLayout(20, 10));
        addressPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Address icon and title in a header panel
        JPanel addressHeaderPanel = new JPanel(new BorderLayout(10, 0));
        addressHeaderPanel.setBackground(ThemeManager.CARD_COLOR);
        
        JLabel addressIcon = new JLabel(getScaledIcon("/icons/location.png", 32, 32));
        JLabel addressTitle = new JLabel("My Addresses");
        addressTitle.setFont(new Font("Arial", Font.BOLD, 20));
        addressTitle.setForeground(ThemeManager.PRIMARY_DARK);
        
        addressHeaderPanel.add(addressIcon, BorderLayout.WEST);
        addressHeaderPanel.add(addressTitle, BorderLayout.CENTER);
        
        // Address content panel
        JPanel addressContentPanel = new JPanel();
        addressContentPanel.setBackground(ThemeManager.CARD_COLOR);
        
        if (addressDAO != null) {
            Address address = addressDAO.getAddressByUserId(currentUser.getId());
            
            if (address != null) {
                addressContentPanel.setLayout(new GridLayout(0, 2, 15, 15));
                addressContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
                
                // Street field
                JLabel streetLabel = new JLabel("Street:");
                streetLabel.setFont(new Font("Arial", Font.BOLD, 16));
                
                JLabel streetValueLabel = new JLabel(address.getStreet());
                streetValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                
                // City field
                JLabel cityLabel = new JLabel("City:");
                cityLabel.setFont(new Font("Arial", Font.BOLD, 16));
                
                JLabel cityValueLabel = new JLabel(address.getCity());
                cityValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                
                // State field
                JLabel stateLabel = new JLabel("State:");
                stateLabel.setFont(new Font("Arial", Font.BOLD, 16));
                
                JLabel stateValueLabel = new JLabel(address.getState());
                stateValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                
                // Pincode field
                JLabel pincodeLabel = new JLabel("Pincode:");
                pincodeLabel.setFont(new Font("Arial", Font.BOLD, 16));
                
                JLabel pincodeValueLabel = new JLabel(address.getPincode());
                pincodeValueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                
                // Add fields to the content panel
                addressContentPanel.add(streetLabel);
                addressContentPanel.add(streetValueLabel);
                addressContentPanel.add(cityLabel);
                addressContentPanel.add(cityValueLabel);
                addressContentPanel.add(stateLabel);
                addressContentPanel.add(stateValueLabel);
                addressContentPanel.add(pincodeLabel);
                addressContentPanel.add(pincodeValueLabel);
            } else {
                addressContentPanel.setLayout(new BorderLayout());
                
                JLabel noAddressLabel = new JLabel("No address has been added yet.");
                noAddressLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                noAddressLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noAddressLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
                
                addressContentPanel.add(noAddressLabel, BorderLayout.CENTER);
            }
        } else {
            addressContentPanel.setLayout(new BorderLayout());
            
            JLabel unavailableLabel = new JLabel("Address management is temporarily unavailable.");
            unavailableLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            unavailableLabel.setHorizontalAlignment(SwingConstants.CENTER);
            unavailableLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            
            addressContentPanel.add(unavailableLabel, BorderLayout.CENTER);
        }
        
        // Address button panel
        JPanel addressButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addressButtonPanel.setBackground(ThemeManager.CARD_COLOR);
        
        JButton addAddressButton = ThemeManager.createStyledButton(addressDAO != null && addressDAO.getAddressByUserId(currentUser.getId()) != null ? 
                "Edit Address" : "Add Address");
        addAddressButton.addActionListener(e -> showAddAddressDialog());
        addressButtonPanel.add(addAddressButton);
        
        // Assemble address panel
        addressPanel.add(addressHeaderPanel, BorderLayout.NORTH);
        addressPanel.add(addressContentPanel, BorderLayout.CENTER);
        addressPanel.add(addressButtonPanel, BorderLayout.SOUTH);
        
        // Add panels to content panel with some spacing
        contentPanel.add(profilePanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(addressPanel);
        
        // Add scroll capability
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(ThemeManager.BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(ThemeManager.BACKGROUND_COLOR);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // Show edit dialog (real implementation)
    private void showEditAccountDialog() {
        JTextField usernameField = new JTextField(currentUser.getUsername());
        JTextField emailField = new JTextField(currentUser.getEmail());
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            currentUser.setUsername(usernameField.getText());
            currentUser.setEmail(emailField.getText());
            try {
                userDAO.update(currentUser);
                JOptionPane.showMessageDialog(this, "Profile updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Show add/edit address dialog (real implementation)
    private void showAddAddressDialog() {
        JTextField addressField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField stateField = new JTextField();
        JTextField zipField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        panel.add(new JLabel("City:"));
        panel.add(cityField);
        panel.add(new JLabel("State:"));
        panel.add(stateField);
        panel.add(new JLabel("ZIP Code:"));
        panel.add(zipField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Address", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Address address = new Address();
            address.setUserId(currentUser.getId());
            address.setStreet(addressField.getText());
            address.setCity(cityField.getText());
            address.setState(stateField.getText());
            address.setPincode(zipField.getText());
            if (addressDAO.addAddress(address)) {
                JOptionPane.showMessageDialog(this, "Address added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add address.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void createFooter() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(35, 47, 62));
        footerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel footerLabel = new JLabel("© 2025 E-Commerce App (ZOROJURO DELIVERY EXPRESS LTD.)- All Rights Reserved");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void startNotificationTimer() {
        notificationTimer = new Timer();
        notificationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateNotificationBadge();
            }
        }, 0, 30000); // Check every 30 seconds
    }
    
    private void stopNotificationTimer() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
            notificationTimer = null;
        }
    }
    
    private void updateNotificationBadge() {
        SwingUtilities.invokeLater(() -> {
            int unreadCount = notificationDAO.countUnreadNotifications(currentUser.getId());
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(unreadCount > 0);
        });
    }
    
    // Methods to switch between panels
    public void showHomePanel() {
        cardLayout.show(contentPanel, "HOME");
    }
    
    public void showProductsPanel() {
        cardLayout.show(contentPanel, "PRODUCTS");
    }
    
    public void showCartPanel() {
        // Refresh the cart view
        CartView cartView = new CartView(currentUser);
        contentPanel.remove(2); // Remove old cart panel
        contentPanel.add(cartView, "CART", 2); // Add new cart panel
        
        cardLayout.show(contentPanel, "CART");
    }
    
    public void showOrdersPanel() {
        try {
            // Refresh the orders panel with updated data
            JPanel ordersPanel = createOrdersPanel();
            
            // Find the index of the "ORDERS" panel
            int ordersIndex = -1;
            for (int i = 0; i < contentPanel.getComponentCount(); i++) {
                Component comp = contentPanel.getComponent(i);
                if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("ORDERS")) {
                    ordersIndex = i;
                    break;
                }
            }
            
            // Remove the old panel and add the new one
            if (ordersIndex >= 0) {
                contentPanel.remove(ordersIndex);
                contentPanel.add(ordersPanel, "ORDERS", ordersIndex);
            } else {
                // If not found, add at the default position
                contentPanel.add(ordersPanel, "ORDERS");
            }
            
            // Force revalidate and repaint
            contentPanel.revalidate();
            contentPanel.repaint();
            
            // Show the orders panel
            cardLayout.show(contentPanel, "ORDERS");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error showing orders panel: " + e.getMessage());
        }
    }
    
    public void showNotificationsPanel() {
        // Refresh notifications panel
        JPanel notificationsPanel = createNotificationsPanel();
        contentPanel.remove(4); // Remove old notifications panel
        contentPanel.add(notificationsPanel, "NOTIFICATIONS", 4); // Add new notifications panel
        
        cardLayout.show(contentPanel, "NOTIFICATIONS");
    }
    
    public void showAccountPanel() {
        cardLayout.show(contentPanel, "ACCOUNT");
    }
    
    public void showWishlistPanel() {
        // Refresh the wishlist view
        WishlistView wishlistView = new WishlistView(currentUser);
        contentPanel.remove(3); // Remove old wishlist panel
        contentPanel.add(wishlistView, "WISHLIST", 3); // Add new wishlist panel
        
        cardLayout.show(contentPanel, "WISHLIST");
    }
    
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(ThemeManager.PRIMARY_MEDIUM);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Helper to scale icons
    private ImageIcon getScaledIcon(String path, int width, int height) {
        URL iconUrl = getClass().getResource(path);
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }

    // Use Caudex font throughout the application
    private void setFontsOnAllComponents() {
        // Only apply default fonts to components that don't have specific fonts set
        for (Component component : getComponents()) {
            if (component instanceof Container) {
                applyFontToContainer((Container) component, null);
            }
        }
        
        // Force refresh of UI components
        SwingUtilities.updateComponentTreeUI(this);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // Ensure fonts are applied when the window becomes visible
            SwingUtilities.invokeLater(() -> {
                setFontsOnAllComponents();
                refreshUI();
            });
        }
    }

    // Method to refresh the UI and ensure fonts are applied
    private void refreshUI() {
        // Apply fonts and request UI updates
        setFontsOnAllComponents();
        repaint();
        revalidate();
    }

    // Replace the existing applyFontToContainer with a method that only applies fonts to components without specific fonts
    private void applyFontToContainer(Container container, Font font) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                // Skip components that already have specific fonts set
                if (label.getFont().getFamily().equals("Arial")) {
                    continue;
                }
                label.setFont(FontManager.getRegular(14f));
            } else if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getFont().getFamily().equals("Arial")) {
                    continue;
                }
                button.setFont(FontManager.getRegular(14f));
            } else if (component instanceof Container) {
                applyFontToContainer((Container) component, font);
            }
        }
    }
} 