<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.sunrise.model.User" %>
<%@ page import="com.sunrise.model.Appointment" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Appointment appt = (Appointment) request.getAttribute("appointment");
    List<Appointment> appointments = (List<Appointment>) request.getAttribute("appointments");
    String error = request.getParameter("error");
    String success = request.getParameter("success");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Appointment Management - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-3 col-lg-2 p-0">
                <jsp:include page="includes/sidebar.jsp" />
            </div>

            <div class="col-md-9 col-lg-10 p-4">
                <h2>Appointment Management</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Appointments</li>
                    </ol>
                </nav>
                <hr>

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div class="btn-group">
                        <a href="AppointmentServlet?action=new" class="btn btn-success">+ New Appointment</a>
                    </div>

                    <form action="SearchAppointmentServlet" method="get" class="d-flex gap-2">
                        <input type="text" class="form-control" name="appointmentNo" placeholder="Search by appointment number" style="min-width: 260px;" required>
                        <button type="submit" class="btn btn-primary">Search</button>
                    </form>
                </div>

                <% if ("notfound".equals(error)) { %>
                    <div class="alert alert-warning" role="alert">
                        No appointment matching that appointment number was found.
                    </div>
                <% } else if ("empty".equals(error)) { %>
                    <div class="alert alert-danger" role="alert">
                        Please enter a valid appointment number.
                    </div>
                <% } else if ("delete_failed".equals(error)) { %>
                    <div class="alert alert-danger" role="alert">
                        The appointment could not be deleted.
                    </div>
                <% } else if ("deleted".equals(success)) { %>
                    <div class="alert alert-success" role="alert">
                        Appointment cancelled successfully.
                    </div>
                <% } %>

                <% if (appointments != null && !appointments.isEmpty()) { %>
                    <div class="card shadow-sm mt-3">
                        <div class="card-header bg-dark text-white">
                            <h5 class="mb-0">Appointments</h5>
                        </div>
                        <div class="card-body p-0">
                            <table class="table table-striped table-hover mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>Appointment No</th>
                                        <th>Patient</th>
                                        <th>Dentist</th>
                                        <th>Treatment</th>
                                        <th>Date</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Appointment item : appointments) { %>
                                        <tr>
                                            <td><%= item.getAppointmentNo() %></td>
                                            <td><%= item.getPatientName() %></td>
                                            <td><%= item.getDentistName() %></td>
                                            <td><%= item.getTreatmentName() %></td>
                                            <td><%= item.getAppointmentDate() %></td>
                                            <td>
                                                <span class="badge <%= "CANCELLED".equals(item.getStatus()) ? "bg-secondary" : "bg-success" %>"><%= item.getStatus() %></span>
                                            </td>
                                            <td>
                                                <div class="btn-group btn-group-sm">
                                                    <a href="SearchAppointmentServlet?appointmentNo=<%= item.getAppointmentNo() %>" class="btn btn-outline-primary">View</a>
                                                    <a href="AppointmentServlet?action=edit&appointmentNo=<%= item.getAppointmentNo() %>" class="btn btn-outline-warning">Edit</a>
                                                    <a href="AppointmentServlet?action=delete&appointmentNo=<%= item.getAppointmentNo() %>" class="btn btn-outline-danger" onclick="return confirm('Are you sure you want to delete appointment <%= item.getAppointmentNo() %>?');">Delete</a>
                                                </div>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                <% } %>

                <% if (appt != null) { %>
                    <div class="card shadow-sm mt-4">
                        <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                            <h5 class="mb-0">Appointment Details: <%= appt.getAppointmentNo() %></h5>
                            <span class="badge bg-light text-dark">Status: <%= appt.getStatus() %></span>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <h6 class="text-primary border-bottom pb-2">Patient Details</h6>
                                    <table class="table table-borderless">
                                        <tr><th style="width: 150px;">Patient:</th><td><%= appt.getPatientName() %></td></tr>
                                        <tr><th>Contact:</th><td><%= appt.getPatientContact() %></td></tr>
                                    </table>
                                </div>
                                <div class="col-md-6">
                                    <h6 class="text-primary border-bottom pb-2">Appointment Details</h6>
                                    <table class="table table-borderless">
                                        <tr><th style="width: 150px;">Dentist:</th><td><%= appt.getDentistName() %></td></tr>
                                        <tr><th>Treatment:</th><td><%= appt.getTreatmentName() %> ($<%= appt.getTreatmentCost() %>)</td></tr>
                                        <tr><th>Date & Time:</th><td><%= appt.getAppointmentDate() %></td></tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div class="card-footer bg-light text-end">
                            <a href="BillServlet?appointmentNo=<%= appt.getAppointmentNo() %>" class="btn btn-primary">Calculate and Print Bill</a>
                            <a href="AppointmentServlet?action=edit&appointmentNo=<%= appt.getAppointmentNo() %>" class="btn btn-warning">Edit</a>
                        </div>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</body>
</html>
