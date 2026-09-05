package com.sunrise.rest;

import com.sunrise.dto.BillDTO;
import com.sunrise.dto.ErrorResponseDTO;
import com.sunrise.model.Bill;
import com.sunrise.model.User;
import com.sunrise.service.BillingService;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;

@Path("/api/bills")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BillResource {
    private final BillingService billingService = new BillingService();

    @GET
    @Path("/{billId}")
    public Response getBillById(@PathParam("billId") int billId, @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        // Note: Current DAO doesn't support retrieval by bill ID, only by appointment number
        // This is a placeholder that would require DAO extension
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(new ErrorResponseDTO(501, "Not Implemented", "Bill retrieval by ID not yet implemented"))
                .build();
    }

    @GET
    @Path("/appointment/{appointmentNo}")
    public Response getBillByAppointment(@PathParam("appointmentNo") String appointmentNo, 
                                         @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Appointment number is required"))
                    .build();
        }

        Bill bill = billingService.getBill(appointmentNo);
        if (bill == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponseDTO(404, "Not Found", "Bill was not found"))
                    .build();
        }

        return Response.ok(convertToDTO(bill)).build();
    }

    private BillDTO convertToDTO(Bill bill) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        BillDTO dto = new BillDTO();
        dto.setBillNo(bill.getBillNo());
        dto.setAppointmentNo(bill.getAppointmentNo());
        dto.setConsultationFee(bill.getConsultationFee());
        dto.setTreatmentCost(bill.getTreatmentCost());
        dto.setTotalBill(bill.getTotalBill());
        if (bill.getBillingDate() != null) {
            dto.setBillingDate(sdf.format(bill.getBillingDate()));
        }
        return dto;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }
}
