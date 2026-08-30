package com.sunrise.controller;

import com.sunrise.model.User;
import com.sunrise.service.ReportService;
import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ReportServlet")
public class ReportServlet extends HttpServlet {
    private ReportService reportService = new ReportService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            response.sendRedirect("DashboardServlet?error=unauthorized");
            return;
        }

        double totalRevenue = reportService.getTotalRevenue();
        Map<String, Integer> treatmentStats = reportService.getTreatmentPopularity();
        Map<String, Integer> dentistStats = reportService.getDentistAppointmentsCount();

        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("treatmentStats", treatmentStats);
        request.setAttribute("dentistStats", dentistStats);

        request.getRequestDispatcher("reports.jsp").forward(request, response);
    }
}
