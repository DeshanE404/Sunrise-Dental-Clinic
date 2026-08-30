package com.sunrise.model;

import java.sql.Timestamp;

public class Appointment {
    private String appointmentNo;
    private int patientId;
    private String dentistName;
    private int treatmentId;
    private Timestamp appointmentDate;
    
    // Virtual fields to simplify display
    private String patientName;
    private String patientContact;
    private String treatmentName;
    private double treatmentCost;

    public Appointment() {}

    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public int getTreatmentId() { return treatmentId; }
    public void setTreatmentId(int treatmentId) { this.treatmentId = treatmentId; }

    public Timestamp getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Timestamp appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientContact() { return patientContact; }
    public void setPatientContact(String patientContact) { this.patientContact = patientContact; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public double getTreatmentCost() { return treatmentCost; }
    public void setTreatmentCost(double treatmentCost) { this.treatmentCost = treatmentCost; }
}
