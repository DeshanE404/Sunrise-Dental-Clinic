<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.sunrise.model.User" %>
<%@ page import="com.sunrise.model.Appointment" %>
<%@ page import="com.sunrise.model.Bill" %>
<%@ page import="com.sunrise.model.Treatment" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    Appointment appt = (Appointment) request.getAttribute("appointment");
    Bill bill = (Bill) request.getAttribute("bill");
    List<Treatment> appointmentTreatments = (List<Treatment>) request.getAttribute("appointmentTreatments");
    double lineTotal = 0.0;
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Patient Bill - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
    /* Printing styles to hide navigation and highlight receipt layout */
    @media print {
        .no-print {
            display: none !important;
        }
        body {
            background-color: white;
            font-size: 12pt;
        }
        .card {
            border: none !important;
            box-shadow: none !important;
        }
        .card-header {
            background-color: transparent !important;
            color: black !important;
            border-bottom: 2px solid #000 !important;
            text-align: center;
        }
    }
</style>
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <div class="sidebar-column col-md-3 col-lg-2 p-0 no-print">
                <jsp:include page="includes/sidebar.jsp" />
            </div>
            
            <!-- Main Content -->
            <div class="content-column col-md-9 col-lg-10 p-4">
                <div class="no-print d-flex justify-content-between align-items-center mb-3">
                    <h2>Calculate and Print Bill</h2>
                    <a href="SearchAppointmentServlet" class="btn btn-outline-secondary">Back to Search</a>
                </div>
                <hr class="no-print">
                
                <% if (appt != null && bill != null) { %>
                    <div class="card shadow mx-auto" style="max-width: 800px;">
                        <div class="card-header bg-primary text-white text-center py-4">
                            <h3 class="mb-1">Sunrise Dental Clinic</h3>
                            <p class="mb-0 small text-white-50">Colombo Road, Colombo, Sri Lanka | Tel: 011-2345678</p>
                        </div>
                        <div class="card-body p-4">
                            <div class="row mb-4">
                                <div class="col-6">
                                    <h6 class="text-muted">BILL TO:</h6>
                                    <h5><strong><%= appt.getPatientName() %></strong></h5>
                                    <p class="mb-0 text-muted">Contact: <%= appt.getPatientContact() %></p>
                                </div>
                                <div class="col-6 text-end">
                                    <h6 class="text-muted">INVOICE DETAILS:</h6>
                                    <h5><strong>Invoice #INV-<%= bill.getBillNo() %></strong></h5>
                                    <p class="mb-0 text-muted">Date: <%= bill.getBillingDate() %></p>
                                    <p class="mb-0 text-muted">Appt Ref: <%= appt.getAppointmentNo() %></p>
                                </div>
                            </div>
                            
                            <div class="row mb-4">
                                <div class="col-12">
                                    <table class="table table-borderless bg-light rounded p-3">
                                        <tr>
                                            <td><strong>Scheduled Dentist:</strong> Dr. <%= appt.getDentistName() %></td>
                                            <td class="text-end"><strong>Appt Date:</strong> <%= appt.getAppointmentDate() %></td>
                                        </tr>
                                    </table>
                                </div>
                            </div>

                            <table class="table table-bordered mt-2">
                                <thead class="table-dark text-center">
                                    <tr>
                                        <th>Description</th>
                                        <th style="width: 150px;">Unit Price (Rs.)</th>
                                        <th style="width: 150px;">Total (Rs.)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Registration &amp; Administration Fee</td>
                                        <td class="text-end"><%= String.format("%,.2f", bill.getConsultationFee()) %></td>
                                        <td class="text-end fw-semibold"><%= String.format("%,.2f", bill.getConsultationFee()) %></td>
                                    </tr>
                                    <%
                                        if (appointmentTreatments != null && !appointmentTreatments.isEmpty()) {
                                            for (Treatment t : appointmentTreatments) {
                                                lineTotal += t.getCost();
                                    %>
                                    <tr>
                                        <td>Dental Treatment: <%= t.getTreatmentName() %></td>
                                        <td class="text-end"><%= String.format("%,.2f", t.getCost()) %></td>
                                        <td class="text-end fw-semibold"><%= String.format("%,.2f", t.getCost()) %></td>
                                    </tr>
                                    <%
                                            }
                                        } else if (appt != null && appt.getTreatmentName() != null) {
                                    %>
                                    <tr>
                                        <td>Dental Treatment: <%= appt.getTreatmentName() %></td>
                                        <td class="text-end"><%= String.format("%,.2f", bill.getTreatmentCost()) %></td>
                                        <td class="text-end fw-semibold"><%= String.format("%,.2f", bill.getTreatmentCost()) %></td>
                                    </tr>
                                    <% } %>
                                    <tr class="table-active">
                                        <td colspan="2" class="text-end"><strong>Total Bill:</strong></td>
                                        <td class="text-end text-primary fs-5 fw-bold">Rs. <%= String.format("%,.2f", bill.getTotalBill()) %></td>
                                    </tr>
                                </tbody>
                            </table>
                            
                            <div class="mt-5 text-center text-muted small border-top pt-3">
                                <p>Thank you for choosing Sunrise Dental Clinic. We wish you a healthy smile!</p>
                                <p class="mb-0">Powered by Sunrise Dental Management System</p>
                            </div>
                        </div>
                        <div class="card-footer bg-light text-center py-3 no-print">
                            <button onclick="window.print()" class="btn btn-success btn-lg px-5">Print Patient Receipt</button>
                        </div>
                    </div>
                <% } else { %>
                    <div class="alert alert-danger" role="alert">
                        No billing information found for the requested appointment.
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</body>
</html>
