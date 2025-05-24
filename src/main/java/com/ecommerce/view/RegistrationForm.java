package com.ecommerce.view;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.UserDAOImpl;
import com.ecommerce.model.User;
import com.ecommerce.model.UserRole;
import com.ecommerce.util.FontManager;
import com.ecommerce.util.ThemeManager;
import com.ecommerce.utils.PasswordUtils;
import com.ecommerce.utils.EmailSender;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class RegistrationForm extends JFrame {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField contactField;
    private JButton registerButton;
    private JButton backButton;
    private UserDAO userDAO;

    public RegistrationForm() {
        userDAO = new UserDAOImpl();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("E-commerce Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);  // Increased height to accommodate all fields
        setLocationRelativeTo(null);
        setResizable(false);

        // Create a gradient background panel
        JPanel backgroundPanel = ThemeManager.createGradientPanel();
        backgroundPanel.setLayout(new BorderLayout());
        
        // Main panel with padding (using a scroll pane to handle overflow)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(ThemeManager.CARD_COLOR);
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(ThemeManager.CARD_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // App title
        JLabel appTitle = new JLabel("E-Commerce App");
        appTitle.setHorizontalAlignment(SwingConstants.CENTER);
        appTitle.setFont(new Font("Arial", Font.BOLD, 32));
        appTitle.setForeground(ThemeManager.PRIMARY_DARK);
        titlePanel.add(appTitle, BorderLayout.NORTH);
        
        // Add back to login button at the top
        backButton = new JButton("Back to Login");
        backButton.setFont(FontManager.getBold(14f));
        backButton.setForeground(Color.BLACK);
        backButton.setBackground(new Color(70, 130, 180)); // Steel Blue color
        backButton.setPreferredSize(new Dimension(150, 30));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setBackground(ThemeManager.CARD_COLOR);
        backButtonPanel.add(backButton);
        titlePanel.add(backButtonPanel, BorderLayout.WEST);
        
        // Registration text
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(FontManager.getBold(24f));
        titleLabel.setForeground(ThemeManager.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Please fill in your details");
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setFont(FontManager.getItalic(16f));
        subtitleLabel.setForeground(ThemeManager.SECONDARY_TEXT_COLOR);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Form panel (with more compact spacing)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(ThemeManager.CARD_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);  // Reduced vertical insets
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(FontManager.getRegular(16f));
        formPanel.add(usernameLabel, gbc);

        gbc.gridy = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(FontManager.getRegular(16f));
        usernameField.setPreferredSize(new Dimension(usernameField.getPreferredSize().width, 40));
        formPanel.add(usernameField, gbc);

        // Email field
        gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(FontManager.getRegular(16f));
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 3;
        emailField = new JTextField(20);
        emailField.setFont(FontManager.getRegular(16f));
        emailField.setPreferredSize(new Dimension(emailField.getPreferredSize().width, 40));
        formPanel.add(emailField, gbc);
        
        // Contact field
        gbc.gridy = 4;
        JLabel contactLabel = new JLabel("Contact:");
        contactLabel.setFont(FontManager.getRegular(16f));
        formPanel.add(contactLabel, gbc);

        gbc.gridy = 5;
        contactField = new JTextField(20);
        contactField.setFont(FontManager.getRegular(16f));
        contactField.setPreferredSize(new Dimension(contactField.getPreferredSize().width, 40));
        formPanel.add(contactField, gbc);

        // Password field
        gbc.gridy = 6;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(FontManager.getRegular(16f));
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 7;
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(ThemeManager.CARD_COLOR);
        passwordField = new JPasswordField(20);
        passwordField.setFont(FontManager.getRegular(16f));
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 40));
        passwordField.setEchoChar('\u2022');
        
        // Add show/hide password button
        JButton togglePasswordButton = new JButton("👁");
        togglePasswordButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        togglePasswordButton.setFocusPainted(false);
        togglePasswordButton.setBorderPainted(false);
        togglePasswordButton.setContentAreaFilled(false);
        togglePasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePasswordButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == '\u2022') {
                passwordField.setEchoChar((char) 0);
                togglePasswordButton.setText("🔒");
            } else {
                passwordField.setEchoChar('\u2022');
                togglePasswordButton.setText("👁");
            }
        });
        
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(togglePasswordButton, BorderLayout.EAST);
        formPanel.add(passwordPanel, gbc);

        // Confirm Password field
        gbc.gridy = 8;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(FontManager.getRegular(16f));
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridy = 9;
        JPanel confirmPasswordPanel = new JPanel(new BorderLayout());
        confirmPasswordPanel.setBackground(ThemeManager.CARD_COLOR);
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(FontManager.getRegular(16f));
        confirmPasswordField.setPreferredSize(new Dimension(confirmPasswordField.getPreferredSize().width, 40));
        confirmPasswordField.setEchoChar('\u2022');
        
        // Add show/hide password button for confirm password
        JButton toggleConfirmPasswordButton = new JButton("👁");
        toggleConfirmPasswordButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        toggleConfirmPasswordButton.setFocusPainted(false);
        toggleConfirmPasswordButton.setBorderPainted(false);
        toggleConfirmPasswordButton.setContentAreaFilled(false);
        toggleConfirmPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleConfirmPasswordButton.addActionListener(e -> {
            if (confirmPasswordField.getEchoChar() == '\u2022') {
                confirmPasswordField.setEchoChar((char) 0);
                toggleConfirmPasswordButton.setText("🔒");
            } else {
                confirmPasswordField.setEchoChar('\u2022');
                toggleConfirmPasswordButton.setText("👁");
            }
        });
        
        confirmPasswordPanel.add(confirmPasswordField, BorderLayout.CENTER);
        confirmPasswordPanel.add(toggleConfirmPasswordButton, BorderLayout.EAST);
        formPanel.add(confirmPasswordPanel, gbc);

        // Create a scroll pane for the form (in case window is too small)
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.setBackground(ThemeManager.CARD_COLOR);
        formScrollPane.getViewport().setBackground(ThemeManager.CARD_COLOR);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 0, 15));
        buttonPanel.setBackground(ThemeManager.CARD_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Reduced top padding
        
        registerButton = ThemeManager.createStyledButton("Register");
        registerButton.setFont(FontManager.getBold(16f));
        registerButton.setPreferredSize(new Dimension(registerButton.getPreferredSize().width, 45));
        
        buttonPanel.add(registerButton);

        // Add components to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Wrap the main panel in a scroll pane to handle potential overflow
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainScrollPane.setBackground(ThemeManager.CARD_COLOR);
        mainScrollPane.getViewport().setBackground(ThemeManager.CARD_COLOR);

        // Add main panel to background panel with some padding
        backgroundPanel.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
        backgroundPanel.add(mainScrollPane, BorderLayout.CENTER);
        backgroundPanel.add(Box.createVerticalStrut(15), BorderLayout.SOUTH);
        backgroundPanel.add(Box.createHorizontalStrut(30), BorderLayout.WEST);
        backgroundPanel.add(Box.createHorizontalStrut(30), BorderLayout.EAST);
        
        // Add background panel to frame
        add(backgroundPanel);

        // Add action listeners
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });
    }

    private void registerUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String contact = contactField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validate inputs
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if username already exists
        try {
            if (userDAO.findByUsername(username) != null) {
                JOptionPane.showMessageDialog(this, "Username already exists", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create user object
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setContact(contact);
            user.setPassword(PasswordUtils.hashPassword(password));
            user.setRole(UserRole.USER);

            // Save user to database
            userDAO.create(user);

            // Send welcome email
            try {
                EmailSender emailSender = new EmailSender();
                String subject = "Welcome to Zorojuro E-commerce";
                String body = "Dear " + username + ",\n\n"
                            + "Thank you for registering with Zorojuro E-commerce. Your account has been created successfully.\n\n"
                            + "You can now log in to your account and start shopping!\n\n"
                            + "Best regards,\n"
                            + "The Zorojuro Team";
                
                boolean emailSent = emailSender.sendEmail(email, subject, body, null);
                if (emailSent) {
                    System.out.println("Welcome email sent successfully to: " + email);
                } else {
                    System.out.println("Failed to send welcome email to: " + email);
                }
            } catch (Exception ex) {
                System.err.println("Error sending welcome email: " + ex.getMessage());
                // Continue with registration even if email sending fails
            }

            JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Return to login form
            goBack();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during registration: " + e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBack() {
        new LoginForm().setVisible(true);
        this.dispose();
    }
} 