package com.sunrise.service;

import com.sunrise.model.Appointment;
import com.sunrise.model.Patient;
import com.sunrise.model.Treatment;
import com.sunrise.dao.PatientDAO;
import com.sunrise.dao.TreatmentDAO;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailServiceImpl implements EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl.class.getName());
    private static final String DEFAULT_EMAIL_FROM = "noreply@sunrisedentalclinic.com";
    private static final String DEFAULT_EMAIL_FROM_NAME = "Sunrise Dental Clinic";
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final Properties LOCAL_CONFIG = loadLocalConfig();
    private final PatientDAO patientDAO = new PatientDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    @Override
    public boolean sendAppointmentConfirmation(Appointment appointment, String patientEmail) {
        if (appointment == null || patientEmail == null || patientEmail.trim().isEmpty()) {
            LOGGER.warning("Email not sent: missing appointment data.");
            return false;
        }
        if (!isEmailEnabled()) {
            LOGGER.info("Email notifications are disabled by configuration.");
            return false;
        }

        return sendEmail(patientEmail, "Appointment Confirmation", buildConfirmationHtml(
                appointment.getAppointmentNo(),
                getPatientName(appointment.getPatientId()),
                appointment.getDentistName(),
                getAppointmentTreatmentLabel(appointment),
                formatDate(appointment.getAppointmentDate()),
                formatTime(appointment.getAppointmentDate())
        ));
    }

    @Override
    public boolean sendAppointmentUpdated(Appointment appointment, String patientEmail) {
        if (appointment == null || patientEmail == null || patientEmail.trim().isEmpty()) {
            LOGGER.warning("Email not sent: missing appointment data.");
            return false;
        }
        if (!isEmailEnabled()) {
            LOGGER.info("Email notifications are disabled by configuration.");
            return false;
        }

        return sendEmail(patientEmail, "Appointment Updated", buildUpdatedHtml(
                appointment.getAppointmentNo(),
                getPatientName(appointment.getPatientId()),
                appointment.getDentistName(),
                getAppointmentTreatmentLabel(appointment),
                formatDate(appointment.getAppointmentDate()),
                formatTime(appointment.getAppointmentDate()),
                appointment.getStatus() == null ? "SCHEDULED" : appointment.getStatus()
        ));
    }

    @Override
    public boolean sendAppointmentCancelled(Appointment appointment, String patientEmail) {
        if (appointment == null || patientEmail == null || patientEmail.trim().isEmpty()) {
            LOGGER.warning("Email not sent: missing appointment data.");
            return false;
        }
        if (!isEmailEnabled()) {
            LOGGER.info("Email notifications are disabled by configuration.");
            return false;
        }

        return sendEmail(patientEmail, "Appointment Cancelled", buildCancelledHtml(
                appointment.getAppointmentNo(),
                getPatientName(appointment.getPatientId()),
                appointment.getDentistName(),
                formatDate(appointment.getAppointmentDate()),
                formatTime(appointment.getAppointmentDate())
        ));
    }

    public String buildConfirmationHtml(String appointmentNo, String patientName, String dentistName,
                                       String treatmentName, String appointmentDate, String appointmentTime) {
        return buildHtml("Appointment Confirmation",
                "Dear " + safe(patientName) + ",",
                "Your dental appointment has been successfully scheduled.",
                "Appointment Number: " + safe(appointmentNo),
                "Dentist: " + safe(dentistName),
                "Treatment: " + safe(treatmentName),
                "Date: " + safe(appointmentDate),
                "Time: " + safe(appointmentTime),
                "Please contact the clinic if you need to make any changes.");
    }

    public String buildUpdatedHtml(String appointmentNo, String patientName, String dentistName,
                                  String treatmentName, String appointmentDate, String appointmentTime, String status) {
        return buildHtml("Appointment Updated",
                "Dear " + safe(patientName) + ",",
                "Your appointment details have been updated.",
                "Appointment Number: " + safe(appointmentNo),
                "Dentist: " + safe(dentistName),
                "Treatment: " + safe(treatmentName),
                "Date: " + safe(appointmentDate),
                "Time: " + safe(appointmentTime),
                "Status: " + safe(status),
                "Please contact the clinic if you need any assistance.");
    }

    public String buildCancelledHtml(String appointmentNo, String patientName, String dentistName,
                                    String appointmentDate, String appointmentTime) {
        return buildHtml("Appointment Cancelled",
                "Dear " + safe(patientName) + ",",
                "Your appointment has been cancelled.",
                "Appointment Number: " + safe(appointmentNo),
                "Dentist: " + safe(dentistName),
                "Original Date: " + safe(appointmentDate),
                "Original Time: " + safe(appointmentTime),
                "If this was not intended, please contact the clinic as soon as possible.");
    }

    private boolean sendEmail(String patientEmail, String subject, String htmlBody) {
        if (patientEmail == null || patientEmail.trim().isEmpty()) {
            LOGGER.warning("Email not sent: patient email missing.");
            return false;
        }

        if (!isEmailEnabled()) {
            LOGGER.info("Email notifications are disabled by configuration.");
            return false;
        }

        String apiKey = setting("EMAIL_API_KEY", null);
        String fromEmail = setting("EMAIL_FROM", DEFAULT_EMAIL_FROM);
        String fromName = setting("EMAIL_FROM_NAME", DEFAULT_EMAIL_FROM_NAME);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Email provider API key missing. Notification not sent.");
            return false;
        }

        try {
            URL url = new URL(BREVO_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("api-key", apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            String payload = "{\n"
                    + "  \"sender\": {\"name\": \"" + jsonEscape(fromName) + "\", \"email\": \"" + jsonEscape(fromEmail) + "\"},\n"
                    + "  \"to\": [{\"email\": \"" + jsonEscape(patientEmail) + "\"}],\n"
                    + "  \"subject\": \"" + jsonEscape(subject) + "\",\n"
                    + "  \"htmlContent\": \"" + jsonEscape(htmlBody) + "\"\n"
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = connection.getResponseCode();
            if (statusCode >= 200 && statusCode < 300) {
                LOGGER.info("Email sent successfully to: " + patientEmail + " | Subject: " + subject);
                return true;
            }

            LOGGER.log(Level.WARNING, "Email provider rejected notification: HTTP " + statusCode
                    + " | To: " + patientEmail + " | Subject: " + subject);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Email sending failed for patient " + patientEmail + ". Application continues without crashing.", e);
            return false;
        }
    }

    private String buildHtml(String title, String... bodyLines) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>")
            .append("body{font-family:Arial,sans-serif;background:#f5f7f7;color:#1d2b2f;padding:24px;}"
                    + ".container{max-width:640px;margin:0 auto;background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:32px;}"
                    + ".header{font-size:28px;font-weight:bold;color:#0f766e;margin-bottom:16px;}"
                    + ".subtitle{font-size:18px;color:#374151;margin-bottom:16px;}"
                    + ".line{margin:8px 0;color:#1f2937;font-size:15px;}"
                    + ".footer{margin-top:24px;padding-top:16px;border-top:1px solid #e5e7eb;color:#6b7280;font-size:12px;}")
            .append("</style></head><body><div class='container'>")
            .append("<div class='header'>Sunrise Dental Clinic</div>")
            .append("<div class='subtitle'>" + safe(title) + "</div>");

        for (String line : bodyLines) {
            html.append("<div class='line'>" + safe(line) + "</div>");
        }

        html.append("<div class='footer'>Regards,<br>Sunrise Dental Clinic<br>Phone: +94 11 234 5678</div>")
            .append("</div></body></html>");

        return html.toString();
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("<", "&lt;").replace(">", "&gt;");
    }

    private boolean isEmailEnabled() {
        return "true".equalsIgnoreCase(setting("EMAIL_ENABLED", "true"));
    }

    /**
     * Resolves a configuration value in order of precedence:
     * 1. JVM system property (-DEMAIL_API_KEY=...)
     * 2. environment variable
     * 3. src/main/resources/email.properties (local, never committed to git)
     */
    private String setting(String name, String defaultValue) {
        String value = System.getProperty(name);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(name);
        }
        if (value == null || value.trim().isEmpty() && LOCAL_CONFIG != null) {
            value = LOCAL_CONFIG.getProperty(name);
        }
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static Properties loadLocalConfig() {
        Properties props = new Properties();
        try (InputStream in = EmailServiceImpl.class.getResourceAsStream("/email.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load email.properties", e);
        }
        return props;
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private String formatDate(Timestamp ts) {
        if (ts == null) {
            return "";
        }
        return ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String formatTime(Timestamp ts) {
        if (ts == null) {
            return "";
        }
        return ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String getPatientName(int patientId) {
        Patient patient = patientDAO.getPatientById(patientId);
        return patient != null ? patient.getName() : "Patient";
    }

    private String getAppointmentTreatmentLabel(Appointment appointment) {
        if (appointment != null && appointment.getTreatmentName() != null && !appointment.getTreatmentName().trim().isEmpty()) {
            return appointment.getTreatmentName();
        }
        if (appointment != null) {
            return getTreatmentName(appointment.getTreatmentId());
        }
        return "Treatment";
    }

    private String getTreatmentName(int treatmentId) {
        Treatment treatment = treatmentDAO.getTreatmentById(treatmentId);
        return treatment != null ? treatment.getTreatmentName() : "Treatment";
    }
}
