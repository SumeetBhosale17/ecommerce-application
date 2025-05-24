package com.ecommerce.dao;

import java.sql.SQLException;
import java.util.List;

import com.ecommerce.model.User;

public interface UserDAO {
    User create(User user) throws SQLException;
    User findById(int id) throws SQLException;
    User findByUsername(String username) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    User update(User user) throws SQLException;
    boolean delete(int id) throws SQLException;
    User authenticate(String username, String password) throws SQLException;
    User getUserByUsername(String username);
    User getUserById(int userId);
    boolean deleteUser(int userId);
    boolean updateUser(User user) throws SQLException;
    boolean checkUsernameExists(String username) throws SQLException;
    List<User> getAdminUsers() throws SQLException;
}
