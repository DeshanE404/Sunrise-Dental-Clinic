package com.sunrise.controller;

import com.sunrise.model.Appointment;
import com.sunrise.service.AppointmentService;
import com.sunrise.service.PatientService;
import com.sunrise.service.ReportService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {
    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Fetch dashboard metrics
        List<Appointment> todayAppointments = appointmentService.getTodayAppointments();
        int todayApptCount = todayAppointments.size();
        int totalPatients = patientService.getTotalPatientsCount();
        BigDecimal totalRevenue = reportService.getTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        request.setAttribute("todayAppointments", todayAppointments);
        request.setAttribute("todayApptCount", todayApptCount);
        request.setAttribute("totalPatients", totalPatients);
        request.setAttribute("totalRevenue", totalRevenue);

        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }
}
