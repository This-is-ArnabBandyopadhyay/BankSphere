
package com.banksphere.service;

import com.banksphere.config.DatabaseConnection;
import com.banksphere.dao.CustomerDao;
import com.banksphere.model.Customer;

import java.sql.Connection;

import com.banksphere.dao.UserDao;
import com.banksphere.model.User;
import com.banksphere.model.UserRole;
import com.banksphere.security.PasswordUtil;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationService {

    private final UserDao userDao;
    private final CustomerDao customerDao;

    // Constructor injection
    public AuthenticationService(UserDao userDao) {
        this(userDao, new CustomerDao());
    }

    // Constructor injection with both DAOs
    public AuthenticationService(
            UserDao userDao, CustomerDao customerDao) {

        if (userDao == null || customerDao == null) {
            throw new IllegalArgumentException(
                    "DAO dependencies cannot be null."
            );
        }

        this.userDao = userDao;
        this.customerDao = customerDao;
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


    // Register a customer with login credentials and profile details
    public int registerCustomer(
            String username,
            String password,
            String fullName,
            String email,
            String phone,
            String address) throws SQLException {

        // Validate login details
        validateUsername(username);
        validatePassword(password);

        username = username.trim();

        // Validate customer name
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Full name cannot be empty."
            );
        }

        fullName = fullName.trim();

        if (fullName.length() > 100) {
            throw new IllegalArgumentException(
                    "Full name cannot exceed 100 characters."
            );
        }

        // Normalize optional customer details
        email = normalizeOptional(email);
        phone = normalizeOptional(phone);
        address = normalizeOptional(address);

        // Validate optional details
        validateCustomerEmail(email);
        validateCustomerPhone(phone);

        if (address != null && address.length() > 255) {
            throw new IllegalArgumentException(
                    "Address cannot exceed 255 characters."
            );
        }

        // Check duplicate username
        if (userDao.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists."
            );
        }

        // Check duplicate email
        if (email != null && customerDao.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email address is already registered."
            );
        }

        // Hash password
        String passwordHash = PasswordUtil.hashPassword(password);

        // Create user object
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.CUSTOMER);

        // Create customer object
        Customer customer = new Customer();
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);

        // Begin database transaction
        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Insert user and retrieve generated user ID
                int userId = userDao.saveUser(connection, user);

                // Link customer profile to the generated user ID
                customer.setUserId(userId);

                // Insert customer profile using the same connection
                customerDao.saveCustomer(connection, customer);

                // Commit both inserts
                connection.commit();

                return userId;

            } catch (SQLException | RuntimeException e) {

                // Roll back both inserts if anything fails
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }
        }
    }

    // Normalize optional input
    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    // Validate customer email
    private void validateCustomerEmail(String email) {

        if (email == null) {
            return;
        }

        if (email.length() > 100 ||
                !email.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }
    }

    // Validate customer phone
    private void validateCustomerPhone(String phone) {

        if (phone == null) {
            return;
        }

        if (phone.length() > 15 ||
                !phone.matches("^\\+?[0-9]{7,15}$")) {

            throw new IllegalArgumentException(
                    "Phone number must contain 7-15 digits, " +
                            "with an optional leading +."
            );
        }
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
