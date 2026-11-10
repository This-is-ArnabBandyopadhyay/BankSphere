
package com.banksphere.session;

import com.banksphere.model.User;
import com.banksphere.model.UserRole;
import com.banksphere.service.AuthenticationService;

import java.sql.SQLException;
import java.util.Optional;

public class SessionManager {

    private final AuthenticationService authenticationService;

    // Stores the currently logged-in user
    private User currentUser;

    // Constructor injection
    public SessionManager(
            AuthenticationService authenticationService) {

        if (authenticationService == null) {
            throw new IllegalArgumentException(
                    "AuthenticationService cannot be null."
            );
        }

        this.authenticationService = authenticationService;
    }

    // Login
    public boolean login(String username, String password)
            throws SQLException {

        // Prevent another login while a user is already logged in
        if (currentUser != null) {
            return false;
        }

        Optional<User> userOptional =
                authenticationService.authenticate(
                        username, password
                );

        if (userOptional.isPresent()) {
            currentUser = userOptional.get();
            return true;
        }

        return false;
    }

    // Logout
    public void logout() {
        currentUser = null;
    }

    // Check whether a user is logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Get currently logged-in user
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    // Check whether current user has a particular role
    public boolean hasRole(UserRole role) {

        if (currentUser == null || role == null) {
            return false;
        }

        return currentUser.getRole() == role;
    }
}
