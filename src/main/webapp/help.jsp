<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.sunrise.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Help & Instructions - Sunrise Dental Clinic</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
    .help-step { margin-bottom: 6px; }
    .role-badge-admin { background-color: #dc3545; color: #fff; font-size: 0.75rem; padding: 2px 8px; border-radius: 10px; }
</style>
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <div class="sidebar-column col-md-3 col-lg-2 p-0">
                <jsp:include page="includes/sidebar.jsp" />
            </div>
            <div class="content-column col-md-9 col-lg-10 p-4">
                <h2>Help &amp; Documentation</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Help</li>
                    </ol>
                </nav>
                <hr>

                <div class="alert alert-info">
                    <strong>You are logged in as:</strong> <%= user.getName() %> (<%= user.getRole() %>)
                    &mdash; items marked <span class="role-badge-admin">ADMIN</span> are only visible to administrators.
                </div>

                <div class="row">
                    <div class="col-lg-8">
                        <div class="card mb-4 shadow-sm">
                            <div class="card-header bg-primary text-white"><h5 class="mb-0">Step-by-Step Staff Instructions</h5></div>
                            <div class="card-body">
                                <div class="accordion" id="helpAccordion">

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hLogin">
                                            <button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#cLogin" aria-expanded="true" aria-controls="cLogin">
                                                1. Login, Remember Me &amp; Dashboard
                                            </button>
                                        </h2>
                                        <div id="cLogin" class="accordion-collapse collapse show" aria-labelledby="hLogin" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>Log in with your clinic email and password.</li>
                                                    <li>Tick <strong>Remember me</strong> to stay logged in for 30 days &mdash; even after the server restarts.</li>
                                                    <li>The session times out after a period of inactivity; simply log in again.</li>
                                                    <li>The <strong>Dashboard</strong> shows today's appointments, total patients and revenue.</li>
                                                    <li>Admin accounts see extra menu items: <em>Register Doctor</em>, <em>Manage Users</em> and <em>Reports</em>.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hAppt">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cAppt" aria-expanded="false" aria-controls="cAppt">
                                                2. Registering an Appointment
                                            </button>
                                        </h2>
                                        <div id="cAppt" class="accordion-collapse collapse" aria-labelledby="hAppt" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>Choose <strong>Register Appointment</strong> from the sidebar. The appointment number is created automatically.</li>
                                                    <li>Enter the patient name, contact number and an <strong>optional email</strong> (used for appointment notifications). If the patient already exists, the record is reused.</li>
                                                    <li>Pick the <strong>Doctor</strong> from the dropdown of registered doctors.</li>
                                                    <li>Click <strong>Search &amp; Select Treatments</strong> to open the treatment panel; use the search box and tick one or more treatments, then press <em>Add Selected</em>. The estimated total updates live.</li>
                                                    <li>Choose a future date and time, then press <strong>Register Appointment</strong>.</li>
                                                    <li>The system prevents double-booking the same doctor at the same time.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hSearch">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cSearch" aria-expanded="false" aria-controls="cSearch">
                                                3. Searching Appointments
                                            </button>
                                        </h2>
                                        <div id="cSearch" class="accordion-collapse collapse" aria-labelledby="hSearch" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>Open <strong>Search Appointment</strong> from the sidebar to see all appointments.</li>
                                                    <li>Use the search box to find an appointment by number.</li>
                                                    <li>Click <strong>&#128269; Search Appointments</strong> for the popup panel: type a name, contact, doctor or treatment to filter.</li>
                                                    <li>Use the tick boxes as <strong>select markers</strong>, then press <em>Open Marked</em> to view the first marked appointment, or use the <em>View</em> button on any row.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hEdit">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cEdit" aria-expanded="false" aria-controls="cEdit">
                                                4. Editing or Cancelling an Appointment
                                            </button>
                                        </h2>
                                        <div id="cEdit" class="accordion-collapse collapse" aria-labelledby="hEdit" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>From the search results click <strong>Edit</strong> to change the patient, doctor, treatments, date or status.</li>
                                                    <li>Click <strong>Delete</strong> to cancel an appointment. Cancelled appointments are kept for history and an audit entry is recorded.</li>
                                                    <li>If the patient provided an email, update and cancellation notifications are sent automatically.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hBill">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cBill" aria-expanded="false" aria-controls="cBill">
                                                5. Generating and Printing a Bill
                                            </button>
                                        </h2>
                                        <div id="cBill" class="accordion-collapse collapse" aria-labelledby="hBill" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>Open an appointment and click <strong>Calculate and Print Bill</strong>.</li>
                                                    <li>The bill lists the registration fee plus every selected treatment in LKR (Rs.).</li>
                                                    <li>Only one bill is created per appointment; clicking again re-opens the same bill.</li>
                                                    <li>Press <strong>Print Patient Receipt</strong> to generate a paper copy.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hDentist">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cDentist" aria-expanded="false" aria-controls="cDentist">
                                                6. Registering / Removing Doctors <span class="role-badge-admin">ADMIN</span>
                                            </button>
                                        </h2>
                                        <div id="cDentist" class="accordion-collapse collapse" aria-labelledby="hDentist" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>Open <strong>Register Doctor</strong> and enter the doctor name and specialization, then save.</li>
                                                    <li>The doctor immediately becomes available in the doctor dropdown when booking appointments.</li>
                                                    <li>Use <strong>Remove</strong> to delete a doctor. A doctor with existing appointments cannot be removed.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hUser">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cUser" aria-expanded="false" aria-controls="cUser">
                                                7. Managing Users <span class="role-badge-admin">ADMIN</span>
                                            </button>
                                        </h2>
                                        <div id="cUser" class="accordion-collapse collapse" aria-labelledby="hUser" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>Open <strong>Manage Users</strong> to create Admin or Reception accounts.</li>
                                                    <li>Email and employee number must be unique.</li>
                                                    <li>Use <strong>Remove</strong> to delete a staff account. You cannot remove your own account, and the last admin cannot be removed.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hReports">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cReports" aria-expanded="false" aria-controls="cReports">
                                                8. Reports for Decision Making <span class="role-badge-admin">ADMIN</span>
                                            </button>
                                        </h2>
                                        <div id="cReports" class="accordion-collapse collapse" aria-labelledby="hReports" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>Open <strong>Reports</strong>, set the date range, then press <em>Generate Report</em>.</li>
                                                    <li>Review the daily appointments, dentist workload, treatment statistics and revenue sections.</li>
                                                    <li>Use <strong>Print Report</strong> for a paper copy.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hEmail">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cEmail" aria-expanded="false" aria-controls="cEmail">
                                                9. Email Notifications
                                            </button>
                                        </h2>
                                        <div id="cEmail" class="accordion-collapse collapse" aria-labelledby="hEmail" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>When a patient email is entered at registration, a confirmation email is sent automatically.</li>
                                                    <li>Editing and cancelling appointments sends update or cancellation notices to the same address.</li>
                                                    <li>If no email is provided, no notification is attempted and the appointment is still saved.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="hLogout">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#cLogout" aria-expanded="false" aria-controls="cLogout">
                                                10. Logout &amp; Security
                                            </button>
                                        </h2>
                                        <div id="cLogout" class="accordion-collapse collapse" aria-labelledby="hLogout" data-bs-parent="#helpAccordion">
                                            <div class="accordion-body">
                                                <ul>
                                                    <li>Click <strong>Logout</strong> at the bottom of the sidebar when you leave the workstation.</li>
                                                    <li>Logging out deletes the remember-me token and invalidates your session, so the browser back button cannot return to protected pages.</li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-4">
                        <div class="card shadow-sm">
                            <div class="card-header bg-dark text-white"><h5 class="mb-0">Quick Tips</h5></div>
                            <div class="card-body">
                                <ul class="small mb-0">
                                    <li class="help-step">Appointment numbers are generated automatically.</li>
                                    <li class="help-step">You can add several treatments to one appointment.</li>
                                    <li class="help-step">Future dates only &mdash; past bookings are rejected.</li>
                                    <li class="help-step">One bill per appointment.</li>
                                    <li class="help-step">All changes to appointments are audited in the database.</li>
                                    <li class="help-step">Tick Remember Me if you use the same workstation daily.</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
