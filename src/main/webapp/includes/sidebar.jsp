<%@ page import="com.sunrise.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
%>
<style>
    /* Sidebar Teal Theme */
    .sidebar-bg { background-color: #1a2226; }
    .nav-link { color: #adb5bd; padding: 12px 20px; transition: 0.3s; }
    .nav-link:hover, .nav-link.active { color: white; background-color: #20c997; border-radius: 5px; }
    .sidebar-brand { color: #20c997; font-weight: bold; letter-spacing: 1px; }
</style>

<div class="sidebar sidebar-bg d-flex flex-column flex-shrink-0 p-3" style="min-height: 100vh;">
    <a href="DashboardServlet" class="d-flex align-items-center mb-4 me-md-auto text-decoration-none">
        <span class="fs-4 sidebar-brand">Sunrise Dental</span>
    </a>
    
    <ul class="nav flex-column mb-auto gap-2">
        <li class="nav-item">
            <a href="DashboardServlet" class="nav-link">
                Dashboard
            </a>
        </li>
        <li>
            <a href="AppointmentServlet?action=new" class="nav-link">
                Register Appointment
            </a>
        </li>
        <li>
            <a href="view_appointment.jsp" class="nav-link">
                Search Appointment
            </a>
        </li>
        <li>
            <a href="view_appointment.jsp" class="nav-link">
                Billing
            </a>
        </li>
        
        <% if (currentUser != null && "ADMIN".equals(currentUser.getRole())) { %>
            <li>
                <a href="UserManagementServlet" class="nav-link">
                    Manage Users
                </a>
            </li>
            <li>
                <a href="ReportServlet" class="nav-link">
                    Reports
                </a>
            </li>
        <% } %>
        
        <li>
            <a href="help.jsp" class="nav-link">
                Help
            </a>
        </li>
    </ul>
    
    <hr style="border-color: #4b545c;">
    
    <div>
        <a href="LogoutServlet" class="btn btn-outline-danger w-100">Logout</a>
    </div>
</div>
