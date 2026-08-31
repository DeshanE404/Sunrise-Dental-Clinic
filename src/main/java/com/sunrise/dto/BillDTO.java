package com.sunrise.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillDTO {
    private int billNo;
    private String appointmentNo;
    private double consultationFee;
    private double treatmentCost;
    private double totalBill;
    private String billingDate;

    public BillDTO() {}

    public BillDTO(int billNo, String appointmentNo, double consultationFee, 
                   double treatmentCost, double totalBill, String billingDate) {
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

    public String getBillingDate() { return billingDate; }
    public void setBillingDate(String billingDate) { this.billingDate = billingDate; }
}
