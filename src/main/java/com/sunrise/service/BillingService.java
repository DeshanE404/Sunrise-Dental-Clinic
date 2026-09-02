package com.sunrise.service;

import com.sunrise.dao.BillDAO;
import com.sunrise.dao.TreatmentDAO;
import com.sunrise.model.Appointment;
import com.sunrise.model.Bill;
import com.sunrise.model.Treatment;
import java.sql.Timestamp;

public class BillingService {
    private final BillDAO billDAO = new BillDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final AppointmentService appointmentService = new AppointmentService();

    private static final double CONSULTATION_FEE = 50.0;

    public Bill generateAndSaveBill(String appointmentNo, int treatmentId) {
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return null;
        }

        String cleanAppointmentNo = appointmentNo.trim();
        Appointment appointment = appointmentService.getAppointmentDetails(cleanAppointmentNo);
        if (appointment == null) {
            return null;
        }

        if ("CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
            return null;
        }

        int appointmentTreatmentId = appointment.getTreatmentId() > 0 ? appointment.getTreatmentId() : treatmentId;
        Treatment treatment = treatmentDAO.getTreatmentById(appointmentTreatmentId);
        if (treatment == null) {
            return null;
        }

        Bill existing = billDAO.getBillByAppointmentNo(cleanAppointmentNo);
        if (existing != null) {
            return existing;
        }

        double treatmentCost = treatment.getCost();

        Bill bill = new Bill();
        bill.setAppointmentNo(cleanAppointmentNo);
        bill.setConsultationFee(CONSULTATION_FEE);
        bill.setTreatmentCost(treatmentCost);
        bill.setTotalBill(0.0);
        bill.setBillingDate(new Timestamp(System.currentTimeMillis()));

        boolean success = billDAO.generateBill(bill);
        if (success) {
            return billDAO.getBillByAppointmentNo(cleanAppointmentNo);
        }
        return null;
    }

    public Bill getBill(String appointmentNo) {
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return null;
        }
        return billDAO.getBillByAppointmentNo(appointmentNo.trim());
    }
}
