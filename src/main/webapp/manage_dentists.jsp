<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.sunrise.model.User" %>
<%@ page import="com.sunrise.model.Dentist" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
        response.sendRedirect("DashboardServlet?error=unauthorized");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Register Doctor - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <div class="sidebar-column col-md-3 col-lg-2 p-0">
                <jsp:include page="includes/sidebar.jsp" />
            </div>
            <div class="content-column col-md-9 col-lg-10 p-4">
                <h2>Register Doctor / Manage Dentists</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Register Doctor</li>
                    </ol>
                </nav>
                <hr>

                <%
                    String success = request.getParameter("success");
                    String error = request.getParameter("error");
                    if ("added".equals(success)) {
                %>
                    <div class="alert alert-success alert-dismissible fade show" role="alert">Doctor registered successfully.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>
                <%
                    } else if ("deleted".equals(success)) {
                %>
                    <div class="alert alert-success alert-dismissible fade show" role="alert">Doctor removed from the list successfully.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>
                <%
                    } else if ("add_failed".equals(error)) {
                %>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">Failed to register the doctor. Check the doctor name is unique.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>
                <%
                    } else if ("name_required".equals(error)) {
                %>
                    <div class="alert alert-warning alert-dismissible fade show" role="alert">Doctor name is required.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>
                <%
                    } else if ("delete_blocked".equals(error)) {
                %>
                    <div class="alert alert-warning alert-dismissible fade show" role="alert">This doctor cannot be removed because they already have appointments recorded in the system.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>
                <% } %>

                <div class="row">
                    <div class="col-md-5 mb-4">
                        <div class="card shadow-sm">
                            <div class="card-header bg-primary text-white"><h5 class="mb-0">Register New Doctor</h5></div>
                            <div class="card-body">
                                <form action="DentistServlet" method="post" id="dentistForm" novalidate>
                                    <input type="hidden" name="action" value="add">
                                    <div class="mb-3">
                                        <label for="dentistName" class="form-label">Doctor Name <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="dentistName" name="dentistName"
                                               placeholder="e.g. Dr. Gunasekara" required>
                                        <div class="invalid-feedback">Doctor name is required.</div>
                                    </div>
                                    <div class="mb-3">
                                        <label for="specialization" class="form-label">Specialization</label>
                                        <input type="text" class="form-control" id="specialization" name="specialization"
                                               placeholder="e.g. Orthodontics, Implantology, Oral Surgery">
                                    </div>
                                    <button type="submit" class="btn btn-success w-100">Register Doctor</button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-7">
                        <div class="card shadow-sm">
                            <div class="card-header bg-dark text-white"><h5 class="mb-0">Registered Doctors</h5></div>
                            <div class="card-body p-0">
                                <table class="table table-striped table-hover mb-0 align-middle">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Doctor Name</th>
                                            <th>Specialization</th>
                                            <th class="text-center">Remove</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            List<Dentist> dentistList = (List<Dentist>) request.getAttribute("dentists");
                                            if (dentistList != null && !dentistList.isEmpty()) {
                                                for (Dentist d : dentistList) {
                                        %>
                                        <tr>
                                            <td><strong><%= d.getDentistName() %></strong></td>
                                            <td><%= d.getSpecialization() != null && !d.getSpecialization().isEmpty() ? d.getSpecialization() : "-" %></td>
                                            <td class="text-center">
                                                <form action="DentistServlet" method="post" class="d-inline"
                                                      onsubmit="return confirm('Remove <%= d.getDentistName() %> from the registered doctor list?');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="dentistId" value="<%= d.getDentistId() %>">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm">Remove</button>
                                                </form>
                                            </td>
                                        </tr>
                                        <%
                                                }
                                            } else {
                                        %>
                                        <tr><td colspan="3" class="text-center text-muted py-4">No doctors registered yet.</td></tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div class="alert alert-info mt-3 mb-0 small">
                            <strong>Tip:</strong> Doctors registered here appear in the doctor dropdown when registering an appointment.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script>
        (() => {
            'use strict';
            const form = document.querySelector('#dentistForm');
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

