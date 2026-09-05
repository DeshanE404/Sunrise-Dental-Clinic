package com.sunrise.service;

import com.sunrise.dao.UserDAO;
import com.sunrise.model.User;
import com.sunrise.util.PasswordUtil;
import java.util.List;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public boolean registerUser(String name, String email, String password, String employeeNumber, String phoneNumber, String role, User currentSessionUser) {
        // Validation: If no admin exists, allow creating the first admin without authentication
        int adminCount = userDAO.getAdminCount();
        if (adminCount < 0) {
            return false;
        }
        if ("ADMIN".equalsIgnoreCase(role) && adminCount == 0) {
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

    /**
     * Removes an existing ADMIN or RECEPTION user.
     * Only an ADMIN can remove users, an admin cannot remove their own
     * account, and the very last remaining ADMIN cannot be removed so the
     * system always has at least one administrator.
     *
     * Returns 0 = success, 1 = target not found, 2 = forbidden, 3 = self-removal
     * blocked, 4 = cannot remove the last admin.
     */
    public int removeUser(int userId, User currentSessionUser) {
        if (currentSessionUser == null || !"ADMIN".equalsIgnoreCase(currentSessionUser.getRole())) {
            return 2;
        }

        User target = userDAO.getUserById(userId);
        if (target == null) {
            return 1;
        }

        if (target.getId() == currentSessionUser.getId()) {
            return 3;
        }

        if ("ADMIN".equalsIgnoreCase(target.getRole()) && userDAO.getAdminCount() <= 1) {
            return 4;
        }

        return userDAO.deleteUser(userId) ? 0 : 5;
    }

    public int getAdminCount() {
        return userDAO.getAdminCount();
    }
}
