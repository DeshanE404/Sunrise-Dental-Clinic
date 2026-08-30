package com.sunrise.controller;

import com.sunrise.model.Appointment;
import com.sunrise.service.AppointmentService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SearchAppointmentServlet")
public class SearchAppointmentServlet extends HttpServlet {
    private AppointmentService appointmentService = new AppointmentService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String appointmentNo = request.getParameter("appointmentNo");
        
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            response.sendRedirect("view_appointment.jsp?error=empty");
            return;
        }

        Appointment appt = appointmentService.getAppointmentDetails(appointmentNo.trim());
        
        if (appt != null) {
            request.setAttribute("appointment", appt);
            request.getRequestDispatcher("view_appointment.jsp").forward(request, response);
        } else {
            response.sendRedirect("view_appointment.jsp?error=notfound");
        }
    }
}
