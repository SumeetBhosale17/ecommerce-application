package com.ecommerce.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import com.ecommerce.model.*;
import com.ecommerce.dao.*;
import com.ecommerce.service.*;

public class UserDashboard extends JFrame {
    private User currentUser;
    private UserDAO userDAO;
    private AddressDAO addressDAO;
    private SaleDAO saleDAO;
    private JTabbedPane tabbedPane;
    
    public UserDashboard(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAOImpl();
        this.addressDAO = new AddressDAOImpl();
        this.saleDAO = new SaleDAOImpl();
        
        setTitle("User Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Header Panel with Sale Banner
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Main Content
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Profile", createProfilePanel());
        tabbedPane.addTab("Orders", createOrdersPanel());
        tabbedPane.addTab("Wishlist", createWishlistPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Welcome, " + currentUser.getUsername());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Sale Banner
        Sale activeSale = saleDAO.getActiveSale();
        if (activeSale != null) {
            JLabel saleLabel = new JLabel(activeSale.getName() + " - " + activeSale.getDiscountPercent() + "% OFF!");
            saleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            saleLabel.setForeground(Color.RED);
            headerPanel.add(saleLabel, BorderLayout.CENTER);
        }
        
        // Logout button
        JButton logoutButton = new JButton("Logout");
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
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Profile form
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        JTextField usernameField = new JTextField(currentUser.getUsername());
        JTextField emailField = new JTextField(currentUser.getEmail());
        JTextField contactField = new JTextField(currentUser.getContact());
        
        // Address section
        Address userAddress = addressDAO.getAddressByUserId(currentUser.getId());
        JTextField streetField = new JTextField(userAddress != null ? userAddress.getStreet() : "");
        JTextField cityField = new JTextField(userAddress != null ? userAddress.getCity() : "");
        JTextField stateField = new JTextField(userAddress != null ? userAddress.getState() : "");
        JTextField zipField = new JTextField(userAddress != null ? userAddress.getZip() : "");
        
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);
        
        // Address section with title
        JPanel addressPanel = new JPanel(new BorderLayout());
        JLabel addressTitle = new JLabel("Address Information");
        addressTitle.setFont(new Font("Arial", Font.BOLD, 14));
        addressPanel.add(addressTitle, BorderLayout.NORTH);
        
        JPanel addressFormPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        addressFormPanel.add(new JLabel("Street:"));
        addressFormPanel.add(streetField);
        addressFormPanel.add(new JLabel("City:"));
        addressFormPanel.add(cityField);
        addressFormPanel.add(new JLabel("State:"));
        addressFormPanel.add(stateField);
        addressFormPanel.add(new JLabel("ZIP:"));
        addressFormPanel.add(zipField);
        
        addressPanel.add(addressFormPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateButton = new JButton("Update Profile");
        updateButton.addActionListener(e -> {
            try {
                // Update user profile
                currentUser.setUsername(usernameField.getText());
                currentUser.setEmail(emailField.getText());
                currentUser.setContact(contactField.getText());
                userDAO.updateUser(currentUser);
                
                // Update address
                Address address = new Address();
                address.setUserId(currentUser.getId());
                address.setStreet(streetField.getText());
                address.setCity(cityField.getText());
                address.setState(stateField.getText());
                address.setZip(zipField.getText());
                
                if (userAddress == null) {
                    addressDAO.addAddress(address);
                } else {
                    address.setId(userAddress.getId());
                    addressDAO.updateAddress(address);
                }
                
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                
                // Refresh the panel to show updated data
                tabbedPane.remove(tabbedPane.indexOfTab("Profile"));
                tabbedPane.addTab("Profile", createProfilePanel());
                tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Profile"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage());
            }
        });
        buttonPanel.add(updateButton);
        
        // Add all panels to main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(addressPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOrdersPanel() {
        // Implementation for orders panel
        return new JPanel();
    }
    
    private JPanel createWishlistPanel() {
        // Implementation for wishlist panel
        return new JPanel();
    }
}