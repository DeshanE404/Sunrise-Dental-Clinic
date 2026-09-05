package com.sunrise.controller;

import com.sunrise.dao.TreatmentDAO;
import com.sunrise.model.Appointment;
import com.sunrise.model.Treatment;
import com.sunrise.service.AppointmentService;
import com.sunrise.service.DentistService;
import com.sunrise.service.PatientService;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/AppointmentServlet")
public class AppointmentServlet extends HttpServlet {
    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final DentistService dentistService = new DentistService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("new".equals(action)) {
            List<Treatment> treatments = treatmentDAO.getAllTreatments();
            request.setAttribute("treatments", treatments);
            request.setAttribute("dentists", dentistService.getAllDentists());
            request.setAttribute("nextAppointmentNumber", appointmentService.getNextAppointmentNumber());
            request.getRequestDispatcher("add_appointment.jsp").forward(request, response);
            return;
        }

        if ("list".equals(action)) {
            List<Appointment> appointments = appointmentService.getAllAppointments();
            request.setAttribute("appointments", appointments);
            request.getRequestDispatcher("view_appointment.jsp").forward(request, response);
            return;
        }

        if ("edit".equals(action)) {
            String appointmentNo = request.getParameter("appointmentNo");
            Appointment appointment = appointmentService.getAppointmentDetails(appointmentNo);
            if (appointment == null) {
                response.sendRedirect("SearchAppointmentServlet?error=notfound");
                return;
            }

            request.setAttribute("appointment", appointment);
            request.setAttribute("treatments", treatmentDAO.getAllTreatments());
            request.setAttribute("dentists", dentistService.getAllDentists());
            request.setAttribute("editMode", true);
            request.getRequestDispatcher("add_appointment.jsp").forward(request, response);
            return;
        }

        if ("delete".equals(action)) {
            String appointmentNo = request.getParameter("appointmentNo");
            boolean deleted = appointmentService.deleteAppointment(appointmentNo);
            if (deleted) {
                response.sendRedirect("AppointmentServlet?action=list&success=deleted");
            } else {
                response.sendRedirect("AppointmentServlet?action=list&error=delete_failed");
            }
            return;
        }

        response.sendRedirect("DashboardServlet");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("add".equals(action) || "create".equals(action)) {
            createAppointment(request, response);
            return;
        }

        if ("update".equals(action)) {
            updateAppointment(request, response);
            return;
        }

        if ("delete".equals(action)) {
            String appointmentNo = request.getParameter("appointmentNo");
            if (appointmentService.deleteAppointment(appointmentNo)) {
                response.sendRedirect("AppointmentServlet?action=list&success=deleted");
            } else {
                response.sendRedirect("AppointmentServlet?action=list&error=delete_failed");
            }
            return;
        }

