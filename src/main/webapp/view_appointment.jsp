<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.sunrise.model.User" %>
<%@ page import="com.sunrise.model.Appointment" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    Appointment appt = (Appointment) request.getAttribute("appointment");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Search Appointment - Sunrise Dental Clinic</title>
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
                <h2>Search Appointment Details</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Search Appointment</li>
                    </ol>
                </nav>
                <hr>
                
                <div class="row mb-4">
                    <div class="col-md-6">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                <form action="SearchAppointmentServlet" method="get" class="row g-2">
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" name="appointmentNo" placeholder="Enter Appointment No (e.g. APPT-1001)" required>
                                    </div>
                                    <div class="col-sm-3">
                                        <button type="submit" class="btn btn-primary w-100">Search</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>

                <% 
                    String error = request.getParameter("error");
                    if ("notfound".equals(error)) {
                %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Not Found!</strong> No appointment matching the number was found in the database.
                    </div>
                <% 
                    } else if ("empty".equals(error)) {
                %>
                    <div class="alert alert-danger" role="alert">
                        Please enter a valid Appointment Number.
                    </div>
                <%
                    }
                %>

                <% if (appt != null) { %>
                    <div class="card shadow-sm mt-3">
                        <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                            <h5 class="mb-0">Appointment #<%= appt.getAppointmentNo() %></h5>
                            <span class="badge bg-light text-dark">Status: Scheduled</span>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <h6 class="text-primary border-bottom pb-2">Patient Details</h6>
                                    <table class="table table-borderless">
                                        <tr>
                                            <th style="width: 150px;">Patient ID:</th>
                                            <td><%= appt.getPatientId() %></td>
                                        </tr>
                                        <tr>
                                            <th>Patient Name:</th>
                                            <td><%= appt.getPatientName() %></td>
                                        </tr>
                                        <tr>
                                            <th>Contact Number:</th>
                                            <td><%= appt.getPatientContact() %></td>
                                        </tr>
                                    </table>
                                </div>
                                <div class="col-md-6">
                                    <h6 class="text-primary border-bottom pb-2">Appointment Details</h6>
                                    <table class="table table-borderless">
                                        <tr>
                                            <th style="width: 150px;">Dentist Name:</th>
                                            <td>Dr. <%= appt.getDentistName() %></td>
                                        </tr>
                                        <tr>
                                            <th>Treatment Type:</th>
                                            <td><%= appt.getTreatmentName() %> ($<%= appt.getTreatmentCost() %>)</td>
                                        </tr>
                                        <tr>
                                            <th>Date & Time:</th>
                                            <td><%= appt.getAppointmentDate() %></td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div class="card-footer bg-light text-end">
                            <a href="BillServlet?appointmentNo=<%= appt.getAppointmentNo() %>" class="btn btn-primary">
                                Calculate and Print Bill
                            </a>
                        </div>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</body>
</html>
