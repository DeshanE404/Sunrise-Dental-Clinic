package com.sunrise.filter;

import com.sunrise.dao.UserDAO;
import com.sunrise.model.User;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthenticationFilter implements Filter {
    private final UserDAO userDAO = new UserDAO();

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
        boolean isInitialAdminRegistration = "POST".equalsIgnoreCase(httpRequest.getMethod())
                && "/UserManagementServlet".equals(path)
                && userDAO.getAdminCount() == 0;

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
                || isInitialAdminRegistration;

        if (isPublicResource) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            httpResponse.sendRedirect("login.jsp?error=session_expired");
            return;
        }

        boolean isAdminPage = uri.contains("UserManagementServlet")
                || uri.contains("ReportServlet")
                || uri.contains("manage_users.jsp")
                || uri.contains("reports.jsp");

        if (isAdminPage && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            httpResponse.sendRedirect("DashboardServlet?error=unauthorized");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no cleanup required
    }
}
