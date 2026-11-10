
package com.banksphere;

import com.banksphere.dao.UserDao;
import com.banksphere.model.User;
import com.banksphere.model.UserRole;
import com.banksphere.service.AuthenticationService;
import com.banksphere.session.SessionManager;

import java.sql.SQLException;
import java.util.Optional;

public class SessionTest {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();

        AuthenticationService authService =
                new AuthenticationService(userDao);

        SessionManager sessionManager =
                new SessionManager(authService);

        String username = "test_customer_1794316272548";
        String password = "BankSphere@123";

        try {

            // TEST 1: Check initial session
            System.out.println("TEST 1: Initial Session");

            System.out.println(
                    "Logged in: " + sessionManager.isLoggedIn()
            );

            // TEST 2: Login
            System.out.println("\nTEST 2: Login");

            boolean loginSuccess =
                    sessionManager.login(username, password);

            System.out.println("Login successful: " + loginSuccess);

            // TEST 3: Get current user
            System.out.println("\nTEST 3: Current User");

            Optional<User> currentUser =
                    sessionManager.getCurrentUser();

            if (currentUser.isPresent()) {
                System.out.println(
                        "Username: " +
                                currentUser.get().getUsername()
                );

                System.out.println(
                        "Role: " +
                                currentUser.get().getRole()
                );
            }

            // TEST 4: Role verification
            System.out.println("\nTEST 4: Role Verification");

            System.out.println(
                    "Is CUSTOMER: " +
                            sessionManager.hasRole(UserRole.CUSTOMER)
            );

            System.out.println(
                    "Is ADMIN: " +
                            sessionManager.hasRole(UserRole.ADMIN)
            );

            // TEST 5: Prevent second login
            System.out.println("\nTEST 5: Second Login");

            boolean secondLogin =
                    sessionManager.login(
                            username, password
                    );

            System.out.println(
                    "Second login allowed: " + secondLogin
            );

            // TEST 6: Logout
            System.out.println("\nTEST 6: Logout");

            sessionManager.logout();

            System.out.println(
                    "Logged in after logout: " +
                            sessionManager.isLoggedIn()
            );

            System.out.println(
                    "Current user exists: " +
                            sessionManager.getCurrentUser().isPresent()
            );

        } catch (SQLException e) {
            System.out.println("Database error:");
            e.printStackTrace();
        }
    }
}
