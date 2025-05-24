package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address
{
    private int id;
    private int userId;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String zip;

    public Address(int userId, String street, String city, String state, String pincode)
    {
        this.userId = userId;
        this.street = street;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
    }
    
    // For backwards compatibility
    public int getUser_id() {
        return userId;
    }
    
    public void setUser_id(int user_id) {
        this.userId = user_id;
    }
    
    public String getAddress() {
        return street;
    }
    
    public void setAddress(String address) {
        this.street = address;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
