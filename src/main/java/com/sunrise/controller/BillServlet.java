package com.sunrise.controller;

import com.sunrise.model.Appointment;
import com.sunrise.model.Bill;
import com.sunrise.service.AppointmentService;
import com.sunrise.service.BillingService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/BillServlet")
public class BillServlet extends HttpServlet {
    private AppointmentService appointmentService = new AppointmentService();
    private BillingService billingService = new BillingService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String appointmentNo = request.getParameter("appointmentNo");
        
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            response.sendRedirect("view_appointment.jsp?error=invalid");
            return;
        }

        Appointment appt = appointmentService.getAppointmentDetails(appointmentNo.trim());
        if (appt == null) {
            response.sendRedirect("view_appointment.jsp?error=notfound");
            return;
        }

        // Generate bill using treatmentId from appointment
        Bill bill = billingService.generateAndSaveBill(appt.getAppointmentNo(), appt.getTreatmentId());
        
        if (bill != null) {
            request.setAttribute("appointment", appt);
            request.setAttribute("bill", bill);
            request.getRequestDispatcher("bill.jsp").forward(request, response);
        } else {
            response.sendRedirect("view_appointment.jsp?error=billingfailed");
        }
    }
}
