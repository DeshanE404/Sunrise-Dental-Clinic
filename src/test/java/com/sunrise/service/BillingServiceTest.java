package com.sunrise.service;

import com.sunrise.dao.BillDAO;
import com.sunrise.dao.TreatmentDAO;
import com.sunrise.model.Appointment;
import com.sunrise.model.Bill;
import com.sunrise.model.Treatment;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingServiceTest {

    private static final double CONSULTATION_FEE = 500.0;

    private static class StubBillDAO extends BillDAO {
        Bill existing;
        Bill saved;
        boolean generateCalled;

        @Override
        public boolean generateBill(Bill bill) {
            generateCalled = true;
            saved = bill;
            return true;
        }

        @Override
        public Bill getBillByAppointmentNo(String appointmentNo) {
            return saved != null ? saved : existing;
        }
    }

    private static class StubTreatmentDAO extends TreatmentDAO {
        @Override
        public Treatment getTreatmentById(int treatmentId) {
            if (treatmentId == 7) {
                return new Treatment(7, "Tooth Extraction", 5500.0);
            }
            return null;
        }
    }

    private static class StubAppointmentService extends AppointmentService {
        Appointment appointment;
        List<Treatment> treatments = Collections.emptyList();

        @Override
        public Appointment getAppointmentDetails(String appointmentNo) {
            return appointment;
        }

        @Override
        public List<Treatment> getAppointmentTreatments(String appointmentNo) {
            return treatments;
        }
    }

    private Appointment scheduledAppointment() {
        Appointment appt = new Appointment();
        appt.setAppointmentNo("APT-2026-0001");
        appt.setPatientId(1);
        appt.setDentistName("Dr. Silva");
        appt.setStatus("SCHEDULED");
        appt.setAppointmentDate(new Timestamp(System.currentTimeMillis() + 86_400_000L));
        return appt;
    }

    @Test
    void returnsNullForMissingAppointmentNumber() {
        StubBillDAO billDAO = new StubBillDAO();
        BillingService service = new BillingService(billDAO, new StubTreatmentDAO(), new StubAppointmentService());
        assertNull(service.generateAndSaveBill(null, 1));
        assertFalse(billDAO.generateCalled);
    }

    @Test
    void returnsNullWhenAppointmentNotFound() {
        StubBillDAO billDAO = new StubBillDAO();
        StubAppointmentService apptService = new StubAppointmentService();
        apptService.appointment = null;

        BillingService service = new BillingService(billDAO, new StubTreatmentDAO(), apptService);
        assertNull(service.generateAndSaveBill("APT-2026-9999", 1));
        assertFalse(billDAO.generateCalled);
    }

    @Test
    void returnsNullForCancelledAppointment() {
        StubBillDAO billDAO = new StubBillDAO();
        StubAppointmentService apptService = new StubAppointmentService();
        Appointment cancelled = scheduledAppointment();
        cancelled.setStatus("CANCELLED");
        apptService.appointment = cancelled;
        apptService.treatments = Collections.singletonList(new Treatment(1, "Cleaning", 1000.0));

        BillingService service = new BillingService(billDAO, new StubTreatmentDAO(), apptService);
        assertNull(service.generateAndSaveBill("APT-2026-0001", 1));
        assertFalse(billDAO.generateCalled);
    }

    @Test
    void sumsAllTreatmentsAndAddsConsultationFee() {
        StubBillDAO billDAO = new StubBillDAO();
        StubAppointmentService apptService = new StubAppointmentService();
        apptService.appointment = scheduledAppointment();
        apptService.treatments = Arrays.asList(
                new Treatment(1, "Teeth Cleaning / Scaling", 1000.0),
                new Treatment(2, "Tooth-Colored Filling", 1500.0)
        );

        BillingService service = new BillingService(billDAO, new StubTreatmentDAO(), apptService);
        Bill bill = service.generateAndSaveBill("APT-2026-0001", 1);

        assertNotNull(bill);
        assertEquals(2500.0, bill.getTreatmentCost(), 0.001);
        assertEquals(CONSULTATION_FEE, bill.getConsultationFee(), 0.001);
        assertEquals(3000.0, bill.getTotalBill(), 0.001);
        assertTrue(billDAO.generateCalled);
    }

    @Test
    void returnsExistingBillWithoutGeneratingANewOne() {
        StubBillDAO billDAO = new StubBillDAO();
        StubAppointmentService apptService = new StubAppointmentService();
        apptService.appointment = scheduledAppointment();
        apptService.treatments = Collections.singletonList(new Treatment(1, "Cleaning", 1000.0));

        Bill existingBill = new Bill();
        existingBill.setBillNo(5);
        existingBill.setTotalBill(1111.0);
        billDAO.existing = existingBill;

        BillingService service = new BillingService(billDAO, new StubTreatmentDAO(), apptService);
        Bill result = service.generateAndSaveBill("APT-2026-0001", 1);

        assertSame(existingBill, result);
        assertFalse(billDAO.generateCalled);
    }

    @Test
    void fallsBackToPrimaryTreatmentWhenTreatmentListIsEmpty() {
        StubBillDAO billDAO = new StubBillDAO();
        StubAppointmentService apptService = new StubAppointmentService();
        Appointment appt = scheduledAppointment();
        appt.setTreatmentId(7);
        apptService.appointment = appt;
        apptService.treatments = Collections.emptyList();

        BillingService service = new BillingService(billDAO, new StubTreatmentDAO(), apptService);
        Bill bill = service.generateAndSaveBill("APT-2026-0001", 0);

        assertNotNull(bill);
        assertEquals(5500.0, bill.getTreatmentCost(), 0.001);
        assertEquals(6000.0, bill.getTotalBill(), 0.001);
        assertTrue(billDAO.generateCalled);
    }
}
