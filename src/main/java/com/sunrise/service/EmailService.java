package com.sunrise.service;

import com.sunrise.model.Appointment;

public interface EmailService {
    boolean sendAppointmentConfirmation(Appointment appointment, String patientEmail);
    boolean sendAppointmentUpdated(Appointment appointment, String patientEmail);
    boolean sendAppointmentCancelled(Appointment appointment, String patientEmail);
}
