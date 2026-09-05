package com.sunrise.controller;

import com.sunrise.dao.RememberTokenDAO;
import com.sunrise.util.TokenUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private final RememberTokenDAO rememberTokenDAO = new RememberTokenDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Prevent caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0); // Proxies.

        // Remove the persistent remember-me token from the database.
        Cookie[] cookies = request.getCookies();
        String rawToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName())) {
                    rawToken = cookie.getValue();
                    break;
                }
            }
        }
        if (rawToken != null && !rawToken.trim().isEmpty()) {
            rememberTokenDAO.deleteToken(TokenUtil.sha256Hex(rawToken.trim()));
        }

        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear remember-me cookies
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName()) || "remember_user".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }

        response.sendRedirect("login.jsp");
    }
}
