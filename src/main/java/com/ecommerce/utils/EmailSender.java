package com.ecommerce.utils;

import com.ecommerce.config.AppConfig;

import javax.mail.Session;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import java.util.Properties;
import java.io.File;

public class EmailSender
{
    private String fromEmail;
    private String fromPassword;
    private boolean isConfigured = false;

    public EmailSender()
    {
        try {
            // Initialize AppConfig if needed
            AppConfig.initialize();
            
            // Get email credentials
            fromEmail = AppConfig.getEmailUsername();
            fromPassword = AppConfig.getEmailPassword();
            
            // Check if email is configured
            isConfigured = AppConfig.isEmailConfigured();
            
            if (isConfigured) {
                System.out.println("Email sender initialized with username: " + fromEmail);
                
                // For Gmail, verify App Password format (16 characters)
                if (fromEmail.toLowerCase().contains("gmail.com") && 
                    (fromPassword == null || fromPassword.length() != 16)) {
                    System.out.println("WARNING: Gmail detected but App Password format may be invalid.");
                    System.out.println("App Passwords should be 16 characters without spaces.");
                    System.out.println("Visit https://myaccount.google.com/apppasswords to generate a valid App Password.");
                }
                
                // Validate basic email configuration
                if (fromEmail == null || !fromEmail.contains("@")) {
                    System.out.println("WARNING: Email username doesn't appear to be a valid email address: " + fromEmail);
                }
            } else {
                System.out.println("Email credentials not properly configured. Email sending is disabled.");
                System.out.println("Please check application.properties and ensure mail.username and mail.password are set.");
            }
        } catch (Exception e) {
            System.err.println("Error initializing email sender: " + e.getMessage());
            e.printStackTrace();
            isConfigured = false;
        }
    }

    public boolean sendEmail(String toEmail, String subject, String body, String attachmentPath)
    {
        // Skip sending if not configured or recipient email is invalid
        if (!isConfigured) {
            System.out.println("Email not sent: Email functionality not configured");
            System.out.println("Username: " + (fromEmail == null ? "null" : (fromEmail.isEmpty() ? "empty" : fromEmail)));
            System.out.println("Password: " + (fromPassword == null ? "null" : (fromPassword.isEmpty() ? "empty" : "******")));
            return false;
        }
        
        if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
            System.out.println("Email not sent: Invalid recipient email address");
            return false;
        }

        String host = AppConfig.EMAIL_HOST;
        String port = AppConfig.EMAIL_PORT;
        
        System.out.println("Email configuration: Host=" + host + ", Port=" + port + ", From=" + fromEmail);

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.timeout", "15000"); // 15 second timeout
        properties.put("mail.smtp.connectiontimeout", "15000"); // 15 second connection timeout
        properties.put("mail.debug", "true"); // Enable debugging
        
        // Enable SSL if using port 465
        if ("465".equals(port)) {
            properties.put("mail.smtp.socketFactory.port", port);
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.ssl.enable", "true");
        }

        Session session = Session.getInstance(properties, new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(fromEmail, fromPassword);
            }
        });
        
        // Enable session debugging
        session.setDebug(true);

        // Try multiple times with increasing delay
        Exception lastException = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                System.out.println("Sending email attempt " + attempt + " to: " + toEmail);
                
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                message.setSubject(subject);

                Multipart multipart = new MimeMultipart();
                
                // Add text part
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(body);
                multipart.addBodyPart(messageBodyPart);

                // Add attachment if provided
                if (attachmentPath != null && !attachmentPath.isEmpty()) {
                    try {
                        System.out.println("Attaching file: " + attachmentPath);
                        File attachmentFile = new File(attachmentPath);
                        if (!attachmentFile.exists()) {
                            System.err.println("Attachment file does not exist: " + attachmentPath);
                            // Continue without attachment
                        } else {
                            messageBodyPart = new MimeBodyPart();
                            DataSource source = new FileDataSource(attachmentPath);
                            messageBodyPart.setDataHandler(new DataHandler(source));
                            messageBodyPart.setFileName("OrderReceipt.pdf");
                            multipart.addBodyPart(messageBodyPart);
                            System.out.println("File attached successfully: " + attachmentFile.length() + " bytes");
                        }
                    } catch (Exception e) {
                        System.err.println("Error attaching file to email: " + e.getMessage());
                        e.printStackTrace();
                        // Continue sending email without attachment
                    }
                }

                message.setContent(multipart);

                System.out.println("Sending email...");
                Transport.send(message);
                System.out.println("Email Sent Successfully to: " + toEmail);
                return true;
            } catch (MessagingException e) {
                lastException = e;
                System.err.println("Error Sending Email to " + toEmail + " (Attempt " + attempt + "): " + e.getMessage());
                e.printStackTrace();
                
                // Wait before retrying
                if (attempt < 3) {
                    try {
                        int delay = attempt * 2000; // Increase delay with each attempt
                        System.out.println("Waiting " + delay + "ms before retry...");
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                lastException = e;
                System.err.println("Unexpected error while sending email (Attempt " + attempt + "): " + e.getMessage());
                e.printStackTrace();
                
                // Wait before retrying
                if (attempt < 3) {
                    try {
                        int delay = attempt * 2000; // Increase delay with each attempt
                        System.out.println("Waiting " + delay + "ms before retry...");
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        if (lastException != null) {
            System.err.println("All email sending attempts failed: " + lastException.getMessage());
        }
        
        return false;
    }
    
    // Check if the email service is properly configured
    public boolean isEmailServiceConfigured() {
        return isConfigured;
    }
}