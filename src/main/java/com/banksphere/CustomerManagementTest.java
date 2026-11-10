
package com.banksphere;

import com.banksphere.dao.CustomerDao;
import com.banksphere.dao.UserDao;
import com.banksphere.model.Customer;
import com.banksphere.service.AuthenticationService;
import com.banksphere.service.CustomerService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CustomerManagementTest {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();
        CustomerDao customerDao = new CustomerDao();

        AuthenticationService authService =
                new AuthenticationService(userDao, customerDao);

        CustomerService customerService =
                new CustomerService(customerDao);

        String uniqueId = String.valueOf(System.currentTimeMillis());

        try {

            // Create two customers for testing
            int firstUserId = authService.registerCustomer(
                    "profile_test_" + uniqueId,
                    "BankSphere@123",
                    "First Test Customer",
                    "first_" + uniqueId + "@example.com",
                    "9876543210",
                    "Kolkata"
            );

            int secondUserId = authService.registerCustomer(
                    "profile_test_second_" + uniqueId,
                    "BankSphere@123",
                    "Second Test Customer",
                    "second_" + uniqueId + "@example.com",
                    "9876543211",
                    "Howrah"
            );

            // TEST 1: Retrieve customer by User ID
            System.out.println("TEST 1: Retrieve Customer by User ID");

            Optional<Customer> customerByUserId =
                    customerService.getCustomerByUserId(firstUserId);

            if (customerByUserId.isPresent()) {
                System.out.println("PASS: Customer retrieved by User ID.");
            } else {
                System.out.println("FAIL: Customer not found.");
            }

            // TEST 2: Retrieve customer by Customer ID
            System.out.println("\nTEST 2: Retrieve Customer by Customer ID");

            Customer firstCustomer = customerByUserId.orElseThrow(
                    () -> new IllegalStateException(
                            "First customer was not found."
                    )
            );

            Optional<Customer> customerById =
                    customerService.getCustomerById(
                            firstCustomer.getCustomerId()
                    );

            if (customerById.isPresent()) {
                System.out.println("PASS: Customer retrieved by Customer ID.");
            } else {
                System.out.println("FAIL: Customer not found.");
            }

            // TEST 3: Retrieve all customers
            System.out.println("\nTEST 3: Retrieve All Customers");

            List<Customer> customers =
                    customerService.getAllCustomers();

            System.out.println(
                    "PASS: Retrieved " + customers.size() + " customers."
            );

            // TEST 4: Update customer profile
            System.out.println("\nTEST 4: Update Customer Profile");

            firstCustomer.setFullName("Updated Test Customer");
            firstCustomer.setEmail("updated_" + uniqueId + "@example.com");
            firstCustomer.setPhone("9123456780");
            firstCustomer.setAddress("New Town, Kolkata");

            customerService.updateCustomerProfile(firstCustomer);

            Optional<Customer> updatedCustomer =
                    customerService.getCustomerById(
                            firstCustomer.getCustomerId()
                    );

            if (updatedCustomer.isPresent()
                    && updatedCustomer.get().getFullName()
                    .equals("Updated Test Customer")) {

                System.out.println("PASS: Customer profile updated.");

            } else {
                System.out.println("FAIL: Profile update unsuccessful.");
            }

            // TEST 5: Reject duplicate email
            System.out.println("\nTEST 5: Reject Duplicate Email");

            Customer secondCustomer =
                    customerService.getCustomerByUserId(secondUserId)
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "Second customer was not found."
                                    )
                            );

            secondCustomer.setEmail(
                    updatedCustomer.get().getEmail()
            );

            try {

                customerService.updateCustomerProfile(secondCustomer);

                System.out.println("FAIL: Duplicate email was accepted.");

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "PASS: Duplicate email correctly rejected."
                );
            }

            // TEST 6: Reject invalid phone number
            System.out.println("\nTEST 6: Reject Invalid Phone Number");

            secondCustomer.setEmail(
                    "second_updated_" + uniqueId + "@example.com"
            );

            secondCustomer.setPhone("ABC123");

            try {

                customerService.updateCustomerProfile(secondCustomer);

                System.out.println("FAIL: Invalid phone was accepted.");

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "PASS: Invalid phone correctly rejected."
                );
            }

            // TEST 7: Reject invalid customer ID
            System.out.println("\nTEST 7: Reject Invalid Customer ID");

            try {

                customerService.getCustomerById(0);

                System.out.println("FAIL: Invalid ID was accepted.");

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "PASS: Invalid customer ID correctly rejected."
                );
            }

        } catch (SQLException e) {

            System.out.println("Database error occurred:");
            e.printStackTrace();

        } catch (IllegalArgumentException e) {

            System.out.println("Validation error: " + e.getMessage());

        } catch (IllegalStateException e) {

            System.out.println("Test setup error: " + e.getMessage());
        }
    }
}
