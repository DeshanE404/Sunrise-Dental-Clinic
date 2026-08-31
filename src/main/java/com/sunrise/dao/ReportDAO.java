package com.sunrise.dao;

import com.sunrise.model.DashboardSummary;
import com.sunrise.model.DailyAppointmentReport;
import com.sunrise.model.DentistWorkloadReport;
import com.sunrise.model.RevenueReport;
import com.sunrise.model.TreatmentStatisticsReport;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public DashboardSummary getDashboardSummary(LocalDate reportDate) {
        String query = "SELECT " +
                "COUNT(a.appointment_id) AS total_appointments, " +
                "SUM(CASE WHEN UPPER(COALESCE(a.status, 'SCHEDULED')) = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_appointments, " +
                "SUM(CASE WHEN UPPER(COALESCE(a.status, 'SCHEDULED')) = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_appointments, " +
                "COALESCE(SUM(b.total_bill), 0) AS revenue " +
                "FROM appointments a " +
                "LEFT JOIN bills b ON b.appointment_no = a.appointment_no " +
                "WHERE DATE(a.appointment_date) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(reportDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DashboardSummary summary = new DashboardSummary();
                    summary.setTotalAppointments(rs.getLong("total_appointments"));
                    summary.setCompletedAppointments(rs.getLong("completed_appointments"));
                    summary.setCancelledAppointments(rs.getLong("cancelled_appointments"));
                    summary.setRevenue(rs.getBigDecimal("revenue"));
                    return summary;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new DashboardSummary();
    }

    public List<DailyAppointmentReport> getDailyAppointments(LocalDate selectedDate) {
        String query = "SELECT a.appointment_no, p.name AS patient_name, a.dentist_name, t.treatment_name, " +
                "a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON p.patient_id = a.patient_id " +
                "LEFT JOIN treatments t ON t.treatment_id = a.treatment_id " +
                "WHERE DATE(a.appointment_date) = ? " +
                "ORDER BY a.appointment_date ASC";

        List<DailyAppointmentReport> report = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(selectedDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailyAppointmentReport row = new DailyAppointmentReport();
                    row.setAppointmentNo(rs.getString("appointment_no"));
                    row.setPatientName(rs.getString("patient_name"));
                    row.setDentistName(rs.getString("dentist_name"));
                    row.setTreatmentName(rs.getString("treatment_name"));
                    row.setAppointmentDateTime(rs.getTimestamp("appointment_date"));
                    row.setStatus(rs.getString("status"));
                    report.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public List<DentistWorkloadReport> getDentistWorkload(LocalDate startDate, LocalDate endDate) {
        String query = "SELECT a.dentist_name, " +
                "COUNT(*) AS total_appointments, " +
                "SUM(CASE WHEN UPPER(COALESCE(a.status, 'SCHEDULED')) = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_appointments, " +
                "SUM(CASE WHEN UPPER(COALESCE(a.status, 'SCHEDULED')) = 'SCHEDULED' THEN 1 ELSE 0 END) AS scheduled_appointments, " +
                "SUM(CASE WHEN UPPER(COALESCE(a.status, 'SCHEDULED')) = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_appointments, " +
                "SUM(CASE WHEN UPPER(COALESCE(a.status, 'SCHEDULED')) = 'NO_SHOW' THEN 1 ELSE 0 END) AS no_show_appointments " +
                "FROM appointments a " +
                "WHERE DATE(a.appointment_date) BETWEEN ? AND ? " +
                "GROUP BY a.dentist_name " +
                "ORDER BY a.dentist_name ASC";

        List<DentistWorkloadReport> report = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DentistWorkloadReport row = new DentistWorkloadReport();
                    row.setDentistName(rs.getString("dentist_name"));
                    row.setTotalAppointments(rs.getLong("total_appointments"));
                    row.setCompletedAppointments(rs.getLong("completed_appointments"));
                    row.setScheduledAppointments(rs.getLong("scheduled_appointments"));
                    row.setCancelledAppointments(rs.getLong("cancelled_appointments"));
                    row.setNoShowAppointments(rs.getLong("no_show_appointments"));
                    report.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public List<TreatmentStatisticsReport> getTreatmentStatistics(LocalDate startDate, LocalDate endDate) {
        String query = "SELECT " +
                "t.treatment_name, " +
                "COUNT(a.appointment_id) AS appointment_count, " +
                "ROUND((COUNT(a.appointment_id)::numeric / NULLIF(total.total_appointments, 0)) * 100, 2) AS percentage_of_total " +
                "FROM treatments t " +
                "LEFT JOIN appointments a ON a.treatment_id = t.treatment_id AND DATE(a.appointment_date) BETWEEN ? AND ? " +
                "CROSS JOIN (SELECT COUNT(*) AS total_appointments FROM appointments WHERE DATE(appointment_date) BETWEEN ? AND ?) AS total " +
                "GROUP BY t.treatment_name, total.total_appointments " +
                "ORDER BY appointment_count DESC, t.treatment_name ASC";

        List<TreatmentStatisticsReport> report = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ps.setDate(3, java.sql.Date.valueOf(startDate));
            ps.setDate(4, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TreatmentStatisticsReport row = new TreatmentStatisticsReport();
                    row.setTreatmentName(rs.getString("treatment_name"));
                    row.setAppointmentCount(rs.getLong("appointment_count"));
                    row.setPercentageOfTotal(rs.getBigDecimal("percentage_of_total"));
                    report.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public List<RevenueReport> getRevenueReport(LocalDate startDate, LocalDate endDate) {
        String query = "SELECT " +
                "DATE(b.billing_date) AS revenue_date, " +
                "COUNT(*) AS bill_count, " +
                "SUM(b.treatment_cost) AS treatment_revenue, " +
                "SUM(b.consultation_fee) AS consultation_revenue, " +
                "SUM(b.total_bill) AS total_revenue " +
                "FROM bills b " +
                "WHERE DATE(b.billing_date) BETWEEN ? AND ? " +
                "GROUP BY DATE(b.billing_date) " +
                "ORDER BY DATE(b.billing_date) ASC";

        List<RevenueReport> report = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevenueReport row = new RevenueReport();
                    row.setReportDate(rs.getDate("revenue_date").toLocalDate());
                    row.setBillCount(rs.getLong("bill_count"));
                    row.setTreatmentRevenue(rs.getBigDecimal("treatment_revenue"));
                    row.setConsultationRevenue(rs.getBigDecimal("consultation_revenue"));
                    row.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    report.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public BigDecimal getTotalRevenue() {
        String query = "SELECT COALESCE(SUM(total_bill), 0) AS total_revenue FROM bills";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("total_revenue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}
