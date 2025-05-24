package com.ecommerce.view;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.UserDAOImpl;
import com.ecommerce.model.User;
import com.ecommerce.util.FontManager;
import com.ecommerce.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private UserDAO userDAO;

    public LoginForm() {
        userDAO = new UserDAOImpl();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("E-commerce Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create a gradient background panel
        JPanel backgroundPanel = ThemeManager.createGradientPanel();
        backgroundPanel.setLayout(new BorderLayout());
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(ThemeManager.CARD_COLOR);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

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
        
        // Welcome text
        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(FontManager.getBold(24f));
        titleLabel.setForeground(ThemeManager.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Please login to continue");
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setFont(FontManager.getItalic(16f));
        subtitleLabel.setForeground(ThemeManager.SECONDARY_TEXT_COLOR);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(ThemeManager.CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(FontManager.getRegular(16f));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setFont(FontManager.getRegular(16f));
        usernameField.setPreferredSize(new Dimension(usernameField.getPreferredSize().width, 40));
        formPanel.add(usernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(FontManager.getRegular(16f));
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
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

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        buttonPanel.setBackground(ThemeManager.CARD_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
        
        loginButton = ThemeManager.createStyledButton("Login");
        loginButton.setFont(FontManager.getBold(16f));
        loginButton.setPreferredSize(new Dimension(loginButton.getPreferredSize().width, 45));
        
        registerButton = new JButton("Create New Account");
        registerButton.setFont(FontManager.getRegular(16f));
        registerButton.setForeground(ThemeManager.PRIMARY_MEDIUM);
        registerButton.setBackground(ThemeManager.CARD_COLOR);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Add components to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to background panel with some padding
        backgroundPanel.add(Box.createVerticalStrut(40), BorderLayout.NORTH);
        backgroundPanel.add(mainPanel, BorderLayout.CENTER);
        backgroundPanel.add(Box.createVerticalStrut(40), BorderLayout.SOUTH);
        backgroundPanel.add(Box.createHorizontalStrut(40), BorderLayout.WEST);
        backgroundPanel.add(Box.createHorizontalStrut(40), BorderLayout.EAST);
        
        // Add background panel to frame
        add(backgroundPanel);

        // Add action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticate();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistrationForm();
            }
        });
    }

    private void authenticate() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                if (user.isAdmin()) {
                    openAdminDashboard(user);
                } else {
                    openUserDashboard(user);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during authentication: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegistrationForm() {
        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setVisible(true);
        this.dispose();
    }

    private void openAdminDashboard(User user) {
        new AdminDashboard(user).setVisible(true);
        this.dispose();
    }

    private void openUserDashboard(User user) {
        new HomePage(user).setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }
} 