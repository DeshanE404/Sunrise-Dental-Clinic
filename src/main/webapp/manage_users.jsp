<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.sunrise.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    if (!"ADMIN".equals(currentUser.getRole())) {
        response.sendRedirect("DashboardServlet?error=unauthorized");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Manage Users - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <div class="sidebar-column col-md-3 col-lg-2 p-0">
                <jsp:include page="includes/sidebar.jsp" />
            </div>
            
            <!-- Main Content -->
            <div class="content-column col-md-9 col-lg-10 p-4">
                <h2>User Management</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Manage Users</li>
                    </ol>
                </nav>
                <hr>

                <% 
                    String success = request.getParameter("success");
                    String error = request.getParameter("error");
                    if ("true".equals(success)) {
                %>
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        User created successfully!
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <% 
                    } else if ("deleted".equals(success)) {
                %>
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        User removed successfully.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <%
                    } else if ("creation_failed".equals(error)) {
                %>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        Failed to create user. Ensure Email and Employee Number are unique.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <%
                    } else if ("self_blocked".equals(error)) {
                %>
                    <div class="alert alert-warning alert-dismissible fade show" role="alert">
                        You cannot remove your own account while you are logged in.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <%
                    } else if ("last_admin_blocked".equals(error)) {
                %>
                    <div class="alert alert-warning alert-dismissible fade show" role="alert">
                        You cannot remove the last remaining ADMIN. Create another admin first.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <%
                    } else if ("user_not_found".equals(error)) {
                %>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        The user you tried to remove no longer exists.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <%
                    } else if ("delete_failed".equals(error)) {
                %>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        Failed to remove the user. Please try again.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <% } %>

                <div class="row">
                    <!-- Create User Form -->
                    <div class="col-md-5 mb-4">
                        <div class="card shadow-sm">
                            <div class="card-header bg-primary text-white">
                                <h5 class="mb-0">Register New User</h5>
                            </div>
                            <div class="card-body">
                                <form action="UserManagementServlet" method="post" id="userForm" novalidate>
                                    <div class="mb-3">
                                        <label for="name" class="form-label">Full Name</label>
                                        <input type="text" class="form-control" id="name" name="name" required>
                                        <div class="invalid-feedback">Full name is required.</div>
                                    </div>
                                    <div class="mb-3">
                                        <label for="email" class="form-label">Email Address</label>
                                        <input type="email" class="form-control" id="email" name="email" required>
                                        <div class="invalid-feedback">A valid email is required.</div>
                                    </div>
                                    <div class="mb-3">
                                        <label for="password" class="form-label">Password</label>
                                        <input type="password" class="form-control" id="password" name="password" required>
                                        <div class="invalid-feedback">Password is required.</div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label for="employee_number" class="form-label">Employee Number</label>
                                            <input type="text" class="form-control" id="employee_number" name="employee_number" required>
                                            <div class="invalid-feedback">Employee Number is required.</div>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label for="phone_number" class="form-label">Phone Number</label>
                                            <input type="text" class="form-control" id="phone_number" name="phone_number" required>
                                            <div class="invalid-feedback">Phone Number is required.</div>
                                        </div>
                                    </div>
                                    <div class="mb-3">
                                        <label for="role" class="form-label">Role</label>
                                        <select class="form-select" id="role" name="role" required>
                                            <option value="RECEPTION">Receptionist</option>
                                            <option value="ADMIN">Administrator</option>
                                        </select>
                                    </div>
                                    <button type="submit" class="btn btn-success w-100">Create Account</button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- User List -->
                    <div class="col-md-7">
                        <div class="card shadow-sm">
                            <div class="card-header bg-dark text-white">
                                <h5 class="mb-0">System Users</h5>
                            </div>
                            <div class="card-body p-0">
                                <table class="table table-striped table-hover mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Emp No</th>
                                            <th>Name</th>
                                            <th>Email</th>
                                            <th>Role</th>
                                            <th class="text-center">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                            List<User> userList = (List<User>) request.getAttribute("users");
                                            if (userList != null && !userList.isEmpty()) {
                                                for(User u : userList) {
                                                    boolean isSelf = currentUser != null && u.getId() == currentUser.getId();
                                        %>
                                        <tr>
                                            <td><%= u.getEmployeeNumber() %></td>
                                            <td>
                                                <%= u.getName() %>
                                                <% if (isSelf) { %>
                                                    <span class="badge bg-info ms-1">You</span>
                                                <% } %>
                                            </td>
                                            <td><%= u.getEmail() %></td>
                                            <td>
                                                <% if ("ADMIN".equals(u.getRole())) { %>
                                                    <span class="badge bg-danger">ADMIN</span>
                                                <% } else { %>
                                                    <span class="badge bg-secondary">RECEPTION</span>
                                                <% } %>
                                            </td>
                                            <td class="text-center">
                                                <% if (!isSelf) { %>
                                                    <form action="UserManagementServlet" method="post" class="d-inline"
                                                          onsubmit="return confirm('Remove user <%= u.getName().replace("'", "\\'") %> (<%= u.getEmail() %>)?');">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="userId" value="<%= u.getId() %>">
                                                        <button type="submit" class="btn btn-outline-danger btn-sm">Remove</button>
                                                    </form>
                                                <% } else { %>
                                                    <span class="text-muted small">-</span>
                                                <% } %>
                                            </td>
                                        </tr>
                                        <% 
                                                }
                                            } else {
                                        %>
                                        <tr><td colspan="5" class="text-center text-muted">No users found.</td></tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script>
        (() => {
            'use strict';
            const form = document.querySelector('#userForm');
            form.addEventListener('submit', event => {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        })();
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
