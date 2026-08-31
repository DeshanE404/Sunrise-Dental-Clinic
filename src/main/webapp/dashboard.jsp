<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.sunrise.model.Appointment" %>
<%@ page import="com.sunrise.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    
    // Fallbacks if attributes are null
    Integer todayApptCount = (Integer) request.getAttribute("todayApptCount");
    Integer totalPatients = (Integer) request.getAttribute("totalPatients");
    BigDecimal totalRevenue = (BigDecimal) request.getAttribute("totalRevenue");
    if (todayApptCount == null) todayApptCount = 0;
    if (totalPatients == null) totalPatients = 0;
    if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Admin Dashboard - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
    /* Custom Teal Theme */
    .bg-teal { background-color: #20c997 !important; }
    .text-teal { color: #20c997 !important; }
    .btn-teal { background-color: #20c997; color: white; border: none; }
    .btn-teal:hover { background-color: #1aa179; color: white; }
    .card-teal-border { border-left: 5px solid #20c997; }
</style>
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
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 class="text-teal fw-bold">Admin Dashboard</h2>
                        <p class="text-muted mb-0">Overview & Analytics</p>
                    </div>
                    <div class="text-end">
                        <span class="badge bg-secondary p-2">Role: <%= user.getRole() %></span>
                        <span class="badge bg-teal p-2 ms-2">User: <%= user.getName() %></span>
                    </div>
                </div>
                
                <% 
                    String error = request.getParameter("error");
                    if ("unauthorized".equals(error)) {
                %>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <strong>Access Denied!</strong> You do not have permission to view administrative pages.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <% } %>
                
                <hr>

                <!-- Top 3 Panels -->
                <div class="row mb-5">
                    <div class="col-md-4 mb-3">
                        <div class="card card-teal-border shadow-sm h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs fw-bold text-teal text-uppercase mb-1">
                                            Today's Appointments
                                        </div>
                                        <div class="h3 mb-0 fw-bold text-dark"><%= todayApptCount %></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4 mb-3">
                        <div class="card card-teal-border shadow-sm h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs fw-bold text-teal text-uppercase mb-1">
                                            Total Patients Registered
                                        </div>
                                        <div class="h3 mb-0 fw-bold text-dark"><%= totalPatients %></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4 mb-3">
                        <div class="card card-teal-border shadow-sm h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs fw-bold text-teal text-uppercase mb-1">
                                            Total Revenue
                                        </div>
                                        <div class="h3 mb-0 fw-bold text-dark">LKR <%= String.format("%.2f", totalRevenue.doubleValue()) %></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <h4 class="text-secondary mb-3">Today's Appointment Schedule</h4>
                <div class="card shadow-sm">
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-striped table-hover align-middle mb-0">
                                <thead class="table-dark">
                                    <tr>
                                        <th>Appt No</th>
                                        <th>Patient Name</th>
                                        <th>Contact Number</th>
                                        <th>Dentist Name</th>
                                        <th>Treatment Type</th>
                                        <th>Scheduled Date & Time</th>
                                        <th class="text-center">Billing</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% 
                                        List<Appointment> list = (List<Appointment>) request.getAttribute("todayAppointments");
                                        if (list != null && !list.isEmpty()) {
                                            for (Appointment a : list) {
                                    %>
                                    <tr>
                                        <td><strong><%= a.getAppointmentNo() %></strong></td>
                                        <td><%= a.getPatientName() %></td>
                                        <td><%= a.getPatientContact() %></td>
                                        <td>Dr. <%= a.getDentistName() %></td>
                                        <td><%= a.getTreatmentName() %></td>
                                        <td><%= a.getAppointmentDate() %></td>
                                        <td class="text-center">
                                            <a href="BillServlet?appointmentNo=<%= a.getAppointmentNo() %>" class="btn btn-sm btn-teal">Process Bill</a>
                                        </td>
                                    </tr>
                                    <% 
                                            }
                                        } else {
                                    %>
                                    <tr>
                                        <td colspan="7" class="text-center text-muted py-4">No appointments scheduled for today.</td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
