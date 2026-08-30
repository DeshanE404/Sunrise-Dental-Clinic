package com.sunrise.service;

import com.sunrise.dao.UserDAO;
import com.sunrise.model.User;
import com.sunrise.util.PasswordUtil;

public class AuthenticationService {
    private UserDAO userDAO = new UserDAO();

    public User login(String email, String password) {
        User user = userDAO.getUserByEmail(email);
        
        if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }
}
