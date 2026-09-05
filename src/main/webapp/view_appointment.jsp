<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.sunrise.model.User" %>
<%@ page import="com.sunrise.model.Appointment" %>
<%@ page import="com.sunrise.model.Treatment" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Appointment appt = (Appointment) request.getAttribute("appointment");
    List<Appointment> appointments = (List<Appointment>) request.getAttribute("appointments");
    List<Treatment> appointmentTreatments = (List<Treatment>) request.getAttribute("appointmentTreatments");
    String error = request.getParameter("error");
    String success = request.getParameter("success");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Appointment Management - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
    .appt-row { cursor: pointer; }
    .appt-row.marked { background-color: #d1f2eb !important; }
    .appt-row.marked td:first-child { box-shadow: inset 4px 0 0 #20c997; }
    .search-result-row:hover { background-color: #f1f8f7; }
</style>
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <div class="sidebar-column col-md-3 col-lg-2 p-0">
                <jsp:include page="includes/sidebar.jsp" />
            </div>

            <div class="content-column col-md-9 col-lg-10 p-4">
                <h2>Appointment Management</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Appointments</li>
                    </ol>
                </nav>
                <hr>

                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                    <div class="btn-group">
                        <a href="AppointmentServlet?action=new" class="btn btn-success">+ New Appointment</a>
                        <button type="button" class="btn btn-outline-primary" data-bs-toggle="modal" data-bs-target="#apptSearchModal">
                            &#128269; Search Appointments
                        </button>
                    </div>

                    <form action="SearchAppointmentServlet" method="get" class="d-flex gap-2">
                        <input type="text" class="form-control" name="appointmentNo" placeholder="Search by appointment number" style="min-width: 260px;">
                        <button type="submit" class="btn btn-primary">Go</button>
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
                                        <tr>
                                            <th style="width: 150px;">Treatments:</th>
                                            <td>
                                                <%
                                                    if (appointmentTreatments != null && !appointmentTreatments.isEmpty()) {
                                                        for (Treatment tr : appointmentTreatments) {
                                                %>
                                                    <span class="d-block">&#10003; <%= tr.getTreatmentName() %> <span class="text-success">(Rs. <%= String.format("%,.2f", tr.getCost()) %>)</span></span>
                                                <%
                                                        }
                                                    } else if (appt.getTreatmentName() != null) {
                                                %>
                                                    <span class="d-block">&#10003; <%= appt.getTreatmentName() %> <span class="text-success">(Rs. <%= String.format("%,.2f", appt.getTreatmentCost()) %>)</span></span>
                                                <% } else { %>
                                                    <span class="text-muted">-</span>
                                                <% } %>
                                            </td>
                                        </tr>
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
    <!-- Search Appointments pop-up panel (admin & reception) -->
    <div class="modal fade" id="apptSearchModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-xl modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">&#128269; Search Appointments</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="row g-2 mb-3">
                        <div class="col-md-6">
                            <input type="text" id="apptSearchBox" class="form-control"
                                   placeholder="Search by number, patient, contact, doctor or treatment...">
                        </div>
                        <div class="col-md-3">
                            <select id="apptStatusFilter" class="form-select">
                                <option value="">All Statuses</option>
                                <option>SCHEDULED</option>
                                <option>CONFIRMED</option>
                                <option>COMPLETED</option>
                                <option>CANCELLED</option>
                                <option>NO_SHOW</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <button type="button" class="btn btn-outline-secondary w-100" id="clearApptMarksBtn">Clear Select Markers</button>
                        </div>
                    </div>
                    <div class="table-responsive border rounded" style="max-height: 55vh; overflow-y: auto;">
                        <table class="table table-sm table-striped table-hover align-middle mb-0">
                            <thead class="table-dark sticky-top">
                                <tr>
                                    <th style="width: 45px;" class="text-center">
                                        <input type="checkbox" class="form-check-input m-0" id="markAllVisible">
                                    </th>
                                    <th>Appointment No</th>
                                    <th>Patient</th>
                                    <th>Contact</th>
                                    <th>Doctor</th>
                                    <th>Treatment</th>
                                    <th>Date &amp; Time</th>
                                    <th>Status</th>
                                    <th style="width: 80px;"></th>
                                </tr>
                            </thead>
                            <tbody id="apptSearchResults"></tbody>
                        </table>
                    </div>
                    <div id="apptSearchEmpty" class="text-center text-muted py-4 d-none">No appointments found.</div>
                </div>
                <div class="modal-footer">
                    <span class="text-muted me-auto" id="apptMarkedCount">0 appointment(s) selected with markers</span>
                    <button type="button" class="btn btn-success" id="openMarkedBtn">Open Marked</button>
                </div>
            </div>
        </div>
    </div>

    <%!
        private String jsEsc(String value) {
            if (value == null) {
                return "";
            }
            return value.replace("\\", "\\\\").replace("\"", "\\\"")
                        .replace("\r", " ").replace("\n", " ");
        }
    %>
    <script>
        (() => {
            'use strict';
            const tbody = document.getElementById('apptSearchResults');
            const searchBox = document.getElementById('apptSearchBox');
            const statusFilter = document.getElementById('apptStatusFilter');
            const emptyMsg = document.getElementById('apptSearchEmpty');
            const markedCountEl = document.getElementById('apptMarkedCount');
            const markAllVisible = document.getElementById('markAllVisible');

            const allRows = [];
            <%
                if (appointments != null) {
                    for (Appointment a : appointments) {
            %>
            allRows.push({
                no: "<%= jsEsc(a.getAppointmentNo()) %>",
                patient: "<%= jsEsc(a.getPatientName()) %>",
                contact: "<%= jsEsc(a.getPatientContact()) %>",
                dentist: "<%= jsEsc(a.getDentistName()) %>",
                treatment: "<%= jsEsc(a.getTreatmentName()) %>",
                date: "<%= jsEsc(a.getAppointmentDate() == null ? "" : a.getAppointmentDate().toString()) %>",
                status: "<%= jsEsc(a.getStatus()) %>",
                marked: false
            });
            <%
                    }
                }
            %>

            function escapeHtml(value) {
                return String(value == null ? '' : value)
                    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
            }

            function badgeClass(status) {
                if (status === 'CANCELLED' || status === 'NO_SHOW') {
                    return 'bg-secondary';
                }
                return 'bg-success';
            }

            function updateMarkedCount() {
                const count = allRows.filter(r => r.marked).length;
                markedCountEl.textContent = count + ' appointment(s) selected with markers';
            }


            function renderResults() {
                const q = searchBox.value.trim().toLowerCase();
                const status = statusFilter.value;
                const filtered = allRows.filter(row => {
                    if (status && row.status !== status) {
                        return false;
                    }
                    if (!q) {
                        return true;
                    }
                    return (row.no + ' ' + row.patient + ' ' + row.contact + ' '
                            + row.dentist + ' ' + row.treatment + ' ' + row.date).toLowerCase().indexOf(q) !== -1;
                });

                tbody.innerHTML = '';
                filtered.forEach(row => {
                    const tr = document.createElement('tr');
                    tr.className = 'search-result-row' + (row.marked ? ' marked' : '');
                    tr.innerHTML =
                        '<td class="text-center"><input type="checkbox" class="form-check-input m-0 row-mark"' + (row.marked ? ' checked' : '') + '></td>' +
                        '<td><a href="SearchAppointmentServlet?appointmentNo=' + encodeURIComponent(row.no) + '"><strong>' + escapeHtml(row.no) + '</strong></a></td>' +
                        '<td>' + escapeHtml(row.patient) + '</td>' +
                        '<td>' + escapeHtml(row.contact) + '</td>' +
                        '<td>' + escapeHtml(row.dentist) + '</td>' +
                        '<td>' + escapeHtml(row.treatment) + '</td>' +
                        '<td>' + escapeHtml(row.date) + '</td>' +
                        '<td><span class="badge ' + badgeClass(row.status) + '">' + escapeHtml(row.status) + '</span></td>' +
                        '<td><a class="btn btn-outline-primary btn-sm" href="SearchAppointmentServlet?appointmentNo=' + encodeURIComponent(row.no) + '">View</a></td>';

                    const markInput = tr.querySelector('.row-mark');
                    markInput.addEventListener('change', () => {
                        row.marked = markInput.checked;
                        tr.classList.toggle('marked', row.marked);
                        updateMarkedCount();
                    });
                    tbody.appendChild(tr);
                });

                emptyMsg.classList.toggle('d-none', filtered.length > 0);
                markAllVisible.checked = filtered.length > 0 && filtered.every(r => r.marked);
            }

            searchBox.addEventListener('input', renderResults);
            statusFilter.addEventListener('change', renderResults);

            markAllVisible.addEventListener('change', () => {
                const q = searchBox.value.trim().toLowerCase();
                const status = statusFilter.value;
                allRows.forEach(row => {
                    const statusOk = !status || row.status === status;
                    const qOk = !q || (row.no + ' ' + row.patient + ' ' + row.contact + ' '
                            + row.dentist + ' ' + row.treatment).toLowerCase().indexOf(q) !== -1;
                    if (statusOk && qOk) {
                        row.marked = markAllVisible.checked;
                    }
                });
                renderResults();
                updateMarkedCount();
            });

            document.getElementById('clearApptMarksBtn').addEventListener('click', () => {
                allRows.forEach(row => { row.marked = false; });
                renderResults();
                updateMarkedCount();
            });

            document.getElementById('openMarkedBtn').addEventListener('click', () => {
                const first = allRows.find(r => r.marked);
                if (first) {
                    window.location.href = 'SearchAppointmentServlet?appointmentNo=' + encodeURIComponent(first.no);
                } else {
                    alert('Please put a select marker on at least one appointment first.');
                }
            });

            document.getElementById('apptSearchModal').addEventListener('shown.bs.modal', renderResults);
            renderResults();
        })();
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>

