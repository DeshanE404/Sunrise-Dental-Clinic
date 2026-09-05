package com.sunrise.controller;

import com.sunrise.model.User;
import com.sunrise.dao.DatabaseConnection;
import com.sunrise.service.UserService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/UserManagementServlet")
public class UserManagementServlet extends HttpServlet {
    private UserService userService = new UserService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (!"ADMIN".equals(currentUser.getRole())) {
            response.sendRedirect("DashboardServlet?error=unauthorized");
            return;
        }

        List<User> users = userService.getAllUsers();
        request.setAttribute("users", users);
        request.getRequestDispatcher("manage_users.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // Delete an existing user (ADMIN or RECEPTION). Only admins may do this.
        if ("delete".equals(action)) {
            deleteUser(request, response);
            return;
        }

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String passwordRaw = request.getParameter("password");
        String employeeNumber = request.getParameter("employee_number");
        String phoneNumber = request.getParameter("phone_number");
        String role = request.getParameter("role");

        if (name == null || email == null || passwordRaw == null || employeeNumber == null || phoneNumber == null || role == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required parameters.");
            return;
        }

        // Get current user from session (will be null for Postman without session)
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (userService.getAdminCount() < 0) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Cannot connect to PostgreSQL: " + DatabaseConnection.getLastFailure());
            return;
        }

        boolean success = userService.registerUser(name.trim(), email.trim(), passwordRaw, employeeNumber.trim(), phoneNumber.trim(), role.trim(), currentUser);

        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            if (currentUser != null) {
                // If requested from UI
                response.sendRedirect("UserManagementServlet?success=true");
            } else {
                // If requested from Postman
                response.getWriter().write("User created successfully!");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (currentUser != null) {
                // If requested from UI
                response.sendRedirect("UserManagementServlet?error=creation_failed");
            } else {
                // If requested from Postman
                response.getWriter().write("Unauthorized or failed to create user. Ensure email and employee number are unique.");
            }
        }
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect("DashboardServlet?error=unauthorized");
            return;
        }

        String userIdParam = request.getParameter("userId");
        if (userIdParam == null || userIdParam.trim().isEmpty()) {
            response.sendRedirect("UserManagementServlet?error=delete_failed");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdParam.trim());
            int result = userService.removeUser(userId, currentUser);
            switch (result) {
                case 0:
                    response.sendRedirect("UserManagementServlet?success=deleted");
                    break;
                case 1:
                    response.sendRedirect("UserManagementServlet?error=user_not_found");
                    break;
                case 3:
                    response.sendRedirect("UserManagementServlet?error=self_blocked");
                    break;
                case 4:
                    response.sendRedirect("UserManagementServlet?error=last_admin_blocked");
                    break;
                default:
                    response.sendRedirect("UserManagementServlet?error=delete_failed");
                    break;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("UserManagementServlet?error=delete_failed");
        }
    }
}
