package com.sunrise.service;

import com.sunrise.dao.AppointmentDAO;
import com.sunrise.model.Appointment;
import java.util.List;

public class AppointmentService {
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    public boolean createAppointment(Appointment appt) {
        // Validation: check if appointment already exists
        if (appointmentDAO.getAppointmentByNo(appt.getAppointmentNo()) != null) {
            return false;
        }
        return appointmentDAO.registerAppointment(appt);
    }

    public Appointment getAppointmentDetails(String appointmentNo) {
        return appointmentDAO.getAppointmentByNo(appointmentNo);
    }

    public List<Appointment> getTodayAppointments() {
        return appointmentDAO.getTodayAppointments();
    }
}
