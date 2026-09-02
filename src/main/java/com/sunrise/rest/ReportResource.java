package com.sunrise.rest;

import com.sunrise.dto.ErrorResponseDTO;
import com.sunrise.model.*;
import com.sunrise.service.ReportService;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportResource {
    private final ReportService reportService = new ReportService();

    @GET
    @Path("/appointments")
    public Response getDailyAppointmentsReport(@QueryParam("date") String dateParam, 
                                               @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        if (!isAdmin(user)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponseDTO(403, "Forbidden", "Only administrators can access reports"))
                    .build();
        }

        LocalDate reportDate;
        try {
            reportDate = dateParam != null && !dateParam.trim().isEmpty() 
                ? LocalDate.parse(dateParam) 
                : LocalDate.now();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Invalid date format. Use YYYY-MM-DD"))
                    .build();
        }

        List<DailyAppointmentReport> appointments = reportService.getDailyAppointments(reportDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("reportDate", reportDate);
        response.put("appointmentCount", appointments.size());
        response.put("appointments", appointments);
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/dentist-workload")
    public Response getDentistWorkloadReport(@QueryParam("from") String fromParam, 
                                             @QueryParam("to") String toParam, 
                                             @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        if (!isAdmin(user)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponseDTO(403, "Forbidden", "Only administrators can access reports"))
                    .build();
        }

        LocalDate startDate, endDate;
        try {
            if (fromParam != null && !fromParam.trim().isEmpty()) {
                startDate = LocalDate.parse(fromParam);
            } else {
                startDate = LocalDate.now().minusDays(6);
            }
            
            if (toParam != null && !toParam.trim().isEmpty()) {
                endDate = LocalDate.parse(toParam);
            } else {
                endDate = LocalDate.now();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Invalid date format. Use YYYY-MM-DD"))
                    .build();
        }

        List<DentistWorkloadReport> workload = reportService.getDentistWorkload(startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("dentistCount", workload.size());
        response.put("dentists", workload);
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/treatments")
    public Response getTreatmentStatisticsReport(@QueryParam("from") String fromParam, 
                                                 @QueryParam("to") String toParam, 
                                                 @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        if (!isAdmin(user)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponseDTO(403, "Forbidden", "Only administrators can access reports"))
                    .build();
        }

        LocalDate startDate, endDate;
        try {
            if (fromParam != null && !fromParam.trim().isEmpty()) {
                startDate = LocalDate.parse(fromParam);
            } else {
                startDate = LocalDate.now().minusDays(6);
            }
            
            if (toParam != null && !toParam.trim().isEmpty()) {
                endDate = LocalDate.parse(toParam);
            } else {
                endDate = LocalDate.now();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Invalid date format. Use YYYY-MM-DD"))
                    .build();
        }

        List<TreatmentStatisticsReport> statistics = reportService.getTreatmentStatistics(startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("treatmentCount", statistics.size());
        response.put("treatments", statistics);
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/revenue")
    public Response getRevenueReport(@QueryParam("from") String fromParam, 
                                     @QueryParam("to") String toParam, 
                                     @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        if (!isAdmin(user)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponseDTO(403, "Forbidden", "Only administrators can access reports"))
                    .build();
        }

        LocalDate startDate, endDate;
        try {
            if (fromParam != null && !fromParam.trim().isEmpty()) {
                startDate = LocalDate.parse(fromParam);
            } else {
                startDate = LocalDate.now().minusDays(6);
            }
            
            if (toParam != null && !toParam.trim().isEmpty()) {
                endDate = LocalDate.parse(toParam);
            } else {
                endDate = LocalDate.now();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Invalid date format. Use YYYY-MM-DD"))
                    .build();
        }

        List<RevenueReport> revenue = reportService.getRevenueReport(startDate, endDate);
        BigDecimal totalRevenue = reportService.getTotalRevenue();
        
        Map<String, Object> response = new HashMap<>();
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("totalRevenue", totalRevenue);
        response.put("dateWiseRevenue", revenue);
        
        return Response.ok(response).build();
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
