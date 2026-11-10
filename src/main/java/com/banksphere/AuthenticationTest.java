
package com.banksphere;

import com.banksphere.dao.CustomerDao;
import com.banksphere.dao.UserDao;
import com.banksphere.model.Customer;
import com.banksphere.model.User;
import com.banksphere.service.AuthenticationService;

import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationTest {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();
        CustomerDao customerDao = new CustomerDao();

        AuthenticationService authService =
                new AuthenticationService(userDao, customerDao);

        String username =
                "test_customer_" + System.currentTimeMillis();

        String password = "BankSphere@123";
        String email = username + "@example.com";

        try {

            // TEST 1: Complete customer registration
            System.out.println("TEST 1: Customer Registration");

            int userId = authService.registerCustomer(
                    username,
                    password,
                    "Arnab Test",
                    email,
                    "9876543210",
                    "Kolkata, West Bengal"
            );

            System.out.println("Registration successful!");
            System.out.println("Generated User ID: " + userId);

            // TEST 2: Verify customer profile creation
            System.out.println(
                    "\nTEST 2: Customer Profile Creation"
            );

            Optional<Customer> customerOptional =
                    customerDao.findByUserId(userId);

            if (customerOptional.isPresent()) {

                Customer customer = customerOptional.get();

                System.out.println(
                        "Customer profile created successfully!"
                );

                System.out.println(
                        "Customer ID: " + customer.getCustomerId()
                );

                System.out.println(
                        "Linked User ID: " + customer.getUserId()
                );

                System.out.println(
                        "Customer Name: " + customer.getFullName()
                );

            } else {

                System.out.println(
                        "ERROR: Customer profile was not created!"
                );
            }

            // TEST 3: Login with correct password
            System.out.println(
                    "\nTEST 3: Correct Password Login"
            );

            Optional<User> loginResult =
                    authService.authenticate(username, password);

            if (loginResult.isPresent()) {

                System.out.println("Login successful!");

                System.out.println(
                        "Welcome: " +
                                loginResult.get().getUsername()
                );

            } else {

                System.out.println("Login failed!");
            }

            // TEST 4: Login with incorrect password
            System.out.println(
                    "\nTEST 4: Incorrect Password Login"
            );

            Optional<User> wrongLogin =
                    authService.authenticate(
                            username,
                            "WrongPassword123"
                    );

            System.out.println(
                    wrongLogin.isEmpty()
                            ? "Correctly rejected wrong password."
                            : "ERROR: Wrong password was accepted!"
            );

            // TEST 5: Duplicate username
            System.out.println(
                    "\nTEST 5: Duplicate Username"
            );

            try {

                authService.registerCustomer(
                        username,
                        password,
                        "Another Customer",
                        "another_" + username + "@example.com",
                        "9876543211",
                        "Kolkata"
                );

                System.out.println(
                        "ERROR: Duplicate username accepted!"
                );

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "Correctly rejected duplicate username."
                );
            }

            // TEST 6: Duplicate email
            System.out.println(
                    "\nTEST 6: Duplicate Email"
            );

            try {

                authService.registerCustomer(
                        "another_" + username,
                        password,
                        "Another Customer",
                        email,
                        "9876543212",
                        "Howrah"
                );

                System.out.println(
                        "ERROR: Duplicate email accepted!"
                );

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "Correctly rejected duplicate email."
                );
            }

        } catch (SQLException e) {

            System.out.println("Database error occurred:");
            e.printStackTrace();

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "Validation error: " + e.getMessage()
            );
        }
    }
}
