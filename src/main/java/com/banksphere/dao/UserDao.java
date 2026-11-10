
package com.banksphere.dao;

import com.banksphere.config.DatabaseConnection;
import com.banksphere.model.User;
import com.banksphere.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class UserDao {


    // Insert a new user using an internally managed connection
    public int saveUser(User user) throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection()) {
            return saveUser(connection, user);
        }
    }

    // Insert a new user using an existing database connection
    public int saveUser(Connection connection, User user)
            throws SQLException {

        String sql = "INSERT INTO users (username, password, role) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPasswordHash());
            preparedStatement.setString(3, user.getRole().name());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("User registration failed.");
            }

            try (ResultSet generatedKeys =
                         preparedStatement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    user.setUserId(userId);

                    return userId;
                } else {
                    throw new SQLException(
                            "User registration failed. No ID generated."
                    );
                }
            }
        }
    }


    // Find a user using username
    public Optional<User> findByUsername(String username)
            throws SQLException {

        String sql = "SELECT user_id, username, password, role, created_at " +
                "FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                if (resultSet.next()) {

                    User user = new User();

                    user.setUserId(resultSet.getInt("user_id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPasswordHash(resultSet.getString("password"));

                    user.setRole(
                            UserRole.valueOf(resultSet.getString("role"))
                    );

                    Timestamp timestamp =
                            resultSet.getTimestamp("created_at");

                    if (timestamp != null) {
                        user.setCreatedAt(timestamp.toLocalDateTime());
                    }

                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }

    // Check whether username already exists
    public boolean existsByUsername(String username)
            throws SQLException {

        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                return resultSet.next();
            }
        }
    }
}
