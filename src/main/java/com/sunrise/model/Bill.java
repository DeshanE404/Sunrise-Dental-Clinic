package com.sunrise.model;

import java.sql.Timestamp;

public class Bill {
    private int billNo;
    private String appointmentNo;
    private double consultationFee;
    private double treatmentCost;
    private double totalBill;
    private Timestamp billingDate;

    public Bill() {}

    public Bill(int billNo, String appointmentNo, double consultationFee, double treatmentCost, double totalBill, Timestamp billingDate) {
        this.billNo = billNo;
        this.appointmentNo = appointmentNo;
        this.consultationFee = consultationFee;
        this.treatmentCost = treatmentCost;
        this.totalBill = totalBill;
        this.billingDate = billingDate;
    }

    public int getBillNo() { return billNo; }
    public void setBillNo(int billNo) { this.billNo = billNo; }

    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public double getTreatmentCost() { return treatmentCost; }
    public void setTreatmentCost(double treatmentCost) { this.treatmentCost = treatmentCost; }

    public double getTotalBill() { return totalBill; }
    public void setTotalBill(double totalBill) { this.totalBill = totalBill; }

    public Timestamp getBillingDate() { return billingDate; }
    public void setBillingDate(Timestamp billingDate) { this.billingDate = billingDate; }
}
