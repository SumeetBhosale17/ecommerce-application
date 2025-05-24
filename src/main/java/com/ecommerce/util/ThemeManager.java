package com.ecommerce.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

public class ThemeManager {
    // Primary Colors
    public static final Color PRIMARY_DARK = new Color(25, 25, 112);  // Dark Blue
    public static final Color PRIMARY_MEDIUM = new Color(65, 65, 155); // Medium Blue
    public static final Color PRIMARY_LIGHT = new Color(100, 100, 200); // Light Blue
    
    // Accent Colors
    public static final Color ACCENT_ORANGE = new Color(255, 153, 0);
    public static final Color ACCENT_GREEN = new Color(46, 139, 87);
    public static final Color ACCENT_RED = new Color(178, 34, 34);
    
    // Neutral Colors
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    public static final Color CARD_COLOR = new Color(255, 255, 255);
    public static final Color TEXT_COLOR = new Color(33, 33, 33);
    public static final Color SECONDARY_TEXT_COLOR = new Color(100, 100, 100);
    
    // Border
    public static final Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(10, 10, 10, 10)
    );
    
    public static final Border HOVER_BORDER = BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_MEDIUM, 1),
            new EmptyBorder(10, 10, 10, 10)
    );
    
    public static void applyGlobalTheme() {
        try {
            // Set default component fonts using FontManager
            UIManager.put("Button.font", FontManager.getRegular(14f));
            UIManager.put("Label.font", FontManager.getRegular(14f));
            UIManager.put("TextField.font", FontManager.getRegular(14f));
            UIManager.put("TextArea.font", FontManager.getRegular(14f));
            UIManager.put("ComboBox.font", FontManager.getRegular(14f));
            UIManager.put("CheckBox.font", FontManager.getRegular(14f));
            UIManager.put("RadioButton.font", FontManager.getRegular(14f));
            UIManager.put("TabbedPane.font", FontManager.getRegular(14f));
            UIManager.put("Table.font", FontManager.getRegular(14f));
            UIManager.put("TableHeader.font", FontManager.getBold(14f));
            UIManager.put("Menu.font", FontManager.getRegular(14f));
            UIManager.put("MenuItem.font", FontManager.getRegular(14f));
            UIManager.put("OptionPane.messageFont", FontManager.getRegular(14f));
            UIManager.put("OptionPane.buttonFont", FontManager.getRegular(14f));
            
            // Set colors - Enhanced with more vibrant colors
            Color vibrantPrimary = new Color(41, 128, 185); // Vibrant blue
            Color vibrantPrimaryDark = new Color(25, 95, 150); // Darker blue
            
            UIManager.put("Panel.background", new ColorUIResource(BACKGROUND_COLOR));
            UIManager.put("Button.background", new ColorUIResource(vibrantPrimary));
            UIManager.put("Button.foreground", new ColorUIResource(Color.WHITE));
            UIManager.put("Button.select", new ColorUIResource(vibrantPrimaryDark));
            UIManager.put("Button.focus", new ColorUIResource(vibrantPrimary));
            UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(vibrantPrimaryDark, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
            ));
            
            UIManager.put("TextField.background", new ColorUIResource(CARD_COLOR));
            UIManager.put("TextField.foreground", new ColorUIResource(TEXT_COLOR));
            UIManager.put("TextField.caretForeground", new ColorUIResource(PRIMARY_MEDIUM));
            UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 8, 5, 8)
            ));
            
            // Table styling
            UIManager.put("Table.background", new ColorUIResource(CARD_COLOR));
            UIManager.put("Table.foreground", new ColorUIResource(TEXT_COLOR));
            UIManager.put("Table.selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
            UIManager.put("Table.selectionForeground", new ColorUIResource(Color.WHITE));
            UIManager.put("Table.gridColor", new ColorUIResource(new Color(220, 220, 220)));
            
            // Menu styling
            UIManager.put("Menu.background", new ColorUIResource(PRIMARY_DARK));
            UIManager.put("Menu.foreground", new ColorUIResource(Color.WHITE));
            UIManager.put("Menu.selectionBackground", new ColorUIResource(PRIMARY_MEDIUM));
            UIManager.put("Menu.selectionForeground", new ColorUIResource(Color.WHITE));
            UIManager.put("Menu.borderPainted", Boolean.FALSE);
            
            UIManager.put("MenuItem.background", new ColorUIResource(CARD_COLOR));
            UIManager.put("MenuItem.foreground", new ColorUIResource(TEXT_COLOR));
            UIManager.put("MenuItem.selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
            UIManager.put("MenuItem.selectionForeground", new ColorUIResource(Color.WHITE));
            UIManager.put("MenuItem.acceleratorForeground", new ColorUIResource(SECONDARY_TEXT_COLOR));
            
            // OptionPane styling
            UIManager.put("OptionPane.background", new ColorUIResource(CARD_COLOR));
            UIManager.put("OptionPane.messageForeground", new ColorUIResource(TEXT_COLOR));
            
            System.out.println("Global theme applied successfully with Caudex fonts");
        } catch (Exception e) {
            System.err.println("Error applying global theme: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static JButton createStyledButton(String text) {
        // Enhanced colors for buttons
        Color vibrantPrimary = new Color(41, 128, 185); // Vibrant blue
        return createRoundedButton(text, vibrantPrimary);
    }
    
    // Add another method to create a styled button with custom background color
    public static JButton createStyledButton(String text, Color backgroundColor) {
        return createRoundedButton(text, backgroundColor);
    }
    
    // Create a custom button that fixes the hover color issue
    public static JButton createRoundedButton(String text, Color backgroundColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Paint background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                // Paint text
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                g2.drawString(getText(), (getWidth() - textWidth) / 2, 
                            (getHeight() + textHeight / 2) / 2 - fm.getDescent());
                g2.dispose();
            }
        };
        
        button.setFont(FontManager.getBold(14f));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(Math.max(button.getPreferredSize().width, 100), 40));
        
        // Determine if text should be white or black based on background color brightness
        int brightness = (backgroundColor.getRed() * 299 + 
                         backgroundColor.getGreen() * 587 + 
                         backgroundColor.getBlue() * 114) / 1000;
        
        // If background is bright, use dark text; otherwise use white text
        if (brightness > 150) {
            button.setForeground(Color.BLACK);
        } else {
            button.setForeground(Color.WHITE);
        }
        
        // We'll store the original background color as a client property
        button.putClientProperty("original_bg_color", backgroundColor);
        button.putClientProperty("hover_bg_color", hoverColor);
        
        // Use mouse adapter to handle both enter and exit events
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.repaint();
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                // Darker color when pressed
                Color pressedColor = new Color(
                    Math.max(0, hoverColor.getRed() - 20),
                    Math.max(0, hoverColor.getGreen() - 20),
                    Math.max(0, hoverColor.getBlue() - 20)
                );
                button.setBackground(pressedColor);
                button.repaint();
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                // Return to hover color when released but still hovering
                if (button.getBounds().contains(evt.getPoint())) {
                    button.setBackground(hoverColor);
                } else {
                    button.setBackground(backgroundColor);
                }
                button.repaint();
            }
        });
        
        return button;
    }
    
    // Overload the createRoundedButton method to automatically calculate hover color
    public static JButton createRoundedButton(String text, Color backgroundColor) {
        // Calculate a slightly darker color for hover effect
        Color hoverColor = new Color(
            Math.max(0, backgroundColor.getRed() - 20),
            Math.max(0, backgroundColor.getGreen() - 20),
            Math.max(0, backgroundColor.getBlue() - 20)
        );
        
        return createRoundedButton(text, backgroundColor, hoverColor);
    }
    
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(DEFAULT_BORDER);
        return card;
    }
    
    public static JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                
                GradientPaint gp = new GradientPaint(
                        0, 0, PRIMARY_DARK,
                        w, h, PRIMARY_MEDIUM);
                
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
    }
    
    // Inner class for rounded borders
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 8;
            insets.top = insets.bottom = 4;
            return insets;
        }
    }
    
    /**
     * Create a specially styled button for admin dashboard with hover effect
     */
    public static JButton createAdminButton(String text, Color backgroundColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        isHovered = true;
                        setBackground(PRIMARY_DARK);
                        repaint();
                    }
                    
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        isHovered = false;
                        setBackground(backgroundColor);
                        repaint();
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Paint background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                // Paint text
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int stringWidth = fm.stringWidth(getText());
                int stringHeight = fm.getHeight();
                int x = (getWidth() - stringWidth) / 2;
                int y = (getHeight() - stringHeight) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        
        // Basic styling
        button.setFont(FontManager.getBold(14f));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(Math.max(button.getPreferredSize().width, 100), 40));
        
        // Enable custom rendering
        button.setContentAreaFilled(false);
        
        return button;
    }
} 