package com.sunrise.service;

import com.sunrise.dao.AppointmentDAO;
import com.sunrise.dao.DentistDAO;
import com.sunrise.model.Dentist;
import java.util.List;

public class DentistService {
    private final DentistDAO dentistDAO = new DentistDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    public List<Dentist> getAllDentists() {
        return dentistDAO.getAllDentists();
    }

    public Dentist getDentistById(int dentistId) {
        return dentistDAO.getDentistById(dentistId);
    }

    public boolean addDentist(String name, String specialization) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return dentistDAO.addDentist(name.trim(), specialization);
    }

    /**
     * Removes a dentist from the registered-doctor list. A dentist who already
     * has appointments recorded against their name cannot be removed because
     * those records must stay intact for history/billing.
     */
    public boolean deleteDentist(int dentistId) {
        Dentist dentist = dentistDAO.getDentistById(dentistId);
        if (dentist == null) {
            return false;
        }
        int appointmentCount = appointmentDAO.countAppointmentsByDentist(dentist.getDentistName());
        if (appointmentCount > 0) {
            return false;
        }
        return dentistDAO.deleteDentist(dentistId);
    }

    public boolean dentistHasAppointments(int dentistId) {
        Dentist dentist = dentistDAO.getDentistById(dentistId);
        if (dentist == null) {
            return false;
        }
        return appointmentDAO.countAppointmentsByDentist(dentist.getDentistName()) > 0;
    }
}