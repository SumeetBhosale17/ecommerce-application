package com.ecommerce.dao;

import com.ecommerce.model.Address;

import java.util.List;

public interface AddressDAO
{
    boolean addAddress(Address address);
    boolean removeAddress(int addressId);
    boolean updateAddress(Address address);
    Address getAddressById(int addressId);
    Address getAddressByUserId(int userId);
    
    // New methods
    List<Address> getAddressesByUserId(int userId);
}
