package com.sunrise.model;

import java.math.BigDecimal;

public class TreatmentStatisticsReport {
    private String treatmentName;
    private long appointmentCount;
    private BigDecimal percentageOfTotal;

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public long getAppointmentCount() { return appointmentCount; }
    public void setAppointmentCount(long appointmentCount) { this.appointmentCount = appointmentCount; }

    public BigDecimal getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(BigDecimal percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
}
