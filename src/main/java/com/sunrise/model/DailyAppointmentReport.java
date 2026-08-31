package com.sunrise.model;

import java.sql.Timestamp;

public class DailyAppointmentReport {
    private String appointmentNo;
    private String patientName;
    private String dentistName;
    private String treatmentName;
    private Timestamp appointmentDateTime;
    private String status;

    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public Timestamp getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(Timestamp appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
