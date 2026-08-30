<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.sunrise.model.Appointment" %>
<%@ page import="com.sunrise.model.Treatment" %>
<%@ page import="com.sunrise.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    List<Treatment> treatments = (List<Treatment>) request.getAttribute("treatments");
    Appointment editAppointment = (Appointment) request.getAttribute("appointment");
    boolean editMode = request.getAttribute("editMode") != null && (Boolean) request.getAttribute("editMode");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title><%= editMode ? "Edit Appointment" : "Register Appointment" %></title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-3 col-lg-2 p-0">
                <jsp:include page="includes/sidebar.jsp" />
            </div>

            <div class="col-md-9 col-lg-10 p-4">
                <h2><%= editMode ? "Edit Appointment" : "Register New Appointment" %></h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page"><%= editMode ? "Edit Appointment" : "Register Appointment" %></li>
                    </ol>
                </nav>
                <hr>

                <% if ("duplicate".equals(error)) { %>
                    <div class="alert alert-danger" role="alert">
                        <strong>Error:</strong> The appointment number already exists. Please use a unique number.
                    </div>
                <% } else if ("validation".equals(error)) { %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> Please provide valid patient, dentist, treatment, date, and status information.
                    </div>
                <% } else if ("date_invalid".equals(error)) { %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> Please choose a valid date and time in the future.
                    </div>
                <% } else if ("dentist_booked".equals(error)) { %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> This dentist is already booked at the selected date and time.
                    </div>
                <% } else if ("format".equals(error)) { %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> Appointment number must follow the format APT-2026-0001.
                    </div>
                <% } %>

                <div class="card shadow-sm">
                    <div class="card-body">
                        <form action="AppointmentServlet" method="post" id="apptForm" novalidate>
                            <input type="hidden" name="action" value="<%= editMode ? "update" : "add" %>">

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="appointmentNo" class="form-label">Appointment Number <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="appointmentNo" name="appointmentNo" value="<%= editMode && editAppointment != null ? editAppointment.getAppointmentNo() : "" %>" required placeholder="APT-2026-0001">
                                    <div class="invalid-feedback">Please enter a valid appointment number.</div>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="patientName" class="form-label">Patient Name <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="patientName" name="patientName" value="<%= editMode && editAppointment != null ? editAppointment.getPatientName() : "" %>" required placeholder="John Doe">
                                    <div class="invalid-feedback">Patient name is required.</div>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label for="address" class="form-label">Address</label>
                                <textarea class="form-control" id="address" name="address" rows="2" placeholder="Patient address"><%= editMode && editAppointment != null && editAppointment.getPatientName() != null ? "" : "" %></textarea>
                            </div>

                            <div class="row">
                                <div class="col-md-4 mb-3">
                                    <label for="contactNumber" class="form-label">Contact Number <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="contactNumber" name="contactNumber" value="<%= editMode && editAppointment != null ? editAppointment.getPatientContact() : "" %>" required pattern="^(?:\+94|0)\d{9,10}$" placeholder="0771234567">
                                    <div class="invalid-feedback">Please enter a valid Sri Lankan contact number.</div>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="dentistName" class="form-label">Dentist <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="dentistName" name="dentistName" value="<%= editMode && editAppointment != null ? editAppointment.getDentistName() : "" %>" required placeholder="Dr. Perera">
                                    <div class="invalid-feedback">Dentist is required.</div>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="status" class="form-label">Status</label>
                                    <select class="form-select" id="status" name="status">
                                        <option value="SCHEDULED" <%= (editMode && editAppointment != null && "SCHEDULED".equals(editAppointment.getStatus())) ? "selected" : "" %>>SCHEDULED</option>
                                        <option value="CONFIRMED" <%= (editMode && editAppointment != null && "CONFIRMED".equals(editAppointment.getStatus())) ? "selected" : "" %>>CONFIRMED</option>
                                        <option value="COMPLETED" <%= (editMode && editAppointment != null && "COMPLETED".equals(editAppointment.getStatus())) ? "selected" : "" %>>COMPLETED</option>
                                        <option value="CANCELLED" <%= (editMode && editAppointment != null && "CANCELLED".equals(editAppointment.getStatus())) ? "selected" : "" %>>CANCELLED</option>
                                        <option value="NO_SHOW" <%= (editMode && editAppointment != null && "NO_SHOW".equals(editAppointment.getStatus())) ? "selected" : "" %>>NO SHOW</option>
                                    </select>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="treatmentId" class="form-label">Treatment <span class="text-danger">*</span></label>
                                    <select class="form-select" id="treatmentId" name="treatmentId" required>
                                        <option value="">-- Select Treatment --</option>
                                        <% if (treatments != null) { for (Treatment t : treatments) { %>
                                            <option value="<%= t.getTreatmentId() %>" <%= (editMode && editAppointment != null && editAppointment.getTreatmentId() == t.getTreatmentId()) ? "selected" : "" %>><%= t.getTreatmentName() %> ($<%= t.getCost() %>)</option>
                                        <% } } %>
                                    </select>
                                    <div class="invalid-feedback">Please select a treatment.</div>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="appointmentDate" class="form-label">Appointment Date & Time <span class="text-danger">*</span></label>
                                    <input type="datetime-local" class="form-control" id="appointmentDate" name="appointmentDate" value="<%= editMode && editAppointment != null && editAppointment.getAppointmentDate() != null ? editAppointment.getAppointmentDate().toLocalDateTime().toString().substring(0, 16) : "" %>" required>
                                    <div class="invalid-feedback">Please choose a date and time.</div>
                                    <div id="date-validation-msg" class="text-danger small mt-1 d-none">Appointment date must be in the future.</div>
                                </div>
                            </div>

                            <div class="mt-4 d-flex gap-2">
                                <button type="submit" class="btn btn-success px-4"><%= editMode ? "Update Appointment" : "Register Appointment" %></button>
                                <a href="AppointmentServlet?action=list" class="btn btn-secondary px-4">Cancel</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        (() => {
            'use strict';
            const form = document.querySelector('#apptForm');
            const dateInput = document.getElementById('appointmentDate');
            const dateErrorMsg = document.getElementById('date-validation-msg');

            form.addEventListener('submit', event => {
                let isValid = true;
                const selectedDate = new Date(dateInput.value);
                const now = new Date();

                if (dateInput.value && selectedDate <= now) {
                    dateErrorMsg.classList.remove('d-none');
                    dateInput.classList.add('is-invalid');
                    isValid = false;
                } else {
                    dateErrorMsg.classList.add('d-none');
                    dateInput.classList.remove('is-invalid');
                }

                if (!form.checkValidity() || !isValid) {
                    event.preventDefault();
                    event.stopPropagation();
                }

                form.classList.add('was-validated');
            }, false);
        })();
    </script>
</body>
</html>
