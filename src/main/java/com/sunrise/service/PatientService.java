package com.sunrise.service;

import com.sunrise.dao.PatientDAO;
import com.sunrise.model.Patient;

public class PatientService {
    private PatientDAO patientDAO = new PatientDAO();

    public int getOrCreatePatient(String name, String address, String contactNumber) {
        // If patient already exists, return id. Otherwise, register them.
        Patient existing = patientDAO.getPatientByNameAndContact(name, contactNumber);
        if (existing != null) {
            return existing.getPatientId();
        }
        
        Patient newPatient = new Patient();
        newPatient.setName(name);
        newPatient.setAddress(address);
        newPatient.setContactNumber(contactNumber);
        
        return patientDAO.registerPatient(newPatient);
    }
    
    public Patient getPatientById(int patientId) {
        return patientDAO.getPatientById(patientId);
    }

    public int getTotalPatientsCount() {
        return patientDAO.getTotalPatientsCount();
    }
}
