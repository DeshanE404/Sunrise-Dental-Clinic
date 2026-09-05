<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.sunrise.model.Appointment" %>
<%@ page import="com.sunrise.model.Treatment" %>
<%@ page import="com.sunrise.model.Dentist" %>
<%@ page import="com.sunrise.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    List<Treatment> treatments = (List<Treatment>) request.getAttribute("treatments");
    List<Dentist> dentists = (List<Dentist>) request.getAttribute("dentists");
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
<style>
    .treatment-item { padding: 8px 6px; border-bottom: 1px solid #eee; border-radius: 4px; cursor: pointer; }
    .treatment-item:hover { background-color: #f1f8f7; }
    .selected-chip { background-color: #20c997; color: white; border-radius: 20px; padding: 4px 12px;
                     display: inline-flex; align-items: center; gap: 6px; font-size: 0.85rem; }
    .selected-chip .remove-chip { cursor: pointer; font-weight: bold; opacity: .85; }
    .selected-chip .remove-chip:hover { opacity: 1; }
</style>
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <div class="sidebar-column col-md-3 col-lg-2 p-0">
                <jsp:include page="includes/sidebar.jsp" />
            </div>

            <div class="content-column col-md-9 col-lg-10 p-4">
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
                        <strong>Error:</strong> Please provide valid patient, doctor, treatment, date, and status information.
                    </div>
                <% } else if ("date_invalid".equals(error)) { %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> Please choose a valid date and time in the future.
                    </div>
                <% } else if ("dentist_booked".equals(error)) { %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> This doctor is already booked at the selected date and time.
                    </div>
                <% } else if ("treatment_invalid".equals(error)) { %>
                    <div class="alert alert-warning" role="alert">
                        <strong>Error:</strong> Please choose at least one treatment from the treatment list.
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
                                    <input type="text" class="form-control" id="appointmentNo" name="appointmentNo" value="<%= editMode && editAppointment != null ? editAppointment.getAppointmentNo() : request.getAttribute("nextAppointmentNumber") %>" <%= editMode ? "" : "readonly" %> required placeholder="APT-2026-0001">
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
                                <textarea class="form-control" id="address" name="address" rows="2" placeholder="Patient address"></textarea>
                            </div>


                            <div class="row">
                                <div class="col-md-4 mb-3">
                                    <label for="contactNumber" class="form-label">Contact Number <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="contactNumber" name="contactNumber" value="<%= editMode && editAppointment != null ? editAppointment.getPatientContact() : "" %>" required pattern="^(?:\+94|0)\d{9,10}$" placeholder="0771234567">
                                    <div class="invalid-feedback">Please enter a valid Sri Lankan contact number.</div>
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="dentistName" class="form-label">Doctor <span class="text-danger">*</span></label>
                                    <select class="form-select" id="dentistName" name="dentistName" required>
                                        <option value="">-- Select Doctor --</option>
                                        <%
                                            boolean dentistFoundInList = false;
                                            if (dentists != null) {
                                                for (Dentist d : dentists) {
                                                    boolean isSelected = editMode && editAppointment != null
                                                            && editAppointment.getDentistName() != null
                                                            && editAppointment.getDentistName().trim().equalsIgnoreCase(d.getDentistName());
                                                    if (isSelected) {
                                                        dentistFoundInList = true;
                                                    }
                                        %>
                                                    <option value="<%= d.getDentistName() %>" <%= isSelected ? "selected" : "" %>>
                                                        <%= d.getDentistName() %><%= d.getSpecialization() != null && !d.getSpecialization().trim().isEmpty() ? " (" + d.getSpecialization() + ")" : "" %>
                                                    </option>
                                        <%
                                                }
                                            }
                                            if (editMode && editAppointment != null && !dentistFoundInList
                                                    && editAppointment.getDentistName() != null && !editAppointment.getDentistName().trim().isEmpty()) {
                                        %>
                                                    <option value="<%= editAppointment.getDentistName() %>" selected>
                                                        <%= editAppointment.getDentistName() %> (no longer in the registered list)
                                                    </option>
                                        <% } %>
                                    </select>
                                    <div class="invalid-feedback">Please choose a registered doctor.</div>
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
                                    <label class="form-label">Treatments <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <button type="button" class="btn btn-outline-secondary text-start form-control" id="pickTreatmentsBtn"
                                                data-bs-toggle="modal" data-bs-target="#treatmentModal">
                                            &#128269; Search &amp; Select Treatments <span id="chosenCountBadge" class="badge bg-dark ms-2">0</span>
                                        </button>
                                    </div>
                                    <div id="selectedTreatments" class="mt-2 d-flex flex-wrap gap-2"></div>
                                    <div class="mt-1 small text-success fw-semibold d-none" id="treatTotalRow">
                                        Estimated total: <span id="treatTotal">Rs. 0</span>
                                    </div>
                                    <div class="invalid-feedback d-block" id="treatment-feedback" style="display:none;">Please tick at least one treatment.</div>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="appointmentDate" class="form-label">Appointment Date &amp; Time <span class="text-danger">*</span></label>
                                    <input type="datetime-local" class="form-control" id="appointmentDate" name="appointmentDate" value="<%= editMode && editAppointment != null && editAppointment.getAppointmentDate() != null ? editAppointment.getAppointmentDate().toLocalDateTime().toString().substring(0, 16) : "" %>" required>
                                    <div class="invalid-feedback">Please choose a date and time.</div>
                                    <div id="date-validation-msg" class="text-danger small mt-1 d-none">Appointment date must be in the future.</div>
                                </div>
                            </div>

                            <div class="mt-4 d-flex gap-2">
                                <button type="submit" class="btn btn-success px-4"><%= editMode ? "Update Appointment" : "Register Appointment" %></button>
                                <a href="SearchAppointmentServlet" class="btn btn-secondary px-4">Cancel</a>
                            </div>


                            <!-- Treatment picker modal (search + tick-marks for multiple treatments) -->
                            <div class="modal fade" id="treatmentModal" tabindex="-1" aria-hidden="true">
                                <div class="modal-dialog modal-lg modal-dialog-scrollable">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h5 class="modal-title">Search &amp; Select Treatments</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="mb-2">
                                                <input type="text" id="treatmentSearch" class="form-control" placeholder="Type to search a treatment name...">
                                            </div>
                                            <div class="border rounded p-1" style="max-height: 55vh; overflow-y: auto;">
                                                <%
                                                    if (treatments != null) {
                                                        for (Treatment t : treatments) {
                                                            boolean isTicked = editMode && editAppointment != null
                                                                    && editAppointment.getTreatmentIds() != null
                                                                    && editAppointment.getTreatmentIds().contains(t.getTreatmentId());
                                                %>
                                                <label class="treatment-item d-flex align-items-center gap-3 mb-0"
                                                       data-name="<%= t.getTreatmentName().toLowerCase() %>">
                                                    <input type="checkbox" class="form-check-input treatment-chk flex-shrink-0 m-0"
                                                           name="treatmentIds" value="<%= t.getTreatmentId() %>"
                                                           data-label="<%= t.getTreatmentName() %>" data-price="<%= t.getCost() %>"
                                                           <%= isTicked ? "checked" : "" %>>
                                                    <span class="flex-grow-1"><%= t.getTreatmentName() %></span>
                                                    <span class="text-nowrap text-success fw-semibold">Rs. <%= String.format("%,.0f", t.getCost()) %></span>
                                                </label>
                                                <%      }
                                                    }
                                                %>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <span class="text-muted me-auto">Tick one or more treatments, then press "Add Selected".</span>
                                            <span class="fw-bold text-success me-2" id="modalTotal"></span>
                                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                                            <button type="button" class="btn btn-success" data-bs-dismiss="modal" id="modalDoneBtn">Add Selected</button>
                                        </div>
                                    </div>
                                </div>
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
            const selectedBox = document.getElementById('selectedTreatments');
            const treatmentFeedback = document.getElementById('treatment-feedback');
            const countBadge = document.getElementById('chosenCountBadge');

            function escapeHtml(value) {
                return String(value == null ? '' : value)
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;');
            }

            function formatRupees(amount) {
                return 'Rs. ' + Number(amount).toLocaleString('en-US');
            }

            function refreshTreatmentSummary() {
                const checks = Array.from(document.querySelectorAll('.treatment-chk:checked'));
                selectedBox.innerHTML = '';
                checks.forEach(chk => {
                    const chip = document.createElement('span');
                    chip.className = 'selected-chip';
                    chip.innerHTML = '<span>' + escapeHtml(chk.dataset.label) + '</span>'
                        + '<span class="remove-chip" title="Remove">&times;</span>';
                    chip.querySelector('.remove-chip').addEventListener('click', () => {
                        chk.checked = false;
                        refreshTreatmentSummary();
                    });
                    selectedBox.appendChild(chip);
                });

                const count = checks.length;
                countBadge.textContent = count;
                const total = checks.reduce((sum, chk) => sum + parseFloat(chk.dataset.price || 0), 0);
                document.getElementById('treatTotal').textContent = formatRupees(total);
                document.getElementById('treatTotalRow').classList.toggle('d-none', count === 0);
                document.getElementById('modalTotal').textContent = count === 0 ? '' : formatRupees(total) + ' (' + count + ' selected)';
                treatmentFeedback.style.display = count === 0 ? 'block' : 'none';
            }

            document.querySelectorAll('.treatment-chk').forEach(chk => {
                chk.addEventListener('change', refreshTreatmentSummary);
            });

            const searchBox = document.getElementById('treatmentSearch');
            searchBox.addEventListener('input', () => {
                const q = searchBox.value.trim().toLowerCase();
                document.querySelectorAll('.treatment-item').forEach(item => {
                    item.style.display = (!q || (item.dataset.name || '').includes(q)) ? '' : 'none';
                });
            });

            form.addEventListener('submit', event => {
                let isValid = true;
                const hasTreatment = document.querySelectorAll('.treatment-chk:checked').length > 0;

                if (!hasTreatment) {
                    treatmentFeedback.style.display = 'block';
                    isValid = false;
                } else {
                    treatmentFeedback.style.display = 'none';
                }

                if (dateInput.value) {
                    const selectedDate = new Date(dateInput.value);
                    const now = new Date();
                    if (selectedDate <= now) {
                        dateErrorMsg.classList.remove('d-none');
                        dateInput.classList.add('is-invalid');
                        isValid = false;
                    } else {
                        dateErrorMsg.classList.add('d-none');
                        dateInput.classList.remove('is-invalid');
                    }
                }

                if (!form.checkValidity() || !isValid) {
                    event.preventDefault();
                    event.stopPropagation();
                }

                form.classList.add('was-validated');
            }, false);

            refreshTreatmentSummary();
        })();
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

