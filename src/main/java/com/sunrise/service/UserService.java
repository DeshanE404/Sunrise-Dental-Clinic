package com.sunrise.service;

import com.sunrise.dao.UserDAO;
import com.sunrise.model.User;
import com.sunrise.util.PasswordUtil;
import java.util.List;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public boolean registerUser(String name, String email, String password, String employeeNumber, String phoneNumber, String role, User currentSessionUser) {
        // Validation: If no admin exists, allow creating the first admin without authentication
        if ("ADMIN".equalsIgnoreCase(role) && userDAO.getAdminCount() == 0) {
            return saveUser(name, email, password, employeeNumber, phoneNumber, "ADMIN");
        }
        
        // Otherwise, enforce that ONLY an ADMIN can create a new user (Admin or Reception)
        if (currentSessionUser == null || !"ADMIN".equalsIgnoreCase(currentSessionUser.getRole())) {
            return false; // Unauthorized
        }

        return saveUser(name, email, password, employeeNumber, phoneNumber, role.toUpperCase());
    }

    private boolean saveUser(String name, String email, String password, String employeeNumber, String phoneNumber, String role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setEmployeeNumber(employeeNumber);
        user.setPhoneNumber(phoneNumber);
        user.setRole(role);
        return userDAO.createUser(user);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
}
