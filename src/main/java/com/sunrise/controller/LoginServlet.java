package com.sunrise.controller;

import com.sunrise.dao.RememberTokenDAO;
import com.sunrise.model.User;
import com.sunrise.service.AuthenticationService;
import com.sunrise.util.TokenUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
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
    private final RememberTokenDAO rememberTokenDAO = new RememberTokenDAO();

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

            boolean rememberOn = "on".equalsIgnoreCase(remember);
            if (rememberOn) {
                issueRememberToken(user.getId(), request, response);
            } else {
                clearRememberToken(request, response);
            }

            // Keep the lightweight email prefill cookie for the login page.
            Cookie rememberCookie = new Cookie("remember_user", email.trim());
            rememberCookie.setPath("/");
            rememberCookie.setHttpOnly(true);
            rememberCookie.setMaxAge(rememberOn ? 30 * 24 * 60 * 60 : 0);
            response.addCookie(rememberCookie);

            response.sendRedirect("DashboardServlet");
            return;
        }

        String encodedEmail = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
        response.sendRedirect("login.jsp?error=invalid&email=" + encodedEmail);
    }

    /**
     * Creates a persistent "remember me" token and stores it in an HttpOnly
     * cookie. Only the SHA-256 hash of the token is saved in the database so a
     * session can be re-created after a server restart.
     */
    private void issueRememberToken(int userId, HttpServletRequest request, HttpServletResponse response) {
        // Discard any previous token before issuing a fresh one.
        clearRememberToken(request, response);

        String rawToken = TokenUtil.generateToken();
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + TokenUtil.REMEMBER_DURATION_MS);
        rememberTokenDAO.saveToken(userId, TokenUtil.sha256Hex(rawToken), expiresAt);

        Cookie tokenCookie = new Cookie("remember_token", rawToken);
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge((int) (TokenUtil.REMEMBER_DURATION_MS / 1000));
        response.addCookie(tokenCookie);
    }

    /** Expires the remember_token cookie and deletes its database row. */
    private void clearRememberToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String rawToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName())) {
                    rawToken = cookie.getValue();
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        if (rawToken != null && !rawToken.trim().isEmpty()) {
            rememberTokenDAO.deleteToken(TokenUtil.sha256Hex(rawToken.trim()));
        }
    }
}
