
package com.banksphere.service;

import com.banksphere.dao.UserDao;
import com.banksphere.model.User;
import com.banksphere.model.UserRole;
import com.banksphere.security.PasswordUtil;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationService {

    private final UserDao userDao;

    // Constructor injection
    public AuthenticationService(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException(
                    "UserDao cannot be null."
            );
        }

        this.userDao = userDao;
    }

    // Register a new customer
    public int registerCustomer(String username, String password)
            throws SQLException {

        validateUsername(username);
        validatePassword(password);

        username = username.trim();

        // Check whether username already exists
        if (userDao.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists."
            );
        }

        // Hash the original password
        String passwordHash = PasswordUtil.hashPassword(password);

        // Create a new customer
        User user = new User();

        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.CUSTOMER);

        // Save user and return generated ID
        return userDao.saveUser(user);
    }

    // Authenticate an existing user
    public Optional<User> authenticate(
            String username, String password) throws SQLException {

        validateUsername(username);

        if (password == null || password.isEmpty()) {
            return Optional.empty();
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return Optional.empty();
        }

        username = username.trim();

        // Find user by username
        Optional<User> userOptional =
                userDao.findByUsername(username);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        // Verify entered password against stored hash
        boolean passwordMatches =
                PasswordUtil.verifyPassword(
                        password,
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            return Optional.empty();
        }

        // Do not expose password hash to the caller
        user.setPasswordHash(null);

        return Optional.of(user);
    }

    // Validate username
    private void validateUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username cannot be empty."
            );
        }

        String trimmedUsername = username.trim();

        if (trimmedUsername.length() > 50) {
            throw new IllegalArgumentException(
                    "Username cannot exceed 50 characters."
            );
        }
    }

    // Validate password during registration
    private void validatePassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        int passwordLength =
                password.getBytes(StandardCharsets.UTF_8).length;

        if (passwordLength < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 bytes."
            );
        }

        if (passwordLength > 72) {
            throw new IllegalArgumentException(
                    "Password cannot exceed 72 UTF-8 bytes."
            );
        }
    }
}
