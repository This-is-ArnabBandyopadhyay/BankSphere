
package com.banksphere;

import com.banksphere.dao.UserDao;
import com.banksphere.service.AuthenticationService;
import com.banksphere.session.SessionManager;

import java.sql.SQLException;

public class AuthenticationEdgeCaseTest {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();

        AuthenticationService authService =
                new AuthenticationService(userDao);

        SessionManager sessionManager =
                new SessionManager(authService);

        String username = "test_customer_1794316272548";
        String correctPassword = "BankSphere@123";

        try {

            // TEST 1: Incorrect password
            System.out.println("TEST 1: Incorrect Password");

            boolean wrongPasswordLogin =
                    sessionManager.login(
                            username, "WrongPassword123"
                    );

            System.out.println(
                    "Login successful: " + wrongPasswordLogin
            );

            System.out.println(
                    "Session created: " +
                            sessionManager.isLoggedIn()
            );

            // TEST 2: Non-existent username
            System.out.println("\nTEST 2: Non-existent Username");

            boolean unknownUserLogin =
                    sessionManager.login(
                            "unknown_customer_999",
                            correctPassword
                    );

            System.out.println(
                    "Login successful: " + unknownUserLogin
            );

            System.out.println(
                    "Session created: " +
                            sessionManager.isLoggedIn()
            );

            // TEST 3: Successful login
            System.out.println("\nTEST 3: Successful Login");

            boolean successfulLogin =
                    sessionManager.login(
                            username, correctPassword
                    );

            System.out.println(
                    "Login successful: " + successfulLogin
            );

            // TEST 4: Failed login while already logged in
            System.out.println(
                    "\nTEST 4: Failed Login During Active Session"
            );

            boolean anotherLogin =
                    sessionManager.login(
                            "unknown_customer_999",
                            correctPassword
                    );

            System.out.println(
                    "Another login allowed: " + anotherLogin
            );

            System.out.println(
                    "Original session still active: " +
                            sessionManager.isLoggedIn()
            );

            // TEST 5: Logout and login again
            System.out.println("\nTEST 5: Login After Logout");

            sessionManager.logout();

            boolean loginAfterLogout =
                    sessionManager.login(
                            username, correctPassword
                    );

            System.out.println(
                    "Login after logout successful: " +
                            loginAfterLogout
            );

            System.out.println(
                    "Session active: " +
                            sessionManager.isLoggedIn()
            );

            // Final cleanup
            sessionManager.logout();

            System.out.println("\nFinal session state: " +
                    sessionManager.isLoggedIn());

        } catch (SQLException e) {
            System.out.println("Database error:");
            e.printStackTrace();
        }
    }
}
