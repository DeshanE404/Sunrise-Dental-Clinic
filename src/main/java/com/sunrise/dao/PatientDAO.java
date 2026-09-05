package com.sunrise.dao;

import com.sunrise.model.Patient;
import java.sql.*;

public class PatientDAO {

    public int registerPatient(Patient patient) {
        String query = "INSERT INTO patients (name, address, contact_number, email) VALUES (?, ?, ?, ?) RETURNING patient_id";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, patient.getName());
            preparedStatement.setString(2, patient.getAddress());
            preparedStatement.setString(3, patient.getContactNumber());
            preparedStatement.setString(4, patient.getEmail());
            
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt("patient_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateEmail(int patientId, String email) {
        String query = "UPDATE patients SET email = ? WHERE patient_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            preparedStatement.setInt(2, patientId);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Patient getPatientById(int patientId) {
        String query = "SELECT * FROM patients WHERE patient_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, patientId);
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setName(rs.getString("name"));
                p.setAddress(rs.getString("address"));
                p.setContactNumber(rs.getString("contact_number"));
                p.setEmail(rs.getString("email"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient getPatientByNameAndContact(String name, String contact) {
        String query = "SELECT * FROM patients WHERE name = ? AND contact_number = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, contact);
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setName(rs.getString("name"));
                p.setAddress(rs.getString("address"));
                p.setContactNumber(rs.getString("contact_number"));
                p.setEmail(rs.getString("email"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getTotalPatientsCount() {
        String query = "SELECT COUNT(*) AS count FROM patients";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
