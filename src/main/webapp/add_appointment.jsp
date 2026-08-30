<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.sunrise.model.Treatment" %>
<%@ page import="com.sunrise.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    List<Treatment> treatments = (List<Treatment>) request.getAttribute("treatments");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Register Appointment</title>
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
                <h2>Register New Appointment</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Register Appointment</li>
                    </ol>
                </nav>
                <hr>
                
                <% 
                    String error = request.getParameter("error");
                    if ("duplicate".equals(error)) {
                %>
                    <div class="alert alert-danger" role="alert">
                        <strong>Error:</strong> The Appointment Number already exists in the system. Please use a unique number.
                    </div>
                <% 
                    } else if ("validation".equals(error)) {
                %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> Please fill in all required fields with valid input.
                    </div>
                <% 
                    } else if ("date_invalid".equals(error)) {
                %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> Invalid appointment date.
                    </div>
                <%
                    }
                %>
                
                <div class="card shadow-sm">
                    <div class="card-body">
                        <form action="AppointmentServlet" method="post" id="apptForm" novalidate>
                            <input type="hidden" name="action" value="add">
                            
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="appointmentNo" class="form-label">Appointment No <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="appointmentNo" name="appointmentNo" required placeholder="e.g. APPT-1001">
                                    <div class="invalid-feedback">Please enter a unique appointment number.</div>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="patientName" class="form-label">Patient Name <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="patientName" name="patientName" required placeholder="e.g. John Doe">
                                    <div class="invalid-feedback">Patient name is required.</div>
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label for="address" class="form-label">Address</label>
                                <textarea class="form-control" id="address" name="address" rows="2" placeholder="Patient's residential address"></textarea>
                            </div>
                            
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="contactNumber" class="form-label">Contact Number <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="contactNumber" name="contactNumber" required 
                                           pattern="^\+?[0-9]{9,15}$" placeholder="e.g. 0771234567">
                                    <div class="invalid-feedback">Please enter a valid contact number (9 to 15 digits).</div>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="dentistName" class="form-label">Dentist Name <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="dentistName" name="dentistName" required placeholder="e.g. Dr. Silva">
                                    <div class="invalid-feedback">Dentist name is required.</div>
                                </div>
                            </div>
                            
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="treatmentId" class="form-label">Treatment Type <span class="text-danger">*</span></label>
                                    <select class="form-select" id="treatmentId" name="treatmentId" required>
                                        <option value="">-- Select Treatment --</option>
                                        <% 
                                            if (treatments != null) {
                                                for (Treatment t : treatments) {
                                        %>
                                            <option value="<%= t.getTreatmentId() %>"><%= t.getTreatmentName() %> ($<%= t.getCost() %>)</option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                    <div class="invalid-feedback">Please select a treatment type.</div>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="appointmentDate" class="form-label">Appointment Date & Time <span class="text-danger">*</span></label>
                                    <input type="datetime-local" class="form-control" id="appointmentDate" name="appointmentDate" required>
                                    <div class="invalid-feedback">Please choose a date and time.</div>
                                    <div id="date-validation-msg" class="text-danger small mt-1 d-none">Appointment date must be in the future.</div>
                                </div>
                            </div>
                            
                            <div class="mt-4">
                                <button type="submit" class="btn btn-success px-4">Register Appointment</button>
                                <button type="reset" class="btn btn-secondary px-4">Clear Form</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        // Form validations
        (() => {
            'use strict';
            const form = document.querySelector('#apptForm');
            const dateInput = document.getElementById('appointmentDate');
            const dateErrorMsg = document.getElementById('date-validation-msg');

            form.addEventListener('submit', event => {
                let isValid = true;
                
                // Validate if date is in the future
                const selectedDate = new Date(dateInput.value);
                const today = new Date();
                
                if (dateInput.value && selectedDate <= today) {
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
