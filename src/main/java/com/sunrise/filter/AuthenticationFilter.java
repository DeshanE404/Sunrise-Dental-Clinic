package com.sunrise.filter;

import com.sunrise.dao.RememberTokenDAO;
import com.sunrise.dao.UserDAO;
import com.sunrise.model.User;
import com.sunrise.util.TokenUtil;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthenticationFilter implements Filter {
    private final UserDAO userDAO = new UserDAO();
    private final RememberTokenDAO rememberTokenDAO = new RememberTokenDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no initialization required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getServletPath();
        String uri = httpRequest.getRequestURI();

        // The application needs one unauthenticated route to bootstrap its first
        // administrator. UserManagementServlet performs the same count check and
        // only accepts an ADMIN role when no administrator exists.
        boolean isUserRegistration = "POST".equalsIgnoreCase(httpRequest.getMethod())
            && "/UserManagementServlet".equals(path);

        boolean isPublicResource = path == null || path.isEmpty()
                || path.equals("/login.jsp")
                || path.equals("/LoginServlet")
                || path.equals("/LogoutServlet")
                || path.startsWith("/assets/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".svg")
                || path.endsWith(".ico")
                || isUserRegistration;

        if (isPublicResource) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            // No live session: try the persistent "remember me" cookie so users
            // stay logged in even after a server restart.
            user = tryRememberMe(httpRequest);
            if (user == null) {
                httpResponse.sendRedirect("login.jsp?error=session_expired");
                return;
            }
        }

        boolean isAdminPage = uri.contains("UserManagementServlet")
                || uri.contains("ReportServlet")
                || uri.contains("DentistServlet")
                || uri.contains("manage_users.jsp")
                || uri.contains("manage_dentists.jsp")
                || uri.contains("reports.jsp");

        if (isAdminPage && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            httpResponse.sendRedirect("DashboardServlet?error=unauthorized");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Restores a session from the persistent remember_token cookie when no
     * server-side session exists (for example after a server restart or after
     * the session timed out). Returns the logged-in user, or null when the
     * cookie is absent/invalid/expired.
     */
    private User tryRememberMe(HttpServletRequest request) {
        Cookie tokenCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName())) {
                    tokenCookie = cookie;
                    break;
                }
            }
        }
        if (tokenCookie == null) {
            return null;
        }

        String rawToken = tokenCookie.getValue();
        if (rawToken == null || rawToken.trim().isEmpty()) {
            return null;
        }

        Integer userId = rememberTokenDAO.findUserIdByTokenHash(TokenUtil.sha256Hex(rawToken.trim()));
        if (userId == null) {
            return null;
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            return null;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setMaxInactiveInterval(15 * 60);
        return user;
    }

    @Override
    public void destroy() {
        // no cleanup required
    }
}
