<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.sunrise.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Help & Instructions</title>
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
                <h2>Help & Documentation Section</h2>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="DashboardServlet">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Help Section</li>
                    </ol>
                </nav>
                <hr>
                
                <div class="row">
                    <div class="col-lg-8">
                        <div class="card mb-4 shadow-sm">
                            <div class="card-header bg-primary text-white">
                                <h5 class="mb-0">Step-by-Step Staff Instructions</h5>
                            </div>
                            <div class="card-body">
                                <div class="accordion" id="instructionsAccordion">
                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="headingOne">
                                            <button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
                                                1. Login & Dashboard Overview
                                            </button>
                                        </h2>
                                        <div id="collapseOne" class="accordion-collapse collapse show" aria-labelledby="headingOne" data-bs-parent="#instructionsAccordion">
                                            <div class="accordion-body">
                                                - Log in using your clinic credentials. 
                                                - <strong>Admin:</strong> Full access to dashboard, registration, billing, search, and reports.
                                                - <strong>Reception:</strong> Access to dashboard, registration, and billing search. Cannot view reports.
                                                - The dashboard displays <strong>today's appointments</strong> filtered dynamically from the database.
                                            </div>
                                        </div>
                                    </div>
                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="headingTwo">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseTwo" aria-expanded="false" aria-controls="collapseTwo">
                                                2. Registering a New Appointment & Patient
                                            </button>
                                        </h2>
                                        <div id="collapseTwo" class="accordion-collapse collapse" aria-labelledby="headingTwo" data-bs-parent="#instructionsAccordion">
                                            <div class="accordion-body">
                                                - Select <strong>Register Appointment</strong> from the sidebar.
                                                - Enter a unique **Appointment No** (the system restricts duplicate entries).
                                                - Enter patient personal details. If they are an existing patient (same name & contact number), the system associates them automatically. Otherwise, it creates a new patient profile.
                                                - Choose the dentist, treatment type, and scheduled time.
                                                - Submit the form.
                                            </div>
                                        </div>
                                    </div>
                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="headingThree">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseThree" aria-expanded="false" aria-controls="collapseThree">
                                                3. Searching and Generating Patient Bills
                                            </button>
                                        </h2>
                                        <div id="collapseThree" class="accordion-collapse collapse" aria-labelledby="headingThree" data-bs-parent="#instructionsAccordion">
                                            <div class="accordion-body">
                                                - Click **Search & Billing** in the sidebar.
                                                - Enter the **Appointment Number** and click Search.
                                                - The patient's complete file will be displayed.
                                                - Click **Calculate and Print Bill**.
                                                - The system will calculate the total cost (consultation fee + treatment cost) and display a print-friendly invoice. Click **Print Patient Receipt** to generate a paper receipt.
                                            </div>
                                        </div>
                                    </div>
                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="headingFour">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseFour" aria-expanded="false" aria-controls="collapseFour">
                                                4. Secure Logout / Exiting System
                                            </button>
                                        </h2>
                                        <div id="collapseFour" class="accordion-collapse collapse" aria-labelledby="headingFour" data-bs-parent="#instructionsAccordion">
                                            <div class="accordion-body">
                                                - To exit the application safely, click the **Logout** button at the bottom of the sidebar. This invalidates your current session and prevents unauthorized access.
                                            </div>
                                        </div>
                                    </div>
                                </div>
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
