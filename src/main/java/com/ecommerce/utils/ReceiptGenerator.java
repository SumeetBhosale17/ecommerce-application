package com.ecommerce.utils;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Transaction;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReceiptGenerator
{
    public static String generateReceipt(Order order, List<OrderItem> orderItems, Transaction transaction, String username, String city, String state, String pincode) throws IOException
    {
        // Create OrderReceipts directory if it doesn't exist
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDir);
        
        File directory = new File(currentDir, "OrderReceipts");
        System.out.println("Creating receipt directory at: " + directory.getAbsolutePath());
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created && !directory.exists()) {
                System.out.println("Failed to create directory: " + directory.getAbsolutePath());
                // Try to use a different location as fallback
                directory = new File(System.getProperty("java.io.tmpdir"), "OrderReceipts");
                System.out.println("Trying fallback directory: " + directory.getAbsolutePath());
                directory.mkdirs();
            } else {
                System.out.println("Directory created successfully");
            }
        } else {
            System.out.println("Directory already exists");
        }
        
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDRectangle mediaBox = page.getMediaBox();
        float pageWidth = mediaBox.getWidth();

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page))
        {
            int margin = 50;
            int yPosition = 750;

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Order Receipt");
            contentStream.endText();

            yPosition -= 40;
            contentStream.setFont(PDType1Font.HELVETICA, 12);

            // Top Order and Transaction Info
            writeKeyValue(contentStream, "Order ID:", String.valueOf(order.getId()), margin, yPosition);
            writeKeyValue(contentStream, "Order Date:", String.valueOf(order.getOrderDate()), margin + 250, yPosition);

            yPosition -= 20;
            writeKeyValue(contentStream, "Transaction ID:", String.valueOf(transaction.getId()), margin, yPosition);
            
            // Safely handle possibly null delivery estimate
            String deliveryEstimate = "Not available";
            if (order.getDeliveryEstimate() != null) {
                deliveryEstimate = String.valueOf(order.getDeliveryEstimate());
            }
            writeKeyValue(contentStream, "Estimated Delivery:", deliveryEstimate, margin + 250, yPosition);

            yPosition -= 20;
            writeKeyValue(contentStream, "Transaction Method:", String.valueOf(transaction.getMethod()), margin, yPosition);
            writeKeyValue(contentStream, "Transaction Status:", String.valueOf(transaction.getStatus()), margin + 250, yPosition);

            yPosition -= 20;
            drawHorizontalLine(contentStream, margin, (int)(pageWidth - margin), yPosition);

            yPosition -= 30;
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Bill To / Ship To:");
            contentStream.endText();

            yPosition -= 20;
            writeSimpleText(contentStream, username, margin + 20, yPosition);

            yPosition -= 20;
            writeSimpleText(contentStream, city + ", " + state, margin + 20, yPosition);

            yPosition -= 20;
            writeSimpleText(contentStream, pincode, margin + 20, yPosition);

            yPosition -= 20;
            drawHorizontalLine(contentStream, margin, (int)(pageWidth - margin), yPosition);

            yPosition -= 30;
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            writeTableRow(contentStream, yPosition, "Product", "Price", "Qty", "Discount", "Amount");

            yPosition -= 10;
            drawHorizontalLine(contentStream, margin, (int)(pageWidth - margin), yPosition);

            yPosition -= 20;
            contentStream.setFont(PDType1Font.HELVETICA, 12);

            double totalPrice = 0;
            double totalDiscount = 0;
            double grandTotal = 0;

            for (OrderItem item : orderItems)
            {
                double itemPrice = item.getPrice();
                // Calculate item discount based on order total vs original price
                double itemOriginalTotal = itemPrice * item.getQuantity();
                double itemFinalTotal = (order.getTotalAmount() / calculateOriginalTotal(orderItems)) * itemOriginalTotal;
                double discount = itemOriginalTotal - itemFinalTotal;
                
                double amount = itemFinalTotal;

                writeTableRow(contentStream, yPosition, item.getProductName(), 
                              String.format("Rs. %.2f", itemPrice), 
                              String.valueOf(item.getQuantity()), 
                              String.format("Rs. %.2f", discount), 
                              String.format("Rs. %.2f", amount));

                totalPrice += itemPrice * item.getQuantity();
                totalDiscount += discount;
                grandTotal += amount;

                yPosition -= 20;
            }

            yPosition -= 10;
            drawHorizontalLine(contentStream, margin, (int)(pageWidth - margin), yPosition);

            // Show applied sale discount if it exists
            if (totalDiscount > 0) {
                double discountPercent = (totalDiscount / totalPrice) * 100;
                yPosition -= 20;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(String.format("Applied Sale Discount: %.1f%%", discountPercent));
                contentStream.endText();
            }

            yPosition -= 20;
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            writeTableRow(contentStream, yPosition, "Total", 
                          String.format("Rs. %.2f", totalPrice), 
                          "", 
                          String.format("Rs. %.2f", totalDiscount), 
                          String.format("Rs. %.2f", grandTotal));

            yPosition -= 40;
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("ZOROJURO DELIVERY EXPRESS LTD");
            contentStream.endText();
        }

        // Verify receipt file exists
        String filename = "OrderReceipt_" + order.getId() + "_" + System.currentTimeMillis() + ".pdf";
        File file = new File(directory, filename);
        String filePath = file.getAbsolutePath();
        System.out.println("Saving receipt to: " + filePath);

        try {
            document.save(file);
            document.close();
            
            System.out.println("✅ Receipt Saved at: " + file.getAbsolutePath());
            if (file.exists() && file.length() > 0) {
                System.out.println("Receipt file verified: Size=" + file.length() + " bytes");
                return file.getAbsolutePath();
            } else {
                System.out.println("WARNING: File may not have been properly saved");
                return filePath; // Return path anyway in case file just hasn't been flushed
            }
        } catch (Exception e) {
            System.out.println("Error saving PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Helper method to calculate original total before discount
    private static double calculateOriginalTotal(List<OrderItem> orderItems) {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    private static void writeKeyValue(PDPageContentStream contentStream, String key, String value, int x, int y) throws IOException
    {
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(key + " " + value);
        contentStream.endText();
    }

    private static void writeSimpleText(PDPageContentStream contentStream, String text, int x, int y) throws IOException
    {
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private static void drawHorizontalLine(PDPageContentStream contentStream, int startX, int endX, int y) throws IOException
    {
        contentStream.moveTo(startX, y);
        contentStream.lineTo(endX, y);
        contentStream.stroke();
    }

    private static void writeTableRow(PDPageContentStream contentStream, int y, String col1, String col2, String col3, String col4, String col5) throws IOException
    {
        int startX = 50;
        int[] colWidths = {120, 80, 40, 80, 80};

        contentStream.beginText();
        contentStream.newLineAtOffset(startX, y);
        contentStream.showText(col1);
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(startX + colWidths[0], y);
        contentStream.showText(col2);
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1], y);
        contentStream.showText(col3);
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1] + colWidths[2], y);
        contentStream.showText(col4);
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(startX + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3], y);
        contentStream.showText(col5);
        contentStream.endText();
    }
}
