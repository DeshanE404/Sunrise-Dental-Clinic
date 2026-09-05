<%@ page import="com.sunrise.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
%>
<style>
    .sidebar-column { transition: flex-basis .2s ease, max-width .2s ease; }
    .sidebar { width: 100%; transition: width .2s ease; }
    .sidebar-bg { background-color: #1a2226; }
    .nav-link { color: #adb5bd; padding: 12px 20px; transition: 0.3s; }
    .nav-link:hover, .nav-link.active { color: white; background-color: #20c997; border-radius: 5px; }
    .sidebar-brand { color: #20c997; font-weight: bold; letter-spacing: 1px; }
    .sidebar-toggle { color: #adb5bd; border: 1px solid #4b545c; background: transparent; }
    .sidebar-toggle:hover { color: white; border-color: #20c997; }
    body.sidebar-collapsed .sidebar-column { flex: 0 0 76px; max-width: 76px; }
    body.sidebar-collapsed .content-column { flex: 0 0 calc(100% - 76px); max-width: calc(100% - 76px); }
    body.sidebar-collapsed .sidebar-brand,
    body.sidebar-collapsed .nav-label,
    body.sidebar-collapsed .logout-label { display: none; }
    body.sidebar-collapsed .sidebar { padding-left: .5rem !important; padding-right: .5rem !important; }
    body.sidebar-collapsed .nav-link { padding-left: 0; padding-right: 0; text-align: center; }
    body.sidebar-collapsed .sidebar-toggle { width: 100%; }
    @media (max-width: 767.98px) {
        .sidebar-column { flex: 0 0 100%; max-width: 100%; }
        .content-column { flex: 0 0 100%; max-width: 100%; }
        body.sidebar-collapsed .sidebar-column { flex-basis: 100%; max-width: 100%; }
        body.sidebar-collapsed .content-column { flex-basis: 100%; max-width: 100%; }
    }
</style>

<div class="sidebar sidebar-bg d-flex flex-column flex-shrink-0 p-3" style="min-height: 100vh;">
    <div class="d-flex align-items-center mb-4 gap-2">
        <button type="button" class="btn sidebar-toggle" id="sidebarToggle" aria-label="Collapse sidebar" title="Collapse sidebar">&#9776;</button>
        <a href="DashboardServlet" class="text-decoration-none">
            <span class="fs-4 sidebar-brand">Sunrise Dental</span>
        </a>
    </div>
    
    <ul class="nav flex-column mb-auto gap-2">
        <li class="nav-item">
            <a href="DashboardServlet" class="nav-link">
                <span class="nav-label">Dashboard</span>
            </a>
        </li>
        <li>
            <a href="AppointmentServlet?action=new" class="nav-link">
                <span class="nav-label">Register Appointment</span>
            </a>
        </li>
        <li>
            <a href="SearchAppointmentServlet" class="nav-link">
                <span class="nav-label">Search Appointment</span>
            </a>
        </li>
        <li>
            <a href="SearchAppointmentServlet" class="nav-link">
                <span class="nav-label">Billing</span>
            </a>
        </li>
        
        <% if (currentUser != null && "ADMIN".equals(currentUser.getRole())) { %>
            <li>
                <a href="DentistServlet" class="nav-link">
                    <span class="nav-label">Register Doctor</span>
                </a>
            </li>
            <li>
                <a href="UserManagementServlet" class="nav-link">
                    <span class="nav-label">Manage Users</span>
                </a>
            </li>
            <li>
                <a href="ReportServlet" class="nav-link">
                    <span class="nav-label">Reports</span>
                </a>
            </li>
        <% } %>
        
        <li>
            <a href="help.jsp" class="nav-link">
                <span class="nav-label">Help</span>
            </a>
        </li>
    </ul>
    
    <hr style="border-color: #4b545c;">
    
    <div>
        <a href="LogoutServlet" class="btn btn-outline-danger w-100"><span class="logout-label">Logout</span>&#10132;</a>
    </div>
</div>
<script>
    (() => {
        const key = 'sunrise-sidebar-collapsed';
        const applyState = collapsed => {
            document.body.classList.toggle('sidebar-collapsed', collapsed);
            const toggle = document.getElementById('sidebarToggle');
            if (toggle) {
                toggle.setAttribute('aria-label', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
                toggle.setAttribute('title', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
            }
        };
        applyState(localStorage.getItem(key) === 'true');
        document.getElementById('sidebarToggle')?.addEventListener('click', () => {
            const collapsed = !document.body.classList.contains('sidebar-collapsed');
            localStorage.setItem(key, collapsed);
            applyState(collapsed);
        });
    })();
</script>
