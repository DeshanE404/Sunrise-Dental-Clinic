package com.sunrise.service;

import com.sunrise.dao.BillDAO;
import com.sunrise.dao.TreatmentDAO;
import com.sunrise.model.Appointment;
import com.sunrise.model.Bill;
import com.sunrise.model.Treatment;
import java.sql.Timestamp;
import java.util.List;

public class BillingService {
    private final BillDAO billDAO;
    private final TreatmentDAO treatmentDAO;
    private final AppointmentService appointmentService;

    public BillingService() {
        this(new BillDAO(), new TreatmentDAO(), new AppointmentService());
    }

    // Package-private constructor that allows tests to inject lightweight fakes.
    BillingService(BillDAO billDAO, TreatmentDAO treatmentDAO, AppointmentService appointmentService) {
        this.billDAO = billDAO;
        this.treatmentDAO = treatmentDAO;
        this.appointmentService = appointmentService;
    }

    private static final double CONSULTATION_FEE = 500.0; // LKR registration & administration fee

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

        // Collect every treatment registered on the appointment. When the
        // appointment_treatments table is empty (legacy record) fall back to the
        // single treatment id supplied by the caller.
        List<Treatment> treatments = appointmentService.getAppointmentTreatments(cleanAppointmentNo);
        if (treatments == null || treatments.isEmpty()) {
            treatments = new java.util.ArrayList<>();
            int fallbackId = appointment.getTreatmentId() > 0 ? appointment.getTreatmentId() : treatmentId;
            if (fallbackId > 0) {
                Treatment fallback = treatmentDAO.getTreatmentById(fallbackId);
                if (fallback != null) {
                    treatments.add(fallback);
                }
            }
        }
        if (treatments.isEmpty()) {
            return null;
        }

        Bill existing = billDAO.getBillByAppointmentNo(cleanAppointmentNo);
        if (existing != null) {
            return existing;
        }

        double treatmentCost = 0.0;
        for (Treatment treatment : treatments) {
            treatmentCost += treatment.getCost();
        }
        double total = CONSULTATION_FEE + treatmentCost;

        Bill bill = new Bill();
        bill.setAppointmentNo(cleanAppointmentNo);
        bill.setConsultationFee(CONSULTATION_FEE);
        bill.setTreatmentCost(treatmentCost);
        bill.setTotalBill(total);
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
