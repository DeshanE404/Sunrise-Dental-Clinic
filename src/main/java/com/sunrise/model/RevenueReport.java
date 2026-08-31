package com.sunrise.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueReport {
    private LocalDate reportDate;
    private long billCount;
    private BigDecimal treatmentRevenue;
    private BigDecimal consultationRevenue;
    private BigDecimal totalRevenue;

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public long getBillCount() { return billCount; }
    public void setBillCount(long billCount) { this.billCount = billCount; }

    public BigDecimal getTreatmentRevenue() { return treatmentRevenue; }
    public void setTreatmentRevenue(BigDecimal treatmentRevenue) { this.treatmentRevenue = treatmentRevenue; }

    public BigDecimal getConsultationRevenue() { return consultationRevenue; }
    public void setConsultationRevenue(BigDecimal consultationRevenue) { this.consultationRevenue = consultationRevenue; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}
