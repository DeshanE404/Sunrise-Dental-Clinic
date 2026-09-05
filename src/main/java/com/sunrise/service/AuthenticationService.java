package com.sunrise.service;

import com.sunrise.dao.UserDAO;
import com.sunrise.model.User;
import com.sunrise.util.PasswordUtil;
import java.util.logging.Logger;

public class AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());
    private final UserDAO userDAO = new UserDAO();

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }

        User user = userDAO.getUserByEmail(email.trim());
        if (user == null) {
            LOGGER.warning("Login failed: no user found for the submitted email address.");
            return null;
        }
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            LOGGER.warning("Login failed: password verification failed for the matching user.");
            return null;
        }

        LOGGER.info("Login succeeded for user id " + user.getId());
        if (user != null) {
            return user;
        }
        return null;
    }

    public boolean isAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
