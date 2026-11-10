
package com.banksphere;

import com.banksphere.dao.UserDao;
import com.banksphere.model.User;
import com.banksphere.service.AuthenticationService;

import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationTest {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();
        AuthenticationService authService =
                new AuthenticationService(userDao);

        // Generate a unique username for every test run
        String username = "test_customer_" + System.currentTimeMillis();
        String password = "BankSphere@123";

        try {
            // TEST 1: Register a customer
            System.out.println("TEST 1: Customer Registration");

            int userId = authService.registerCustomer(
                    username, password
            );

            System.out.println("Registration successful!");
            System.out.println("Generated User ID: " + userId);

            // TEST 2: Login with correct password
            System.out.println("\nTEST 2: Correct Password Login");

            Optional<User> loginResult =
                    authService.authenticate(username, password);

            if (loginResult.isPresent()) {
                System.out.println("Login successful!");
                System.out.println("Welcome: " +
                        loginResult.get().getUsername());
            } else {
                System.out.println("Login failed!");
            }

            // TEST 3: Login with incorrect password
            System.out.println("\nTEST 3: Incorrect Password Login");

            Optional<User> wrongLogin =
                    authService.authenticate(
                            username, "WrongPassword123"
                    );

            System.out.println(
                    wrongLogin.isEmpty()
                            ? "Correctly rejected wrong password."
                            : "ERROR: Wrong password was accepted!"
            );

            // TEST 4: Duplicate username
            System.out.println("\nTEST 4: Duplicate Username");

            try {
                authService.registerCustomer(username, password);
                System.out.println("ERROR: Duplicate username accepted!");

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Correctly rejected duplicate username."
                );
            }

        } catch (SQLException e) {
            System.out.println("Database error occurred:");
            e.printStackTrace();

        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }
}
