package com.sunrise.service;

import com.sunrise.dao.BillDAO;
import com.sunrise.dao.TreatmentDAO;
import com.sunrise.model.Bill;
import com.sunrise.model.Treatment;
import java.sql.Timestamp;

public class BillingService {
    private BillDAO billDAO = new BillDAO();
    private TreatmentDAO treatmentDAO = new TreatmentDAO();
    
    private static final double CONSULTATION_FEE = 50.0;

    public Bill generateAndSaveBill(String appointmentNo, int treatmentId) {
        // Fetch treatment to get cost
        Treatment t = treatmentDAO.getTreatmentById(treatmentId);
        double treatmentCost = (t != null) ? t.getCost() : 0.0;
        double total = CONSULTATION_FEE + treatmentCost;

        // Check if bill already exists
        Bill existing = billDAO.getBillByAppointmentNo(appointmentNo);
        if (existing != null) {
            return existing;
        }

        Bill bill = new Bill();
        bill.setAppointmentNo(appointmentNo);
        bill.setConsultationFee(CONSULTATION_FEE);
        bill.setTreatmentCost(treatmentCost);
        bill.setTotalBill(total);
        bill.setBillingDate(new Timestamp(System.currentTimeMillis()));

        boolean success = billDAO.generateBill(bill);
        if (success) {
            return billDAO.getBillByAppointmentNo(appointmentNo);
        }
        return null;
    }

    public Bill getBill(String appointmentNo) {
        return billDAO.getBillByAppointmentNo(appointmentNo);
    }
}
