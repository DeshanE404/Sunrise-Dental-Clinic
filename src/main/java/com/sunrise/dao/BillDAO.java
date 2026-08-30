package com.sunrise.dao;

import com.sunrise.model.Bill;
import java.sql.*;

public class BillDAO {

    public boolean generateBill(Bill bill) {
        if (bill == null || bill.getAppointmentNo() == null || bill.getAppointmentNo().trim().isEmpty()) {
            return false;
        }

        String query = "INSERT INTO bills (appointment_no, consultation_fee, treatment_cost, total_bill, billing_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, bill.getAppointmentNo().trim());
            preparedStatement.setDouble(2, bill.getConsultationFee());
            preparedStatement.setDouble(3, bill.getTreatmentCost());
            preparedStatement.setDouble(4, bill.getTotalBill());
            preparedStatement.setTimestamp(5, bill.getBillingDate());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                return false;
            }
            e.printStackTrace();
        }
        return false;
    }

    public Bill getBillByAppointmentNo(String appointmentNo) {
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT * FROM bills WHERE appointment_no = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appointmentNo.trim());
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Bill bill = new Bill();
                    bill.setBillNo(rs.getInt("bill_no"));
                    bill.setAppointmentNo(rs.getString("appointment_no"));
                    bill.setConsultationFee(rs.getDouble("consultation_fee"));
                    bill.setTreatmentCost(rs.getDouble("treatment_cost"));
                    bill.setTotalBill(rs.getDouble("total_bill"));
                    bill.setBillingDate(rs.getTimestamp("billing_date"));
                    return bill;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
