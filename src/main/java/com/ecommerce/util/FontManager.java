package com.ecommerce.util;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontManager {
    private static final Map<String, Font> fontCache = new HashMap<>();
    
    // Font names (kept for API compatibility)
    public static final String CAUDEX_REGULAR = "Caudex-Regular";
    public static final String CAUDEX_BOLD = "Caudex-Bold";
    public static final String CAUDEX_ITALIC = "Caudex-Italic";
    public static final String CAUDEX_BOLD_ITALIC = "Caudex-BoldItalic";
    
    // Font sizes
    private static final float DEFAULT_SIZE = 14f;
    private static final float TITLE_SIZE = 32f;
    private static final float SUBTITLE_SIZE = 18f;
    
    // Default system fonts as true fallbacks
    private static final String DEFAULT_FONT = "SansSerif";
    
    static {
        loadFonts();
    }
    
    private static void loadFonts() {
        try {
            System.out.println("Loading Caudex fonts...");
            
            // Check if font files exist in resources before attempting to load
            boolean regularExists = FontManager.class.getResource("/fonts/Caudex-Regular.ttf") != null;
            boolean boldExists = FontManager.class.getResource("/fonts/Caudex-Bold.ttf") != null;
            boolean italicExists = FontManager.class.getResource("/fonts/Caudex-Italic.ttf") != null;
            boolean boldItalicExists = FontManager.class.getResource("/fonts/Caudex-BoldItalic.ttf") != null;
            
            System.out.println("Font resources found: Regular=" + regularExists + ", Bold=" + boldExists + 
                               ", Italic=" + italicExists + ", BoldItalic=" + boldItalicExists);
            
            if (regularExists) registerFont(CAUDEX_REGULAR, "/fonts/Caudex-Regular.ttf");
            if (boldExists) registerFont(CAUDEX_BOLD, "/fonts/Caudex-Bold.ttf");
            if (italicExists) registerFont(CAUDEX_ITALIC, "/fonts/Caudex-Italic.ttf");
            if (boldItalicExists) registerFont(CAUDEX_BOLD_ITALIC, "/fonts/Caudex-BoldItalic.ttf");
            
            // If no fonts were loaded, populate cache with system fonts
            if (fontCache.isEmpty()) {
                System.out.println("No Caudex fonts were loaded, using system fonts as fallback");
                fontCache.put(CAUDEX_REGULAR, new Font(DEFAULT_FONT, Font.PLAIN, (int)DEFAULT_SIZE));
                fontCache.put(CAUDEX_BOLD, new Font(DEFAULT_FONT, Font.BOLD, (int)DEFAULT_SIZE));
                fontCache.put(CAUDEX_ITALIC, new Font(DEFAULT_FONT, Font.ITALIC, (int)DEFAULT_SIZE));
                fontCache.put(CAUDEX_BOLD_ITALIC, new Font(DEFAULT_FONT, Font.BOLD | Font.ITALIC, (int)DEFAULT_SIZE));
            } else {
                System.out.println("Caudex fonts loaded successfully: " + fontCache.keySet());
            }
        } catch (Exception e) {
            System.err.println("Error loading fonts: " + e.getMessage());
            e.printStackTrace();
            
            // Ensure we always have fonts in the cache even if loading fails
            fontCache.put(CAUDEX_REGULAR, new Font(DEFAULT_FONT, Font.PLAIN, (int)DEFAULT_SIZE));
            fontCache.put(CAUDEX_BOLD, new Font(DEFAULT_FONT, Font.BOLD, (int)DEFAULT_SIZE));
            fontCache.put(CAUDEX_ITALIC, new Font(DEFAULT_FONT, Font.ITALIC, (int)DEFAULT_SIZE));
            fontCache.put(CAUDEX_BOLD_ITALIC, new Font(DEFAULT_FONT, Font.BOLD | Font.ITALIC, (int)DEFAULT_SIZE));
        }
    }
    
    private static void registerFont(String name, String path) {
        try {
            System.out.println("Registering font: " + name + " from " + path);
            InputStream is = FontManager.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Font file not found: " + path);
                return;
            }
            
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            fontCache.put(name, font);
            System.out.println("Successfully registered font: " + name);
            is.close();
        } catch (Exception e) {
            System.err.println("Error registering font " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Font getter methods - always return Arial with appropriate style
    
    public static Font getRegular(float size) {
        return getFont(CAUDEX_REGULAR, Font.PLAIN, size);
    }
    
    public static Font getBold(float size) {
        return getFont(CAUDEX_BOLD, Font.BOLD, size);
    }
    
    public static Font getItalic(float size) {
        return getFont(CAUDEX_ITALIC, Font.ITALIC, size);
    }
    
    public static Font getBoldItalic(float size) {
        return getFont(CAUDEX_BOLD_ITALIC, Font.BOLD | Font.ITALIC, size);
    }
    
    public static Font getDefault() {
        return getRegular(DEFAULT_SIZE);
    }
    
    public static Font getTitle() {
        return getBold(TITLE_SIZE);
    }
    
    public static Font getSubtitle() {
        return getBold(SUBTITLE_SIZE);
    }
    
    private static Font getFont(String name, int style, float size) {
        Font baseFont = fontCache.get(name);
        if (baseFont == null) {
            System.err.println("Font not found in cache: " + name + ", using default font");
            return new Font(DEFAULT_FONT, style, (int)size);
        }
        return baseFont.deriveFont(style, size);
    }
    
    // Application methods
    
    public static void applyFontToComponent(Component component) {
        if (component == null) return;
        
        // Skip applying font to app title labels
        if (component instanceof JLabel) {
            JLabel label = (JLabel)component;
            String text = label.getText();
            if (text != null && text.contains("E-Commerce App")) {
                // Keep the app title using its current font but larger
                Font currentFont = label.getFont();
                label.setFont(currentFont.deriveFont(Font.BOLD, 32f));
                return;
            }
        }
        
        // Apply default font to the component
        component.setFont(getDefault());
        
        // Apply fonts to all child components
        if (component instanceof Container) {
            Component[] children = ((Container)component).getComponents();
            for (Component child : children) {
                if (child != null) {
                    try {
                        applyFontToComponent(child);
                    } catch (Exception e) {
                        System.err.println("Error applying font to component: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    public static void applyFontToAllComponents(Window window) {
        if (window == null) return;
        
        try {
            System.out.println("Applying Caudex fonts to all components in " + window.getClass().getSimpleName());
            for (Component component : window.getComponents()) {
                applyFontToComponent(component);
            }
            
            // Force full revalidation to update all components
            SwingUtilities.updateComponentTreeUI(window);
            window.revalidate();
            window.repaint();
            
            System.out.println("Finished applying fonts to all components in window");
        } catch (Exception e) {
            System.err.println("Error applying fonts to window: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 