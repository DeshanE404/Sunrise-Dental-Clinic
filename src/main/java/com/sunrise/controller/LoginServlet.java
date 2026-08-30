package com.sunrise.controller;

import com.sunrise.model.User;
import com.sunrise.service.AuthenticationService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private AuthenticationService authService = new AuthenticationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String passwordRaw = request.getParameter("password");
        String remember = request.getParameter("remember");
        
        // Input validation
        if (email == null || email.trim().isEmpty() || passwordRaw == null || passwordRaw.trim().isEmpty()) {
            response.sendRedirect("login.jsp?error=empty");
            return;
        }

        User user = authService.login(email.trim(), passwordRaw);
        
        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            
            // Handle Remember Me Cookie
            if (remember != null && remember.equals("on")) {
                Cookie c = new Cookie("remember_user", email.trim());
                c.setMaxAge(60 * 60 * 24 * 30); // 30 days
                response.addCookie(c);
            } else {
                Cookie c = new Cookie("remember_user", "");
                c.setMaxAge(0); // Delete cookie
                response.addCookie(c);
            }
            
            response.sendRedirect("DashboardServlet");
        } else {
            response.sendRedirect("login.jsp?error=invalid");
        }
    }
}
