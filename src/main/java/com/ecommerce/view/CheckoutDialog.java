package com.ecommerce.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.ecommerce.dao.AddressDAO;
import com.ecommerce.dao.AddressDAOImpl;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;

public class CheckoutDialog extends JDialog {
    private final User currentUser;
    private final double totalAmount;
    private final AddressDAO addressDAO;
    private boolean orderPlaced = false;
    
    private JComboBox<String> addressComboBox;
    private JComboBox<String> paymentMethodComboBox;
    private JTextField emailField;
    
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    
    public CheckoutDialog(Window owner, User currentUser, double totalAmount) {
        super(owner, "Checkout", ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.totalAmount = totalAmount;
        this.addressDAO = new AddressDAOImpl();
        
        setupUI();
        loadUserAddresses();
    }
    
    private void setupUI() {
        setSize(500, 400);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Checkout");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Order Summary
        formPanel.add(new JLabel("Total Amount:"));
        formPanel.add(new JLabel("Rs. " + currencyFormat.format(totalAmount)));
        
        // Email for receipt
        formPanel.add(new JLabel("Email for Receipt:"));
        emailField = new JTextField(currentUser.getEmail());
        formPanel.add(emailField);
        
        // Delivery Address
        formPanel.add(new JLabel("Delivery Address:"));
        addressComboBox = new JComboBox<>();
        addressComboBox.setForeground(Color.BLACK);
        formPanel.add(addressComboBox);
        
        // Add New Address button
        formPanel.add(new JLabel("")); // Empty label for alignment
        JButton addAddressButton = new JButton("Add New Address");
        addAddressButton.addActionListener(e -> showAddAddressDialog());
        formPanel.add(addAddressButton);
        
        // Payment Method
        formPanel.add(new JLabel("Payment Method:"));
        paymentMethodComboBox = new JComboBox<>(new String[]{"UPI", "Pay on Delivery"});
        paymentMethodComboBox.setForeground(Color.BLACK);
        paymentMethodComboBox.addActionListener(e -> {
            String method = (String) paymentMethodComboBox.getSelectedItem();
            if (method != null && (method.equals("UPI") || method.equals("CREDIT_CARD") || 
                                   method.equals("DEBIT_CARD") || method.equals("NET_BANKING"))) {
                JButton payButton = new JButton("Pay Now");
                payButton.setBackground(new Color(0, 123, 255));
                payButton.setForeground(Color.BLACK);
                payButton.addActionListener(evt -> showPaymentDialog(method));
                
                // Check if we already added the button
                Component[] components = formPanel.getComponents();
                boolean buttonExists = false;
                for (Component c : components) {
                    if (c instanceof JButton && ((JButton)c).getText().equals("Pay Now")) {
                        buttonExists = true;
                        break;
                    }
                }
                
                if (!buttonExists) {
                    formPanel.add(new JLabel("")); // Empty label for alignment
                    formPanel.add(payButton);
                    revalidate();
                    repaint();
                }
            } else {
                // Remove Pay button if it exists
                Component[] components = formPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    if (components[i] instanceof JButton && ((JButton)components[i]).getText().equals("Pay Now")) {
                        formPanel.remove(i);
                        formPanel.remove(i-1); // Remove the empty label too
                        revalidate();
                        repaint();
                        break;
                    }
                }
            }
        });
        formPanel.add(paymentMethodComboBox);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton placeOrderButton = new JButton("Place Order");
        placeOrderButton.setBackground(new Color(40, 167, 69));
        placeOrderButton.setForeground(Color.BLACK);
        
        cancelButton.addActionListener(e -> dispose());
        placeOrderButton.addActionListener(e -> placeOrder());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(placeOrderButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void showPaymentDialog(String method) {
        JDialog paymentDialog = new JDialog(this, method + " Payment", true);
        paymentDialog.setSize(400, 500);
        paymentDialog.setLocationRelativeTo(this);
        paymentDialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Display QR code from resources
        JLabel qrImageLabel = new JLabel();
        try {
            ImageIcon qrIcon = new ImageIcon(getClass().getResource("/images/payment_qr.png"));
            if (qrIcon.getIconWidth() > 0) {
                // Scale the image if needed
                Image scaledImage = qrIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                qrImageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                qrImageLabel.setText("QR Code Image Not Available");
            }
        } catch (Exception e) {
            qrImageLabel.setText("QR Code Image Not Available");
        }
        
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.add(qrImageLabel);
        contentPanel.add(imagePanel, BorderLayout.CENTER);
        
        // Payment details
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        detailsPanel.add(new JLabel("Amount: Rs. " + currencyFormat.format(totalAmount)));
        detailsPanel.add(new JLabel("Payment Method: " + method));
        detailsPanel.add(new JLabel("Merchant: ZOROJURO DELIVERY EXPRESS LTD"));
        detailsPanel.add(new JLabel("Transaction ID: DEMO-" + System.currentTimeMillis()));
        
        contentPanel.add(detailsPanel, BorderLayout.NORTH);
        
        // Payment instructions
        JTextArea instructionsArea = new JTextArea();
        instructionsArea.setText("DEMO MODE: This is a simulated payment.\n" +
                                 "In a real app, scan this QR code with your payment app.\n" +
                                 "Click 'Payment Complete' after payment.");
        instructionsArea.setEditable(false);
        instructionsArea.setBackground(new Color(255, 255, 200));
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        contentPanel.add(instructionsArea, BorderLayout.SOUTH);
        paymentDialog.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton completeButton = new JButton("Payment Complete");
        completeButton.setBackground(new Color(40, 167, 69));
        completeButton.setForeground(Color.BLACK);
        
        JButton cancelButton = new JButton("Cancel Payment");
        
        completeButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(paymentDialog, 
                    "Payment Successful! You can now place your order.", 
                    "Payment Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            paymentDialog.dispose();
        });
        
        cancelButton.addActionListener(e -> paymentDialog.dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(completeButton);
        paymentDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        paymentDialog.setVisible(true);
    }
    
    private void loadUserAddresses() {
        addressComboBox.removeAllItems();
        
        List<Address> addresses = addressDAO.getAddressesByUserId(currentUser.getId());
        if (addresses.isEmpty()) {
            addressComboBox.addItem("No addresses found - Add a new address");
        } else {
            for (Address address : addresses) {
                String addressText = String.format("%s, %s, %s - %s (ID: %d)", 
                        address.getStreet(), 
                        address.getCity(), 
                        address.getState(), 
                        address.getPincode(),
                        address.getId());
                addressComboBox.addItem(addressText);
            }
        }
    }
    
    private void showAddAddressDialog() {
        JDialog addressDialog = new JDialog(this, "Add New Address", true);
        addressDialog.setSize(400, 300);
        addressDialog.setLocationRelativeTo(this);
        addressDialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField streetField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField stateField = new JTextField();
        JTextField pincodeField = new JTextField();
        
        formPanel.add(new JLabel("Street Address:"));
        formPanel.add(streetField);
        formPanel.add(new JLabel("City:"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("State:"));
        formPanel.add(stateField);
        formPanel.add(new JLabel("Pincode:"));
        formPanel.add(pincodeField);
        
        addressDialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Address");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            String street = streetField.getText().trim();
            String city = cityField.getText().trim();
            String state = stateField.getText().trim();
            String pincode = pincodeField.getText().trim();
            
            if (street.isEmpty() || city.isEmpty() || state.isEmpty() || pincode.isEmpty()) {
                JOptionPane.showMessageDialog(addressDialog, 
                        "Please fill in all address fields.", 
                        "Missing Information", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Address newAddress = new Address();
            newAddress.setUserId(currentUser.getId());
            newAddress.setStreet(street);
            newAddress.setCity(city);
            newAddress.setState(state);
            newAddress.setPincode(pincode);
            
            if (addressDAO.addAddress(newAddress)) {
                JOptionPane.showMessageDialog(addressDialog, 
                        "Address added successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                loadUserAddresses(); // Reload addresses in combo box
                addressDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(addressDialog, 
                        "Failed to add address. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> addressDialog.dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        addressDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        addressDialog.setVisible(true);
    }
    
    private void placeOrder() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid email address for your receipt.", 
                    "Invalid Email", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (addressComboBox.getSelectedItem() == null || 
                addressComboBox.getSelectedItem().toString().startsWith("No addresses found")) {
            JOptionPane.showMessageDialog(this, 
                    "Please add a delivery address before placing your order.", 
                    "Address Required", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extract address ID from combo box selection
        String selectedAddress = addressComboBox.getSelectedItem().toString();
        int addressId = Integer.parseInt(selectedAddress.substring(
                selectedAddress.lastIndexOf("ID: ") + 4, 
                selectedAddress.lastIndexOf(")")));
        
        // Get address details for the receipt
        Address address = addressDAO.getAddressById(addressId);
        String username = currentUser.getUsername();
        String city = address.getCity();
        String state = address.getState();
        String pincode = address.getPincode();
        
        // Get payment method
        String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
        
        // Use OrderService to place the order
        OrderService orderService = new OrderService();
        boolean success = orderService.placeOrder(
                currentUser.getId(), 
                addressId, 
                paymentMethod, 
                email, 
                username,
                city,
                state,
                pincode);
        
        if (success) {
            // Create buttons for viewing the receipt
            int option = JOptionPane.showOptionDialog(
                this,
                "Your order has been placed successfully!\n" +
                "A receipt has been sent to your email and saved locally.",
                "Order Placed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[] {"OK", "View Receipt"},
                "OK"
            );
            
            // If user wants to view the receipt
            if (option == 1) {
                try {
                    File receiptDir = new File("OrderReceipts");
                    // Find the most recent receipt for this user
                    File[] files = receiptDir.listFiles((dir, name) -> name.endsWith(".pdf"));
                    if (files != null && files.length > 0) {
                        // Sort by last modified (newest first)
                        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                        Desktop.getDesktop().open(files[0]);
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Receipt file not found. It may still be generating.",
                            "Receipt Not Found",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Could not open receipt: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
            orderPlaced = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "There was a problem placing your order. Please try again.", 
                    "Order Failed", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isOrderPlaced() {
        return orderPlaced;
    }
} 