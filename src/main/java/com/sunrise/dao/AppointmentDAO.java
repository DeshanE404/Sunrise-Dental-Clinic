package com.sunrise.dao;

import com.sunrise.model.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public boolean registerAppointment(Appointment appt) {
        String query = "INSERT INTO appointments (appointment_no, patient_id, dentist_name, treatment_id, appointment_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, appt.getAppointmentNo());
            preparedStatement.setInt(2, appt.getPatientId());
            preparedStatement.setString(3, appt.getDentistName());
            preparedStatement.setInt(4, appt.getTreatmentId());
            preparedStatement.setTimestamp(5, appt.getAppointmentDate());
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
            
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
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                Appointment appt = new Appointment();
                appt.setAppointmentNo(rs.getString("appointment_no"));
                appt.setPatientId(rs.getInt("patient_id"));
                appt.setDentistName(rs.getString("dentist_name"));
                appt.setTreatmentId(rs.getInt("treatment_id"));
                appt.setAppointmentDate(rs.getTimestamp("appointment_date"));
                
                appt.setPatientName(rs.getString("patient_name"));
                appt.setPatientContact(rs.getString("patient_contact"));
                appt.setTreatmentName(rs.getString("treatment_name"));
                appt.setTreatmentCost(rs.getDouble("treatment_cost"));
                return appt;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
                Appointment appt = new Appointment();
                appt.setAppointmentNo(rs.getString("appointment_no"));
                appt.setPatientId(rs.getInt("patient_id"));
                appt.setDentistName(rs.getString("dentist_name"));
                appt.setTreatmentId(rs.getInt("treatment_id"));
                appt.setAppointmentDate(rs.getTimestamp("appointment_date"));
                
                appt.setPatientName(rs.getString("patient_name"));
                appt.setPatientContact(rs.getString("patient_contact"));
                appt.setTreatmentName(rs.getString("treatment_name"));
                appt.setTreatmentCost(rs.getDouble("treatment_cost"));
                list.add(appt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
