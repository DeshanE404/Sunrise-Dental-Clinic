package com.sunrise.controller;

import com.sunrise.model.DailyAppointmentReport;
import com.sunrise.model.DashboardSummary;
import com.sunrise.model.DentistWorkloadReport;
import com.sunrise.model.RevenueReport;
import com.sunrise.model.TreatmentStatisticsReport;
import com.sunrise.model.User;
import com.sunrise.service.ReportService;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ReportServlet")
public class ReportServlet extends HttpServlet {
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request, response)) {
            return;
        }

        LocalDate selectedDate = parseDate(request.getParameter("selectedDate"));
        LocalDate startDate = parseDate(request.getParameter("startDate"));
        LocalDate endDate = parseDate(request.getParameter("endDate"));

        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today.minusDays(6);
            endDate = today;
        }

        // Default summary for current day
        DashboardSummary dashboardSummary = reportService.getDashboardSummary(selectedDate);
        List<DailyAppointmentReport> dailyAppointments = reportService.getDailyAppointments(selectedDate);
        List<DentistWorkloadReport> dentistWorkload = reportService.getDentistWorkload(startDate, endDate);
        List<TreatmentStatisticsReport> treatmentStats = reportService.getTreatmentStatistics(startDate, endDate);
        List<RevenueReport> revenueReport = reportService.getRevenueReport(startDate, endDate);
        BigDecimal totalRevenue = reportService.getTotalRevenue();

        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("dashboardSummary", dashboardSummary);
        request.setAttribute("dailyAppointments", dailyAppointments);
        request.setAttribute("dentistWorkload", dentistWorkload);
        request.setAttribute("treatmentStats", treatmentStats);
        request.setAttribute("revenueReport", revenueReport);
        request.setAttribute("totalRevenue", totalRevenue);

        request.getRequestDispatcher("reports.jsp").forward(request, response);
    }

    private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return false;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect("DashboardServlet?error=unauthorized");
            return false;
        }
        return true;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
