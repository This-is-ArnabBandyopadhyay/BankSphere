
package com.banksphere.dao;

import com.banksphere.config.DatabaseConnection;
import com.banksphere.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDao {

    // Insert a customer using an existing database connection
    public int saveCustomer(Connection connection, Customer customer)
            throws SQLException {

        String sql = "INSERT INTO customers " +
                "(user_id, full_name, email, phone, address) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, customer.getUserId());
            preparedStatement.setString(2, customer.getFullName());
            preparedStatement.setString(3, customer.getEmail());
            preparedStatement.setString(4, customer.getPhone());
            preparedStatement.setString(5, customer.getAddress());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Customer registration failed.");
            }

            try (ResultSet generatedKeys =
                         preparedStatement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    int customerId = generatedKeys.getInt(1);
                    customer.setCustomerId(customerId);

                    return customerId;
                } else {
                    throw new SQLException(
                            "Customer registration failed. No ID generated."
                    );
                }
            }
        }
    }

    // Find customer using customer ID
    public Optional<Customer> findByCustomerId(int customerId)
            throws SQLException {

        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, customerId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapCustomer(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    // Find customer using user ID
    public Optional<Customer> findByUserId(int userId)
            throws SQLException {

        String sql = "SELECT * FROM customers WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapCustomer(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    // Retrieve all customers
    public List<Customer> findAll() throws SQLException {

        String sql = "SELECT * FROM customers ORDER BY customer_id";

        List<Customer> customers = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                customers.add(mapCustomer(resultSet));
            }
        }

        return customers;
    }

    // Update customer profile
    public boolean updateCustomer(Customer customer)
            throws SQLException {

        String sql = "UPDATE customers SET " +
                "full_name = ?, email = ?, phone = ?, address = ? " +
                "WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(1, customer.getFullName());
            preparedStatement.setString(2, customer.getEmail());
            preparedStatement.setString(3, customer.getPhone());
            preparedStatement.setString(4, customer.getAddress());
            preparedStatement.setInt(5, customer.getCustomerId());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    // Check whether email already exists
    public boolean existsByEmail(String email)
            throws SQLException {

        String sql = "SELECT 1 FROM customers WHERE email = ? LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }


    // Check whether an email belongs to another customer
    public boolean existsByEmailForOtherCustomer(
            String email, int customerId) throws SQLException {

        String sql = "SELECT 1 FROM customers " +
                "WHERE email = ? AND customer_id <> ? LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            preparedStatement.setInt(2, customerId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    // Map database records to Customer objects
    private Customer mapCustomer(ResultSet resultSet)
            throws SQLException {

        Customer customer = new Customer();

        customer.setCustomerId(resultSet.getInt("customer_id"));
        customer.setUserId(resultSet.getInt("user_id"));
        customer.setFullName(resultSet.getString("full_name"));
        customer.setEmail(resultSet.getString("email"));
        customer.setPhone(resultSet.getString("phone"));
        customer.setAddress(resultSet.getString("address"));

        return customer;
    }
}
