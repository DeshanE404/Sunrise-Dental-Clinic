<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.sunrise.model.*" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
        response.sendRedirect("DashboardServlet?error=unauthorized");
        return;
    }

    LocalDate selectedDate = (LocalDate) request.getAttribute("selectedDate");
    LocalDate startDate = (LocalDate) request.getAttribute("startDate");
    LocalDate endDate = (LocalDate) request.getAttribute("endDate");
    DashboardSummary dashboardSummary = (DashboardSummary) request.getAttribute("dashboardSummary");
    List<DailyAppointmentReport> dailyAppointments = (List<DailyAppointmentReport>) request.getAttribute("dailyAppointments");
    List<DentistWorkloadReport> dentistWorkload = (List<DentistWorkloadReport>) request.getAttribute("dentistWorkload");
    List<TreatmentStatisticsReport> treatmentStats = (List<TreatmentStatisticsReport>) request.getAttribute("treatmentStats");
    List<RevenueReport> revenueReport = (List<RevenueReport>) request.getAttribute("revenueReport");
    BigDecimal totalRevenue = (BigDecimal) request.getAttribute("totalRevenue");

    if (selectedDate == null) selectedDate = LocalDate.now();
    if (startDate == null) startDate = LocalDate.now().minusDays(6);
    if (endDate == null) endDate = LocalDate.now();
    if (dashboardSummary == null) dashboardSummary = new DashboardSummary();
    if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Reports - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
    @media print {
        .no-print { display: none !important; }
        body { background: white; }
        .card { border: 1px solid #dee2e6 !important; box-shadow: none !important; }
    }
</style>
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-3 col-lg-2 p-0 no-print">
                <jsp:include page="includes/sidebar.jsp" />
            </div>

            <div class="col-md-9 col-lg-10 p-4">
                <div class="d-flex justify-content-between align-items-center mb-3 no-print">
                    <div>
                        <h2 class="mb-1">Reports Dashboard</h2>
                        <nav aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                                <li class="breadcrumb-item active" aria-current="page">Reports</li>
                            </ol>
                        </nav>
                    </div>
                    <button onclick="window.print()" class="btn btn-outline-primary">Print Report</button>
                </div>

                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-dark text-white">
                        <h5 class="mb-0">Report Filters</h5>
                    </div>
                    <div class="card-body no-print">
                        <form method="get" action="ReportServlet" class="row g-3 align-items-end">
                            <div class="col-md-4">
                                <label class="form-label">Daily Appointment Date</label>
                                <input type="date" class="form-control" name="selectedDate" value="<%= selectedDate %>">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">From Date</label>
                                <input type="date" class="form-control" name="startDate" value="<%= startDate %>">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">To Date</label>
                                <input type="date" class="form-control" name="endDate" value="<%= endDate %>">
                            </div>
                            <div class="col-md-2 d-flex gap-2">
                                <button type="submit" class="btn btn-primary flex-fill">Generate Report</button>
                                <a href="ReportServlet" class="btn btn-outline-secondary">Clear</a>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="row mb-4">
                    <div class="col-md-3 mb-3">
                        <div class="card border-primary h-100">
                            <div class="card-body">
                                <div class="text-muted text-uppercase small">Total Appointments</div>
                                <h3 class="mb-0"><%= dashboardSummary.getTotalAppointments() %></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3 mb-3">
                        <div class="card border-success h-100">
                            <div class="card-body">
                                <div class="text-muted text-uppercase small">Completed</div>
                                <h3 class="mb-0"><%= dashboardSummary.getCompletedAppointments() %></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3 mb-3">
                        <div class="card border-secondary h-100">
                            <div class="card-body">
                                <div class="text-muted text-uppercase small">Cancelled</div>
                                <h3 class="mb-0"><%= dashboardSummary.getCancelledAppointments() %></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3 mb-3">
                        <div class="card border-warning h-100">
                            <div class="card-body">
                                <div class="text-muted text-uppercase small">Revenue</div>
                                <h3 class="mb-0">$<%= String.format("%.2f", totalRevenue) %></h3>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-primary text-white">
                        <h5 class="mb-0">1. Daily Appointment Report - <%= selectedDate %></h5>
                    </div>
                    <div class="card-body p-0">
                        <% if (dailyAppointments != null && !dailyAppointments.isEmpty()) { %>
                            <div class="table-responsive">
                                <table class="table table-striped table-hover mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Appointment No</th>
                                            <th>Patient</th>
                                            <th>Dentist</th>
                                            <th>Treatment</th>
                                            <th>Time</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (DailyAppointmentReport row : dailyAppointments) { %>
                                            <tr>
                                                <td><%= row.getAppointmentNo() %></td>
                                                <td><%= row.getPatientName() %></td>
                                                <td><%= row.getDentistName() %></td>
                                                <td><%= row.getTreatmentName() != null ? row.getTreatmentName() : "-" %></td>
                                                <td><%= row.getAppointmentDateTime() %></td>
                                                <td>
                                                    <span class="badge bg-<%= "CANCELLED".equalsIgnoreCase(row.getStatus()) ? "secondary" : "success" %>"><%= row.getStatus() %></span>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } else { %>
                            <div class="alert alert-info m-3 mb-0">No appointments found for <%= selectedDate %>.</div>
                        <% } %>
                    </div>
                </div>

                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-info text-white">
                        <h5 class="mb-0">2. Dentist Workload Report</h5>
                    </div>
                    <div class="card-body p-0">
                        <% if (dentistWorkload != null && !dentistWorkload.isEmpty()) { %>
                            <div class="table-responsive">
                                <table class="table table-striped mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Dentist</th>
                                            <th>Total</th>
                                            <th>Completed</th>
                                            <th>Scheduled</th>
                                            <th>Cancelled</th>
                                            <th>No-show</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (DentistWorkloadReport row : dentistWorkload) { %>
                                            <tr>
                                                <td><%= row.getDentistName() %></td>
                                                <td><%= row.getTotalAppointments() %></td>
                                                <td><%= row.getCompletedAppointments() %></td>
                                                <td><%= row.getScheduledAppointments() %></td>
                                                <td><%= row.getCancelledAppointments() %></td>
                                                <td><%= row.getNoShowAppointments() %></td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } else { %>
                            <div class="alert alert-warning m-3 mb-0">No dentist workload data found for the selected range.</div>
                        <% } %>
                    </div>
                </div>

                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-success text-white">
                        <h5 class="mb-0">3. Treatment Statistics</h5>
                    </div>
                    <div class="card-body p-0">
                        <% if (treatmentStats != null && !treatmentStats.isEmpty()) { %>
                            <div class="table-responsive">
                                <table class="table table-striped mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Treatment</th>
                                            <th>Appointments</th>
                                            <th>Percentage</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (TreatmentStatisticsReport row : treatmentStats) { %>
                                            <tr>
                                                <td><%= row.getTreatmentName() %></td>
                                                <td><%= row.getAppointmentCount() %></td>
                                                <td><%= row.getPercentageOfTotal() != null ? row.getPercentageOfTotal() + "%" : "0.00%" %></td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } else { %>
                            <div class="alert alert-warning m-3 mb-0">No treatment statistics available for the selected range.</div>
                        <% } %>
                    </div>
                </div>

                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-warning text-dark">
                        <h5 class="mb-0">4. Revenue Report</h5>
                    </div>
                    <div class="card-body p-0">
                        <% if (revenueReport != null && !revenueReport.isEmpty()) { %>
                            <div class="table-responsive">
                                <table class="table table-striped mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Date</th>
                                            <th>Bills</th>
                                            <th>Treatment Revenue</th>
                                            <th>Consultation Revenue</th>
                                            <th>Total Revenue</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (RevenueReport row : revenueReport) { %>
                                            <tr>
                                                <td><%= row.getReportDate() %></td>
                                                <td><%= row.getBillCount() %></td>
                                                <td>$<%= String.format("%.2f", row.getTreatmentRevenue()) %></td>
                                                <td>$<%= String.format("%.2f", row.getConsultationRevenue()) %></td>
                                                <td>$<%= String.format("%.2f", row.getTotalRevenue()) %></td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } else { %>
                            <div class="alert alert-warning m-3 mb-0">No revenue records available for the selected range.</div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
