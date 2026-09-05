package com.sunrise.service;

import com.sunrise.model.Appointment;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceImplTest {

    @Test
    void buildConfirmationHtmlContainsRequiredDetails() {
        EmailServiceImpl service = new EmailServiceImpl();

        String html = service.buildConfirmationHtml(
                "APT-2026-0001",
                "Jane Perera",
                "Dr. Silva",
                "Whitening",
                "2026-09-15",
                "10:30"
        );

        assertTrue(html.contains("Sunrise Dental Clinic"));
        assertTrue(html.contains("APT-2026-0001"));
        assertTrue(html.contains("Jane Perera"));
        assertTrue(html.contains("Dr. Silva"));
        assertTrue(html.contains("Whitening"));
        assertTrue(html.contains("2026-09-15"));
        assertTrue(html.contains("10:30"));
    }

    @Test
    void sendAppointmentConfirmationReturnsFalseWhenEmailIsDisabled() {
        String previous = System.getProperty("EMAIL_ENABLED");
        System.setProperty("EMAIL_ENABLED", "false");

        try {
            EmailServiceImpl service = new EmailServiceImpl();
            Appointment appointment = new Appointment();
            appointment.setAppointmentNo("APT-2026-0002");
            appointment.setDentistName("Dr. Perera");
            appointment.setTreatmentId(1);
            appointment.setAppointmentDate(Timestamp.valueOf("2026-09-15 10:30:00"));
            appointment.setStatus("SCHEDULED");

            boolean sent = service.sendAppointmentConfirmation(appointment, "jane@example.com");
            assertFalse(sent);
        } finally {
            if (previous == null) {
                System.clearProperty("EMAIL_ENABLED");
            } else {
                System.setProperty("EMAIL_ENABLED", previous);
            }
        }
    }
}
