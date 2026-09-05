package com.sunrise.service;

import com.sunrise.dao.ReportDAO;
import com.sunrise.model.DailyAppointmentReport;
import com.sunrise.model.DashboardSummary;
import com.sunrise.model.DentistWorkloadReport;
import com.sunrise.model.RevenueReport;
import com.sunrise.model.TreatmentStatisticsReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportService {
    private final ReportDAO reportDAO = new ReportDAO();

    public BigDecimal getTotalRevenue() {
        return reportDAO.getTotalRevenue();
    }

    public DashboardSummary getDashboardSummary(LocalDate reportDate) {
        if (reportDate == null) {
            reportDate = LocalDate.now();
        }
        return reportDAO.getDashboardSummary(reportDate);
    }

    public List<DailyAppointmentReport> getDailyAppointments(LocalDate selectedDate) {
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }
        return reportDAO.getDailyAppointments(selectedDate);
    }

    public List<DentistWorkloadReport> getDentistWorkload(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            return reportDAO.getDentistWorkload(today.minusDays(6), today);
        }
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        return reportDAO.getDentistWorkload(startDate, endDate);
    }

    public List<TreatmentStatisticsReport> getTreatmentStatistics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            return reportDAO.getTreatmentStatistics(today.minusDays(6), today);
        }
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        return reportDAO.getTreatmentStatistics(startDate, endDate);
    }

    public List<RevenueReport> getRevenueReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            return reportDAO.getRevenueReport(today.minusDays(6), today);
        }
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        return reportDAO.getRevenueReport(startDate, endDate);
    }
}
