package com.sunrise.service;

import com.sunrise.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    public double getTotalRevenue() {
        String query = "SELECT SUM(total_bill) AS total FROM bills";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Map<String, Integer> getTreatmentPopularity() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT t.treatment_name, COUNT(a.appointment_no) AS count " +
                       "FROM appointments a " +
                       "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                       "GROUP BY t.treatment_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("treatment_name"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public Map<String, Integer> getDentistAppointmentsCount() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT dentist_name, COUNT(appointment_no) AS count FROM appointments GROUP BY dentist_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("dentist_name"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
