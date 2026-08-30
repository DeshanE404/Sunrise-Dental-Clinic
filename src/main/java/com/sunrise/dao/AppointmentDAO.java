package com.sunrise.dao;

import com.sunrise.model.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public boolean registerAppointment(Appointment appt) {
        String query = "INSERT INTO appointments (appointment_no, patient_id, dentist_name, treatment_id, appointment_date, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appt.getAppointmentNo());
            preparedStatement.setInt(2, appt.getPatientId());
            preparedStatement.setString(3, appt.getDentistName());
            preparedStatement.setInt(4, appt.getTreatmentId());
            preparedStatement.setTimestamp(5, appt.getAppointmentDate());
            preparedStatement.setString(6, appt.getStatus() == null ? "SCHEDULED" : appt.getStatus().trim().toUpperCase());

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAppointment(Appointment appt) {
        String query = "UPDATE appointments SET patient_id = ?, dentist_name = ?, treatment_id = ?, appointment_date = ?, status = ? WHERE appointment_no = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, appt.getPatientId());
            preparedStatement.setString(2, appt.getDentistName());
            preparedStatement.setInt(3, appt.getTreatmentId());
            preparedStatement.setTimestamp(4, appt.getAppointmentDate());
            preparedStatement.setString(5, appt.getStatus() == null ? "SCHEDULED" : appt.getStatus().trim().toUpperCase());
            preparedStatement.setString(6, appt.getAppointmentNo());

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAppointment(String appointmentNo) {
        String query = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_no = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appointmentNo);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean appointmentExists(String appointmentNo) {
        String query = "SELECT 1 FROM appointments WHERE appointment_no = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appointmentNo);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isDentistAvailable(String dentistName, Timestamp appointmentDate, String excludeAppointmentNo) {
        String query = "SELECT COUNT(*) AS count FROM appointments WHERE LOWER(dentist_name) = LOWER(?) AND appointment_date = ? AND status <> 'CANCELLED' " +
                       "AND (? IS NULL OR appointment_no <> ? )";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, dentistName.trim());
            preparedStatement.setTimestamp(2, appointmentDate);
            preparedStatement.setString(3, excludeAppointmentNo);
            preparedStatement.setString(4, excludeAppointmentNo);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Appointment getAppointmentByNo(String appointmentNo) {
        String query = "SELECT a.*, p.name AS patient_name, p.contact_number AS patient_contact, t.treatment_name, t.cost AS treatment_cost " +
                       "FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.patient_id " +
                       "LEFT JOIN treatments t ON a.treatment_id = t.treatment_id " +
                       "WHERE a.appointment_no = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appointmentNo);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return mapAppointment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name, p.contact_number AS patient_contact, t.treatment_name, t.cost AS treatment_cost " +
                       "FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.patient_id " +
                       "LEFT JOIN treatments t ON a.treatment_id = t.treatment_id " +
                       "ORDER BY a.appointment_date DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public List<Appointment> getTodayAppointments() {
        List<Appointment> list = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name, p.contact_number AS patient_contact, t.treatment_name, t.cost AS treatment_cost " +
                       "FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.patient_id " +
                       "LEFT JOIN treatments t ON a.treatment_id = t.treatment_id " +
                       "WHERE DATE(a.appointment_date) = CURRENT_DATE " +
                       "ORDER BY a.appointment_date ASC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment appt = new Appointment();
        appt.setAppointmentNo(rs.getString("appointment_no"));
        appt.setPatientId(rs.getInt("patient_id"));
        appt.setDentistName(rs.getString("dentist_name"));
        appt.setTreatmentId(rs.getInt("treatment_id"));
        appt.setAppointmentDate(rs.getTimestamp("appointment_date"));
        appt.setStatus(rs.getString("status"));

        appt.setPatientName(rs.getString("patient_name"));
        appt.setPatientContact(rs.getString("patient_contact"));
        appt.setTreatmentName(rs.getString("treatment_name"));
        appt.setTreatmentCost(rs.getDouble("treatment_cost"));
        return appt;
    }
}