        response.sendRedirect("DashboardServlet");
    }

    private void createAppointment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String apptNo = appointmentService.getNextAppointmentNumber();
        String patientName = request.getParameter("patientName");
        String address = request.getParameter("address");
        String contactNumber = request.getParameter("contactNumber");
        String dentistName = request.getParameter("dentistName");
        String dateTimeStr = request.getParameter("appointmentDate");
        String status = request.getParameter("status");

        if (apptNo == null || patientName == null || contactNumber == null || dentistName == null || dateTimeStr == null || dateTimeStr.isEmpty()) {
            response.sendRedirect("AppointmentServlet?action=new&error=validation");
            return;
        }

        if (patientName.trim().isEmpty() || dentistName.trim().isEmpty() || contactNumber.trim().isEmpty()) {
            response.sendRedirect("AppointmentServlet?action=new&error=validation");
            return;
        }

        try {
            List<Integer> treatmentIds = parseTreatmentIds(request);
            if (treatmentIds.isEmpty()) {
                response.sendRedirect("AppointmentServlet?action=new&error=treatment_invalid");
                return;
            }

            int patientId = patientService.getOrCreatePatient(patientName.trim(), address == null ? "" : address.trim(), contactNumber.trim());

            if (patientId == -1) {
                response.sendRedirect("AppointmentServlet?action=new&error=patient_failed");
                return;
            }

            Appointment appointment = new Appointment();
            appointment.setAppointmentNo(apptNo.trim());
            appointment.setPatientId(patientId);
            appointment.setDentistName(dentistName.trim());
            appointment.setTreatmentIds(treatmentIds);
            appointment.setAppointmentDate(Timestamp.valueOf(LocalDateTime.parse(dateTimeStr)));
            appointment.setStatus(status == null || status.trim().isEmpty() ? "SCHEDULED" : status.trim().toUpperCase());

            String validationError = appointmentService.validateAppointment(appointment);
            if (validationError != null) {
                if (validationError.contains("Appointment number")) {
                    response.sendRedirect("AppointmentServlet?action=new&error=format");
                } else if (validationError.contains("date")) {
                    response.sendRedirect("AppointmentServlet?action=new&error=date_invalid");
                } else if (validationError.contains("Dentist") || validationError.contains("booked")) {
                    response.sendRedirect("AppointmentServlet?action=new&error=dentist_booked");
                } else if (validationError.contains("Treatment")) {
                    response.sendRedirect("AppointmentServlet?action=new&error=treatment_invalid");
                } else {
                    response.sendRedirect("AppointmentServlet?action=new&error=validation");
                }
                return;
            }

            if (appointmentService.getAppointmentDetails(appointment.getAppointmentNo()) != null) {
                response.sendRedirect("AppointmentServlet?action=new&error=duplicate");
                return;
            }

            if (!appointmentService.createAppointment(appointment)) {
                response.sendRedirect("AppointmentServlet?action=new&error=dentist_booked");
                return;
            }

            response.sendRedirect("SearchAppointmentServlet?appointmentNo=" + appointment.getAppointmentNo());
        } catch (Exception e) {
            response.sendRedirect("AppointmentServlet?action=new&error=validation");
        }
    }

    private void updateAppointment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String apptNo = request.getParameter("appointmentNo");
        String patientName = request.getParameter("patientName");
        String address = request.getParameter("address");
        String contactNumber = request.getParameter("contactNumber");
        String dentistName = request.getParameter("dentistName");
        String dateTimeStr = request.getParameter("appointmentDate");
        String status = request.getParameter("status");

        if (apptNo == null || patientName == null || contactNumber == null || dentistName == null || dateTimeStr == null || dateTimeStr.isEmpty()) {
            response.sendRedirect("AppointmentServlet?action=list&error=validation");
            return;
        }

        try {
            List<Integer> treatmentIds = parseTreatmentIds(request);
            if (treatmentIds.isEmpty()) {
                response.sendRedirect("AppointmentServlet?action=edit&appointmentNo=" + apptNo + "&error=treatment_invalid");
                return;
            }

            int patientId = patientService.getOrCreatePatient(patientName.trim(), address == null ? "" : address.trim(), contactNumber.trim());

            if (patientId == -1) {
                response.sendRedirect("AppointmentServlet?action=edit&appointmentNo=" + apptNo + "&error=patient_failed");
                return;
            }

            Appointment appointment = new Appointment();
            appointment.setAppointmentNo(apptNo.trim());
            appointment.setPatientId(patientId);
            appointment.setDentistName(dentistName.trim());
            appointment.setTreatmentIds(treatmentIds);
            appointment.setAppointmentDate(Timestamp.valueOf(LocalDateTime.parse(dateTimeStr)));
            appointment.setStatus(status == null || status.trim().isEmpty() ? "SCHEDULED" : status.trim().toUpperCase());

            String validationError = appointmentService.validateAppointment(appointment);
            if (validationError != null) {
                response.sendRedirect("AppointmentServlet?action=edit&appointmentNo=" + apptNo + "&error=validation");
                return;
            }

            if (!appointmentService.updateAppointment(appointment)) {
                response.sendRedirect("AppointmentServlet?action=edit&appointmentNo=" + apptNo + "&error=dentist_booked");
                return;
            }

            response.sendRedirect("SearchAppointmentServlet?appointmentNo=" + appointment.getAppointmentNo());
        } catch (Exception e) {
            response.sendRedirect("AppointmentServlet?action=edit&appointmentNo=" + apptNo + "&error=validation");
        }
    }

    private List<Integer> parseTreatmentIds(HttpServletRequest request) {
        List<Integer> ids = new ArrayList<>();
        String[] raw = request.getParameterValues("treatmentIds");
        if (raw != null) {
            for (String value : raw) {
                try {
                    int id = Integer.parseInt(value);
                    if (id > 0 && !ids.contains(id)) {
                        ids.add(id);
                    }
                } catch (NumberFormatException ignored) {
                    // skip invalid values
                }
            }
        }
        return ids;
    }
}
