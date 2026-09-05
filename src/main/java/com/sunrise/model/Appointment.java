package com.sunrise.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    private String appointmentNo;
    private int patientId;
    private String dentistName;
    private int treatmentId;
    private Timestamp appointmentDate;
    private String status = "SCHEDULED";

    // Virtual fields to simplify display
    private String patientName;
    private String patientContact;
    private String treatmentName;
    private double treatmentCost;

    // Every treatment registered for this appointment. An appointment can now
    // hold multiple treatments. treatmentId (above) keeps the primary/first one
    // so the REST API and legacy code remain compatible.
    private List<Integer> treatmentIds = new ArrayList<>();

    public Appointment() {
        // Default constructor required for JSP/JSON binding and object creation.
    }

    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }

    public String getAppointmentNumber() { return getAppointmentNo(); }
    public void setAppointmentNumber(String appointmentNo) { setAppointmentNo(appointmentNo); }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public int getTreatmentId() { return treatmentId; }
    public void setTreatmentId(int treatmentId) { this.treatmentId = treatmentId; }

    public List<Integer> getTreatmentIds() { return treatmentIds; }
    public void setTreatmentIds(List<Integer> treatmentIds) {
        this.treatmentIds = treatmentIds == null ? new ArrayList<>() : treatmentIds;
    }
    public void addTreatmentId(int treatmentId) {
        if (treatmentIds == null) {
            treatmentIds = new ArrayList<>();
        }
        if (!treatmentIds.contains(treatmentId)) {
            treatmentIds.add(treatmentId);
        }
    }

    public Timestamp getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Timestamp appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientContact() { return patientContact; }
    public void setPatientContact(String patientContact) { this.patientContact = patientContact; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public double getTreatmentCost() { return treatmentCost; }
    public void setTreatmentCost(double treatmentCost) { this.treatmentCost = treatmentCost; }
}
