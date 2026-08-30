package com.sunrise.controller;

import com.sunrise.model.Appointment;
import com.sunrise.model.Treatment;
import com.sunrise.dao.TreatmentDAO;
import com.sunrise.service.AppointmentService;
import com.sunrise.service.PatientService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/AppointmentServlet")
public class AppointmentServlet extends HttpServlet {
    private AppointmentService appointmentService = new AppointmentService();
    private PatientService patientService = new PatientService();
    private TreatmentDAO treatmentDAO = new TreatmentDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("new".equals(action)) {
            // Load treatments for registration page
            List<Treatment> treatments = treatmentDAO.getAllTreatments();
            request.setAttribute("treatments", treatments);
            request.getRequestDispatcher("add_appointment.jsp").forward(request, response);
        } else {
            // Default: redirect to dashboard
            response.sendRedirect("DashboardServlet");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("add".equals(action)) {
            String apptNo = request.getParameter("appointmentNo");
            String patientName = request.getParameter("patientName");
            String address = request.getParameter("address");
            String contactNumber = request.getParameter("contactNumber");
            String dentistName = request.getParameter("dentistName");
            String treatmentIdStr = request.getParameter("treatmentId");
            String dateTimeStr = request.getParameter("appointmentDate");
            
            // Server-side validation
            if (apptNo == null || apptNo.trim().isEmpty() ||
                patientName == null || patientName.trim().isEmpty() ||
                contactNumber == null || contactNumber.trim().isEmpty() ||
                dentistName == null || dentistName.trim().isEmpty() ||
                treatmentIdStr == null || dateTimeStr == null || dateTimeStr.isEmpty()) {
                
                response.sendRedirect("AppointmentServlet?action=new&error=validation");
                return;
            }

            int treatmentId = Integer.parseInt(treatmentIdStr);
            
            // Get or create patient
            int patientId = patientService.getOrCreatePatient(patientName.trim(), address.trim(), contactNumber.trim());
            
            if (patientId == -1) {
                response.sendRedirect("AppointmentServlet?action=new&error=patient_failed");
                return;
            }

            // Create Appointment
            Appointment appt = new Appointment();
            appt.setAppointmentNo(apptNo.trim());
            appt.setPatientId(patientId);
            appt.setDentistName(dentistName.trim());
            appt.setTreatmentId(treatmentId);
            
            try {
                // Parse "YYYY-MM-DDTHH:mm" to Timestamp
                LocalDateTime ldt = LocalDateTime.parse(dateTimeStr);
                appt.setAppointmentDate(Timestamp.valueOf(ldt));
            } catch (Exception e) {
                response.sendRedirect("AppointmentServlet?action=new&error=date_invalid");
                return;
            }

            boolean success = appointmentService.createAppointment(appt);
            
            if (success) {
                // Redirect to search or view page
                response.sendRedirect("SearchAppointmentServlet?appointmentNo=" + appt.getAppointmentNo());
            } else {
                response.sendRedirect("AppointmentServlet?action=new&error=duplicate");
            }
        }
    }
}
