package com.sunrise.service;

import com.sunrise.dao.AppointmentDAO;
import com.sunrise.dao.TreatmentDAO;
import com.sunrise.model.Appointment;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

public class AppointmentService {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

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

        return appointmentDAO.registerAppointment(appt);
    }

    public boolean updateAppointment(Appointment appt) {
        String validationError = validateAppointment(appt);
        if (validationError != null) {
            return false;
        }

        if (!appointmentDAO.isDentistAvailable(appt.getDentistName(), appt.getAppointmentDate(), appt.getAppointmentNo())) {
            return false;
        }

        return appointmentDAO.updateAppointment(appt);
    }

    public boolean deleteAppointment(String appointmentNo) {
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return false;
        }
        return appointmentDAO.deleteAppointment(appointmentNo.trim());
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

        if (appt.getTreatmentId() <= 0 || treatmentDAO.getTreatmentById(appt.getTreatmentId()) == null) {
            return "Treatment not found";
        }

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

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    public List<Appointment> getTodayAppointments() {
        return appointmentDAO.getTodayAppointments();
    }
}
