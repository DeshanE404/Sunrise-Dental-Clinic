<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.sunrise.model.User" %>
<%@ page import="java.util.Map" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    if (!"ADMIN".equals(user.getRole())) {
        response.sendRedirect("DashboardServlet?error=unauthorized");
        return;
    }
    Double totalRevenue = (Double) request.getAttribute("totalRevenue");
    Map<String, Integer> treatmentStats = (Map<String, Integer>) request.getAttribute("treatmentStats");
    Map<String, Integer> dentistStats = (Map<String, Integer>) request.getAttribute("dentistStats");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Admin Reports - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <div class="col-md-3 col-lg-2 p-0">
                <jsp:include page="includes/sidebar.jsp" />
            </div>
            
            <!-- Main Content -->
            <div class="col-md-9 col-lg-10 p-4">
                <h2>Administrative Management Reports</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Reports</li>
                    </ol>
                </nav>
                <hr>

                <!-- Revenue summary Card -->
                <div class="card border-primary mb-4 shadow-sm" style="max-width: 400px;">
                    <div class="card-body text-center py-4">
                        <h5 class="card-title text-muted text-uppercase mb-2">Total Estimated Revenue</h5>
                        <h1 class="display-5 text-primary fw-bold">$<%= (totalRevenue != null) ? String.format("%.2f", totalRevenue) : "0.00" %></h1>
                        <p class="card-text text-muted small">Sum of all bills generated in the database</p>
                    </div>
                </div>

                <div class="row">
                    <!-- Treatment Stats -->
                    <div class="col-md-6 mb-4">
                        <div class="card shadow-sm h-100">
                            <div class="card-header bg-dark text-white">
                                <h5 class="mb-0">Popularity of Treatment Types</h5>
                            </div>
                            <div class="card-body">
                                <% if (treatmentStats != null && !treatmentStats.isEmpty()) { %>
                                    <ul class="list-group list-group-flush">
                                        <% for (Map.Entry<String, Integer> entry : treatmentStats.entrySet()) { %>
                                            <li class="list-group-item d-flex justify-content-between align-items-center">
                                                <%= entry.getKey() %>
                                                <span class="badge bg-primary rounded-pill"><%= entry.getValue() %> bookings</span>
                                            </li>
                                        <% } %>
                                    </ul>
                                <% } else { %>
                                    <p class="text-muted text-center py-4">No treatment statistics available.</p>
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <!-- Dentist Load Stats -->
                    <div class="col-md-6 mb-4">
                        <div class="card shadow-sm h-100">
                            <div class="card-header bg-dark text-white">
                                <h5 class="mb-0">Dentist Loading Statistics</h5>
                            </div>
                            <div class="card-body">
                                <% if (dentistStats != null && !dentistStats.isEmpty()) { %>
                                    <ul class="list-group list-group-flush">
                                        <% for (Map.Entry<String, Integer> entry : dentistStats.entrySet()) { %>
                                            <li class="list-group-item d-flex justify-content-between align-items-center">
                                                Dr. <%= entry.getKey() %>
                                                <span class="badge bg-success rounded-pill"><%= entry.getValue() %> patients scheduled</span>
                                            </li>
                                        <% } %>
                                    </ul>
                                <% } else { %>
                                    <p class="text-muted text-center py-4">No dentist load statistics available.</p>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
