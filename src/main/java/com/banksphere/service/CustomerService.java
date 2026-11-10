
package com.banksphere.service;

import com.banksphere.dao.CustomerDao;
import com.banksphere.model.Customer;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        if (customerDao == null) {
            throw new IllegalArgumentException(
                    "CustomerDao cannot be null."
            );
        }

        this.customerDao = customerDao;
    }

    // Retrieve customer using customer ID
    public Optional<Customer> getCustomerById(int customerId)
            throws SQLException {

        validateId(customerId);

        return customerDao.findByCustomerId(customerId);
    }

    // Retrieve customer using user ID
    public Optional<Customer> getCustomerByUserId(int userId)
            throws SQLException {

        validateId(userId);

        return customerDao.findByUserId(userId);
    }

    // Retrieve all customers
    public List<Customer> getAllCustomers() throws SQLException {

        return customerDao.findAll();
    }

    // Update customer profile
    public void updateCustomerProfile(Customer customer)
            throws SQLException {

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer details cannot be null."
            );
        }

        validateId(customer.getCustomerId());

        // Validate and normalize full name
        String fullName = validateFullName(customer.getFullName());

        // Validate and normalize optional fields
        String email = normalizeOptional(customer.getEmail());
        String phone = normalizeOptional(customer.getPhone());
        String address = normalizeOptional(customer.getAddress());

        validateEmail(email);
        validatePhone(phone);
        validateAddress(address);

        // Confirm that the customer exists
        Optional<Customer> existingCustomer =
                customerDao.findByCustomerId(customer.getCustomerId());

        if (existingCustomer.isEmpty()) {
            throw new IllegalArgumentException(
                    "Customer does not exist."
            );
        }

        // Check whether another customer already uses this email
        if (email != null &&
                customerDao.existsByEmailForOtherCustomer(
                        email, customer.getCustomerId())) {

            throw new IllegalArgumentException(
                    "Email address is already registered."
            );
        }

        // Update normalized values
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);

        boolean updated = customerDao.updateCustomer(customer);

        if (!updated) {
            throw new SQLException(
                    "Customer profile could not be updated."
            );
        }
    }

    // Validate customer/user ID
    private void validateId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID must be greater than zero."
            );
        }
    }

    // Validate full name
    private String validateFullName(String fullName) {

        if (fullName == null || fullName.trim().isEmpty()) {
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

        return fullName;
    }

    // Normalize optional fields
    private String normalizeOptional(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    // Validate email
    private void validateEmail(String email) {

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

    // Validate phone
    private void validatePhone(String phone) {

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

    // Validate address
    private void validateAddress(String address) {

        if (address != null && address.length() > 255) {
            throw new IllegalArgumentException(
                    "Address cannot exceed 255 characters."
            );
        }
    }
}
