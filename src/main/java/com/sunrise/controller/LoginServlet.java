package com.sunrise.controller;

import com.sunrise.model.User;
import com.sunrise.service.AuthenticationService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect("DashboardServlet");
            return;
        }

        response.sendRedirect("login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String remember = request.getParameter("remember");

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            response.sendRedirect("login.jsp?error=empty");
            return;
        }

        User user = authService.login(email.trim(), password);

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(15 * 60);

            Cookie rememberCookie = new Cookie("remember_user", email.trim());
            rememberCookie.setPath("/");
            rememberCookie.setHttpOnly(true);
            rememberCookie.setMaxAge("on".equalsIgnoreCase(remember) ? 30 * 24 * 60 * 60 : 0);
            response.addCookie(rememberCookie);

            response.sendRedirect("DashboardServlet");
            return;
        }

        String encodedEmail = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
        response.sendRedirect("login.jsp?error=invalid&email=" + encodedEmail);
    }
}
