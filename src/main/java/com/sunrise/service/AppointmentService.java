package com.sunrise.service;

import com.sunrise.dao.AppointmentDAO;
import com.sunrise.dao.TreatmentDAO;
import com.sunrise.model.Appointment;
import com.sunrise.model.Treatment;
import com.sunrise.service.EmailService;
import com.sunrise.service.EmailServiceImpl;
import java.sql.Timestamp;
import java.time.Year;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppointmentService {
    private final AppointmentDAO appointmentDAO;
    private final TreatmentDAO treatmentDAO;
    private final EmailService emailService;

    public AppointmentService() {
        this(new AppointmentDAO(), new TreatmentDAO(), new EmailServiceImpl());
    }

    // Package-private constructor that allows tests to inject lightweight fakes.
    AppointmentService(AppointmentDAO appointmentDAO, TreatmentDAO treatmentDAO, EmailService emailService) {
        this.appointmentDAO = appointmentDAO;
        this.treatmentDAO = treatmentDAO;
        this.emailService = emailService;
    }

    public String getNextAppointmentNumber() {
        return appointmentDAO.getNextAppointmentNumber(Year.now().getValue());
    }

    public boolean createAppointment(Appointment appt) {
        String validationError = validateAppointment(appt);
        if (validationError != null) {
            return false;
        }

        if (appointmentDAO.appointmentExists(appt.getAppointmentNo())) {
            return false;
        }

        if (!appointmentDAO.isDentistAvailable(appt.getDentistName(), appt.getAppointmentDate(), null)) {
            return false;
        }

        boolean saved = appointmentDAO.registerAppointment(appt);
        if (saved) {
            sendConfirmationEmail(appt);
        }
        return saved;
    }

    public boolean updateAppointment(Appointment appt) {
        String validationError = validateAppointment(appt);
        if (validationError != null) {
            return false;
        }

        if (!appointmentDAO.isDentistAvailable(appt.getDentistName(), appt.getAppointmentDate(), appt.getAppointmentNo())) {
            return false;
        }

        boolean updated = appointmentDAO.updateAppointment(appt);
        if (updated) {
            sendUpdatedEmail(appt);
        }
        return updated;
    }

    public boolean deleteAppointment(String appointmentNo) {
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return false;
        }

        Appointment existing = getAppointmentDetails(appointmentNo.trim());
        boolean deleted = appointmentDAO.deleteAppointment(appointmentNo.trim());
        if (deleted && existing != null) {
            sendCancellationEmail(existing);
        }
        return deleted;
    }

    public String validateAppointment(Appointment appt) {
        if (appt == null) {
            return "Invalid appointment data";
        }

        String appointmentNo = appt.getAppointmentNo() == null ? "" : appt.getAppointmentNo().trim();
        if (appointmentNo.isEmpty()) {
            return "Appointment number is required";
        }
        if (!appointmentNo.matches("^APT-\\d{4}-\\d{4}$")) {
            return "Appointment number format is invalid";
        }

        if (appt.getPatientId() <= 0) {
            return "Patient must be selected";
        }

        if (appt.getDentistName() == null || appt.getDentistName().trim().isEmpty()) {
            return "Dentist name is required";
        }

        // Validate the treatment selection. An appointment may contain one or
        // more treatments. If the list is empty we fall back to the legacy
        // single treatmentId so the REST API keeps working unchanged.
        List<Integer> treatmentIds = appt.getTreatmentIds();
        if (treatmentIds == null || treatmentIds.isEmpty()) {
            if (appt.getTreatmentId() <= 0) {
                return "At least one treatment must be selected";
            }
            treatmentIds = Collections.singletonList(appt.getTreatmentId());
        }
        for (Integer id : treatmentIds) {
            if (id == null || id <= 0 || treatmentDAO.getTreatmentById(id) == null) {
                return "Treatment not found";
            }
        }
        appt.setTreatmentIds(new java.util.ArrayList<>(treatmentIds));
        appt.setTreatmentId(treatmentIds.get(0));

        if (appt.getAppointmentDate() == null) {
            return "Appointment date is required";
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (appt.getAppointmentDate().before(now)) {
            return "Appointment date must be in the future";
        }

        String status = appt.getStatus() == null ? "SCHEDULED" : appt.getStatus().trim().toUpperCase();
        List<String> validStatuses = Arrays.asList("SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW");
        if (!validStatuses.contains(status)) {
            return "Invalid appointment status";
        }
        appt.setStatus(status);

        return null;
    }

    public Appointment getAppointmentDetails(String appointmentNo) {
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return null;
        }
        return appointmentDAO.getAppointmentByNo(appointmentNo.trim());
    }

    public List<Treatment> getAppointmentTreatments(String appointmentNo) {
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return appointmentDAO.getTreatmentsForAppointment(appointmentNo.trim());
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    public List<Appointment> getTodayAppointments() {
        return appointmentDAO.getTodayAppointments();
    }

    private void sendConfirmationEmail(Appointment appt) {
        String patientEmail = getPatientEmail(appt.getPatientId());
        if (patientEmail == null || patientEmail.isBlank()) {
            return;
        }
        emailService.sendAppointmentConfirmation(appt, patientEmail);
    }

    private void sendUpdatedEmail(Appointment appt) {
        String patientEmail = getPatientEmail(appt.getPatientId());
        if (patientEmail == null || patientEmail.isBlank()) {
            return;
        }
        emailService.sendAppointmentUpdated(appt, patientEmail);
    }

    private void sendCancellationEmail(Appointment appt) {
        String patientEmail = getPatientEmail(appt.getPatientId());
        if (patientEmail == null || patientEmail.isBlank()) {
            return;
        }
        emailService.sendAppointmentCancelled(appt, patientEmail);
    }

    private String getPatientEmail(int patientId) {
        if (patientId <= 0) {
            return null;
        }
        com.sunrise.model.Patient patient = new com.sunrise.dao.PatientDAO().getPatientById(patientId);
        return patient != null ? patient.getEmail() : null;
    }
}
