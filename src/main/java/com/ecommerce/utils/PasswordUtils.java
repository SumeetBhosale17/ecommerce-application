package com.ecommerce.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    private static final int WORKLOAD = 12;

    public static String hashPassword(String plainTextPassword) {
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(plainTextPassword, salt);
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
} 