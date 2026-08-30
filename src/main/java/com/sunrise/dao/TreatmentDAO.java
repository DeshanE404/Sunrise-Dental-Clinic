package com.sunrise.dao;

import com.sunrise.model.Treatment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDAO {

    public List<Treatment> getAllTreatments() {
        List<Treatment> list = new ArrayList<>();
        String query = "SELECT * FROM treatments ORDER BY treatment_name ASC";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {
            
            while (rs.next()) {
                Treatment t = new Treatment();
                t.setTreatmentId(rs.getInt("treatment_id"));
                t.setTreatmentName(rs.getString("treatment_name"));
                t.setCost(rs.getDouble("cost"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Treatment getTreatmentById(int treatmentId) {
        String query = "SELECT * FROM treatments WHERE treatment_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, treatmentId);
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                Treatment t = new Treatment();
                t.setTreatmentId(rs.getInt("treatment_id"));
                t.setTreatmentName(rs.getString("treatment_name"));
                t.setCost(rs.getDouble("cost"));
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Treatment getTreatmentByName(String name) {
        String query = "SELECT * FROM treatments WHERE treatment_name = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, name);
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                Treatment t = new Treatment();
                t.setTreatmentId(rs.getInt("treatment_id"));
                t.setTreatmentName(rs.getString("treatment_name"));
                t.setCost(rs.getDouble("cost"));
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
