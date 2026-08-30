package com.sunrise.controller;

import com.sunrise.model.User;
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
}
