package com.sunrise.service;

import com.sunrise.dao.AppointmentDAO;
import com.sunrise.dao.TreatmentDAO;
import com.sunrise.model.Appointment;
import com.sunrise.model.Treatment;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AppointmentServiceValidationTest {

    /** Stub that returns treatments without touching the database. */
    private static class StubTreatmentDAO extends TreatmentDAO {
        @Override
        public Treatment getTreatmentById(int treatmentId) {
            if (treatmentId == 1) {
                return new Treatment(1, "Teeth Cleaning / Scaling", 4500.0);
            }
            if (treatmentId == 2) {
                return new Treatment(2, "Tooth-Colored Filling", 5000.0);
            }
            return null;
        }
    }

    private static class NoopEmailService implements EmailService {
        @Override
        public boolean sendAppointmentConfirmation(Appointment appointment, String patientEmail) {
            return true;
        }

        @Override
        public boolean sendAppointmentUpdated(Appointment appointment, String patientEmail) {
            return true;
        }

        @Override
        public boolean sendAppointmentCancelled(Appointment appointment, String patientEmail) {
            return true;
        }
    }

    private final AppointmentService service =
            new AppointmentService(new AppointmentDAO(), new StubTreatmentDAO(), new NoopEmailService());

    private Appointment validAppointment() {
        Appointment appt = new Appointment();
        appt.setAppointmentNo("APT-2026-0001");
        appt.setPatientId(1);
        appt.setDentistName("Dr. Silva");
        appt.setTreatmentIds(Arrays.asList(1));
        appt.setAppointmentDate(new Timestamp(System.currentTimeMillis() + 86_400_000L)); // tomorrow
        appt.setStatus("SCHEDULED");
        return appt;
    }

    @Test
    void acceptsValidAppointmentAndNormalisesStatus() {
        Appointment appt = validAppointment();
        assertNull(service.validateAppointment(appt));
        assertEquals("SCHEDULED", appt.getStatus());
        assertEquals(1, appt.getTreatmentId());
    }

    @Test
    void acceptsMultipleTreatments() {
        Appointment appt = validAppointment();
        appt.setTreatmentIds(Arrays.asList(1, 2));
        assertNull(service.validateAppointment(appt));
        assertEquals(Arrays.asList(1, 2), appt.getTreatmentIds());
    }

    @Test
    void rejectsNullAppointment() {
        assertEquals("Invalid appointment data", service.validateAppointment(null));
    }

    @Test
    void rejectsMissingAppointmentNumber() {
        Appointment appt = validAppointment();
        appt.setAppointmentNo("");
        assertEquals("Appointment number is required", service.validateAppointment(appt));
    }

    @Test
    void rejectsMalformedAppointmentNumber() {
        Appointment appt = validAppointment();
        appt.setAppointmentNo("APT-2026-1");
        assertEquals("Appointment number format is invalid", service.validateAppointment(appt));
    }

    @Test
    void rejectsMissingPatient() {
        Appointment appt = validAppointment();
        appt.setPatientId(0);
        assertEquals("Patient must be selected", service.validateAppointment(appt));
    }

    @Test
    void rejectsMissingDentist() {
        Appointment appt = validAppointment();
        appt.setDentistName("   ");
        assertEquals("Dentist name is required", service.validateAppointment(appt));
    }

    @Test
    void rejectsAppointmentWithoutTreatments() {
        Appointment appt = validAppointment();
        appt.setTreatmentIds(Arrays.asList());
        appt.setTreatmentId(0);
        assertEquals("At least one treatment must be selected", service.validateAppointment(appt));
    }

    @Test
    void rejectsUnknownTreatment() {
        Appointment appt = validAppointment();
        appt.setTreatmentIds(Arrays.asList(99));
        assertEquals("Treatment not found", service.validateAppointment(appt));
    }

    @Test
    void rejectsPastAppointmentDate() {
        Appointment appt = validAppointment();
        appt.setAppointmentDate(new Timestamp(System.currentTimeMillis() - 86_400_000L)); // yesterday
        assertEquals("Appointment date must be in the future", service.validateAppointment(appt));
    }

    @Test
    void rejectsInvalidStatus() {
        Appointment appt = validAppointment();
        appt.setStatus("DRAFT");
        assertEquals("Invalid appointment status", service.validateAppointment(appt));
    }
}
