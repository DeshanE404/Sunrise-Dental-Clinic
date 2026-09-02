package com.sunrise.dao;

import com.sunrise.model.Dentist;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DentistDAO {

    public List<Dentist> getAllDentists() {
        List<Dentist> list = new ArrayList<>();
        String query = "SELECT * FROM dentists ORDER BY dentist_name ASC";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {
            
            while (rs.next()) {
                Dentist d = new Dentist();
                d.setDentistId(rs.getInt("dentist_id"));
                d.setDentistName(rs.getString("dentist_name"));
                d.setSpecialization(rs.getString("specialization"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Dentist getDentistById(int dentistId) {
        String query = "SELECT * FROM dentists WHERE dentist_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, dentistId);
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                Dentist d = new Dentist();
                d.setDentistId(rs.getInt("dentist_id"));
                d.setDentistName(rs.getString("dentist_name"));
                d.setSpecialization(rs.getString("specialization"));
                return d;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Dentist getDentistByName(String dentistName) {
        String query = "SELECT * FROM dentists WHERE LOWER(dentist_name) = LOWER(?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, dentistName);
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                Dentist d = new Dentist();
                d.setDentistId(rs.getInt("dentist_id"));
                d.setDentistName(rs.getString("dentist_name"));
                d.setSpecialization(rs.getString("specialization"));
                return d;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
