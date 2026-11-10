
package com.banksphere;

import com.banksphere.dao.CustomerDao;
import com.banksphere.dao.UserDao;
import com.banksphere.model.Customer;
import com.banksphere.service.AuthenticationService;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionRollbackTest {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();

        // Simulate a failure during customer profile insertion
        CustomerDao failingCustomerDao = new CustomerDao() {

            @Override
            public int saveCustomer(
                    Connection connection,
                    Customer customer) throws SQLException {

                throw new SQLException(
                        "Simulated customer insertion failure."
                );
            }
        };

        AuthenticationService authService =
                new AuthenticationService(
                        userDao,
                        failingCustomerDao
                );

        String username =
                "rollback_test_" + System.currentTimeMillis();

        String password = "BankSphere@123";

        try {

            System.out.println(
                    "TEST: Database Transaction Rollback"
            );

            try {

                authService.registerCustomer(
                        username,
                        password,
                        "Rollback Test Customer",
                        username + "@example.com",
                        "9876543210",
                        "Kolkata"
                );

                System.out.println(
                        "FAIL: Registration unexpectedly succeeded."
                );

            } catch (SQLException e) {

                System.out.println(
                        "Expected failure: " + e.getMessage()
                );
            }

            // Check whether the user record was rolled back
            boolean userExists =
                    userDao.existsByUsername(username);

            System.out.println(
                    "\nChecking database after rollback..."
            );

            if (!userExists) {

                System.out.println(
                        "PASS: User record was rolled back."
                );

                System.out.println(
                        "PASS: No incomplete registration remains."
                );

            } else {

                System.out.println(
                        "FAIL: User record still exists!"
                );
            }

        } catch (SQLException e) {

            System.out.println("Database error occurred:");
            e.printStackTrace();
        }
    }
}
