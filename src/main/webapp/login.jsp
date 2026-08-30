<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String savedEmail = "";
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie c : cookies) {
            if ("remember_user".equals(c.getName())) {
                savedEmail = c.getValue() == null ? "" : c.getValue();
            }
        }
    }

    String emailValue = request.getParameter("email") != null ? request.getParameter("email") : savedEmail;
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Login - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
    .bg-teal { background-color: #20c997 !important; }
    .btn-teal { background-color: #20c997; color: white; border: none; }
    .btn-teal:hover { background-color: #1aa179; color: white; }
</style>
</head>
<body class="bg-light">
    <div class="container">
        <div class="row justify-content-center align-items-center" style="min-height: 100vh;">
            <div class="col-md-5">
                <div class="card shadow-lg border-0 rounded-lg">
                    <div class="card-header bg-teal text-white text-center py-4">
                        <h3 class="font-weight-light mb-0 fw-bold">Sunrise Dental Clinic</h3>
                        <p class="mb-0 small text-white-50">Appointment & Patient Management System</p>
                    </div>
                    <div class="card-body p-4">
                        <%
                            String error = request.getParameter("error");
                            if ("invalid".equals(error)) {
                        %>
                            <div class="alert alert-danger" role="alert">
                                Invalid email or password. Please try again.
                            </div>
                        <%
                            } else if ("empty".equals(error)) {
                        %>
                            <div class="alert alert-warning" role="alert">
                                Email and password cannot be empty.
                            </div>
                        <%
                            } else if ("session_expired".equals(error)) {
                        %>
                            <div class="alert alert-warning" role="alert">
                                Your session has expired. Please log in again.
                            </div>
                        <%
                            } else if ("unauthorized".equals(error)) {
                        %>
                            <div class="alert alert-danger" role="alert">
                                You do not have permission to access that page.
                            </div>
                        <%
                            }
                        %>

                        <form action="LoginServlet" method="post" id="loginForm" novalidate>
                            <div class="mb-3">
                                <label for="email" class="form-label text-secondary fw-bold">Email Address</label>
                                <input type="email" class="form-control form-control-lg" id="email" name="email" value="<%= emailValue %>" required placeholder="Enter your email">
                                <div class="invalid-feedback">Please enter a valid email address.</div>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label text-secondary fw-bold">Password</label>
                                <input type="password" class="form-control form-control-lg" id="password" name="password" required placeholder="Enter your password">
                                <div class="invalid-feedback">Please enter your password.</div>
                            </div>

                            <div class="mb-3 form-check">
                                <input type="checkbox" class="form-check-input" id="remember" name="remember" <%= !savedEmail.isEmpty() ? "checked" : "" %>>
                                <label class="form-check-label text-secondary" for="remember">Remember me</label>
                            </div>

                            <div class="d-grid gap-2 mt-4">
                                <button type="submit" class="btn btn-teal btn-lg fw-bold">Secure Login</button>
                            </div>
                        </form>
                    </div>
                    <div class="card-footer text-center py-3 bg-light border-0">
                        <div class="small text-muted">&copy; 2026 Sunrise Dental Clinic, Colombo</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        (() => {
            'use strict';
            const forms = document.querySelectorAll('#loginForm');
            Array.from(forms).forEach(form => {
                form.addEventListener('submit', event => {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                    form.classList.add('was-validated');
                }, false);
            });
        })();
    </script>
</body>
</html>
