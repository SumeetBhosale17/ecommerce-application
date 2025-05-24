package com.ecommerce.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Sale
{
    private int id;
    private String name;
    private double discountPercent;
    private Date startDate;
    private Date endDate;
    private boolean isActive;
    private String status; // Added status field for SCHEDULED, ACTIVE, COMPLETED

    // Default constructor
    public Sale() {
        this.status = "SCHEDULED"; // Default status
    }
    
    // Full constructor
    public Sale(int id, String name, double discountPercent, Date startDate, Date endDate, boolean isActive, String status) {
        this.id = id;
        this.name = name;
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.status = status != null ? status : "SCHEDULED";
    }

    public Sale(String name, double discountPercent, Date startDate, Date endDate)
    {
        this.discountPercent = discountPercent;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "SCHEDULED"; // Default status
    }
    
    // For backward compatibility with timestamp-based code
    public Sale(String name, double discountPercent, Timestamp startDate, Timestamp endDate)
    {
        this.name = name;
        this.discountPercent = discountPercent;
        if (startDate != null) {
            this.startDate = new Date(startDate.getTime());
        }
        if (endDate != null) {
            this.endDate = new Date(endDate.getTime());
        }
        this.status = "SCHEDULED"; // Default status
    }
    
    // Standard getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getDiscountPercent() {
        return discountPercent;
    }
    
    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }
    
    // SQL Date getters and setters
    public Date getSqlStartDate() {
        return startDate;
    }
    
    public Date getSqlEndDate() {
        return endDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    // For compatibility with older code
    public void setStartDate(Timestamp startDate) {
        if (startDate != null) {
            this.startDate = new Date(startDate.getTime());
        }
    }
    
    public void setEndDate(Timestamp endDate) {
        if (endDate != null) {
            this.endDate = new Date(endDate.getTime());
        }
    }
    
    // Utility methods
    public void setStartDateFromLocalDate(LocalDate date) {
        if (date != null) {
            this.startDate = Date.valueOf(date);
        }
    }
    
    public void setEndDateFromLocalDate(LocalDate date) {
        if (date != null) {
            this.endDate = Date.valueOf(date);
        }
    }
    
    // Getters for Timestamp format (for backward compatibility)
    public Timestamp getStartDateAsTimestamp() {
        return (startDate != null) ? new Timestamp(startDate.getTime()) : null;
    }
    
    public Timestamp getEndDateAsTimestamp() {
        return (endDate != null) ? new Timestamp(endDate.getTime()) : null;
    }
    
    // Methods for LocalDateTime conversion
    public LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toLocalDate().atStartOfDay();
    }
    
    // For SaleStatusScheduler compatibility
    public LocalDateTime getStartDate() {
        if (startDate == null) return null;
        return toLocalDateTime(startDate);
    }
    
    public LocalDateTime getEndDate() {
        if (endDate == null) return null;
        return toLocalDateTime(endDate);
    }
    
    // isActive getter and setter
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    // Getter and setter for status
    public String getStatus() {
        return status != null ? status : (isActive ? "ACTIVE" : "COMPLETED");
    }
    
    public void setStatus(String status) {
        this.status = status;
        // Update isActive based on status for backward compatibility
        this.isActive = "ACTIVE".equals(status);
    }
}
