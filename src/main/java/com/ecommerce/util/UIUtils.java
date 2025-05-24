package com.ecommerce.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Utility class for UI enhancements
 */
public class UIUtils {
    
    // Vibrant colors for buttons
    private static final Color PRIMARY_COLOR = new Color(0, 123, 255);       // Bright Blue
    private static final Color SUCCESS_COLOR = new Color(40, 220, 120);      // Vibrant Green
    private static final Color DANGER_COLOR = new Color(255, 65, 54);        // Bright Red
    private static final Color WARNING_COLOR = new Color(255, 198, 10);      // Bright Yellow
    private static final Color NEUTRAL_COLOR = new Color(75, 85, 105);       // Modern Slate Gray
    
    // Shadow colors
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);        // More visible shadow
    
    /**
     * Stylize a button with rounded corners, hover effects, and hand cursor
     * @param button The button to stylize
     */
    public static void styleButton(JButton button) {
        styleButton(button, PRIMARY_COLOR, Color.WHITE, 15);
    }
    
    /**
     * Stylize a button as a success button (green)
     * @param button The button to stylize
     */
    public static void styleSuccessButton(JButton button) {
        styleButton(button, SUCCESS_COLOR, Color.WHITE, 15);
    }
    
    /**
     * Stylize a button as a danger button (red)
     * @param button The button to stylize
     */
    public static void styleDangerButton(JButton button) {
        styleButton(button, DANGER_COLOR, Color.WHITE, 15);
    }
    
    /**
     * Stylize a button as a warning button (yellow)
     * @param button The button to stylize
     */
    public static void styleWarningButton(JButton button) {
        styleButton(button, WARNING_COLOR, Color.BLACK, 15);
    }
    
    /**
     * Stylize a button as a neutral button (gray)
     * @param button The button to stylize
     */
    public static void styleNeutralButton(JButton button) {
        styleButton(button, NEUTRAL_COLOR, Color.WHITE, 15);
    }
    
    /**
     * Stylize a button with custom colors and rounded corners
     * @param button The button to stylize
     * @param backgroundColor The background color
     * @param foregroundColor The text color
     * @param radius The corner radius in pixels
     */
    public static void styleButton(JButton button, Color backgroundColor, Color foregroundColor, int radius) {
        // Set colors
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        
        // Create rounded border with larger padding for better visibility
        Border line = new LineBorder(backgroundColor.darker(), 1, true);
        Border margin = new EmptyBorder(10, 24, 10, 24);
        button.setBorder(new CompoundBorder(line, margin));
        
        // Make sure button is visible and has appropriate dimensions
        Dimension buttonSize = button.getPreferredSize();
        buttonSize.height = Math.max(buttonSize.height, 40); // Increased minimum height
        button.setPreferredSize(buttonSize);
        
        // Enable rounded corners
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        
        // Set hand cursor on hover
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Brighten the color slightly on hover
                Color hoverColor = new Color(
                    Math.min(backgroundColor.getRed() + 15, 255),
                    Math.min(backgroundColor.getGreen() + 15, 255),
                    Math.min(backgroundColor.getBlue() + 15, 255)
                );
                button.setBackground(hoverColor);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
                button.repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                // Darken color when pressed
                Color pressedColor = new Color(
                    Math.max(backgroundColor.getRed() - 30, 0),
                    Math.max(backgroundColor.getGreen() - 30, 0),
                    Math.max(backgroundColor.getBlue() - 30, 0)
                );
                button.setBackground(pressedColor);
                button.repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                // Return to hover color if still hovering
                if (button.contains(e.getPoint())) {
                    Color hoverColor = new Color(
                        Math.min(backgroundColor.getRed() + 15, 255),
                        Math.min(backgroundColor.getGreen() + 15, 255),
                        Math.min(backgroundColor.getBlue() + 15, 255)
                    );
                    button.setBackground(hoverColor);
                } else {
                    button.setBackground(backgroundColor);
                }
                button.repaint();
            }
        });
        
        // Custom painting for rounded corners with shadow
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void update(Graphics g, JComponent c) {
                if (c.isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int w = c.getWidth();
                    int h = c.getHeight();
                    
                    // Draw shadow first (offset by 3 pixels and slightly larger for more visibility)
                    g2.setColor(SHADOW_COLOR);
                    g2.fill(new RoundRectangle2D.Float(3, 4, w - 4, h - 4, radius, radius));
                    
                    // Draw button background
                    g2.setColor(button.getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius * 1.5f, radius * 1.5f));
                    
                    // Draw subtle highlight at top (gradient effect)
                    g2.setColor(new Color(255, 255, 255, 90)); // More visible highlight
                    g2.fillRect(2, 2, w - 4, h/3);
                    
                    g2.dispose();
                }
                paint(g, c);
            }
        });
    }
    
    /**
     * Apply UI theme to a container and all its children
     * @param container The container to apply theme to
     */
    public static void applyThemeToContainer(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                
                // Style based on button text/function
                String buttonText = button.getText().toLowerCase();
                if (buttonText.contains("cancel") || buttonText.contains("delete") || buttonText.contains("remove")) {
                    styleDangerButton(button);
                } else if (buttonText.contains("ok") || buttonText.contains("save") || 
                        buttonText.contains("add") || buttonText.contains("place order")) {
                    styleSuccessButton(button);
                } else if (buttonText.contains("pay now")) {
                    // Bright blue for payment buttons
                    styleButton(button, new Color(0, 123, 255), Color.WHITE, 15);
                } else {
                    styleButton(button);
                }
            } else if (comp instanceof Container) {
                // Recursively apply to all containers
                applyThemeToContainer((Container) comp);
            }
        }
    }
} 