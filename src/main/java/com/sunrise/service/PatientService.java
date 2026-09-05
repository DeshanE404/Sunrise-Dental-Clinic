package com.sunrise.service;

import com.sunrise.dao.PatientDAO;
import com.sunrise.model.Patient;

public class PatientService {
    private PatientDAO patientDAO = new PatientDAO();

    public int getOrCreatePatient(String name, String address, String contactNumber) {
        return getOrCreatePatient(name, address, contactNumber, null);
    }

    /**
     * Returns the id of an existing patient matching the name and contact number,
     * or creates a new patient. When an email is supplied it is stored for new
     * patients and used to keep existing patient records up to date (email
     * notifications rely on this value).
     */
    public int getOrCreatePatient(String name, String address, String contactNumber, String email) {
        Patient existing = patientDAO.getPatientByNameAndContact(name, contactNumber);
        if (existing != null) {
            if (email != null && !email.trim().isEmpty()
                    && (existing.getEmail() == null || !existing.getEmail().equalsIgnoreCase(email.trim()))) {
                patientDAO.updateEmail(existing.getPatientId(), email.trim());
            }
            return existing.getPatientId();
        }

        Patient newPatient = new Patient();
        newPatient.setName(name);
        newPatient.setAddress(address);
        newPatient.setContactNumber(contactNumber);
        newPatient.setEmail(email == null ? null : email.trim());

        return patientDAO.registerPatient(newPatient);
    }
    
    public Patient getPatientById(int patientId) {
        return patientDAO.getPatientById(patientId);
    }

    public int getTotalPatientsCount() {
        return patientDAO.getTotalPatientsCount();
    }
}
