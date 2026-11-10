
package com.banksphere.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    // Generate a secure BCrypt hash
    public static String hashPassword(String plainPassword) {

        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be null or empty."
            );
        }

        return BCrypt.hashpw(
                plainPassword,
                BCrypt.gensalt(WORK_FACTOR)
        );
    }

    // Verify a password against its stored hash
    public static boolean verifyPassword(
            String plainPassword,
            String hashedPassword) {

        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
