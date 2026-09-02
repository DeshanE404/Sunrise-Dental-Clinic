package com.sunrise.rest;

import com.sunrise.dto.AppointmentDTO;
import com.sunrise.dto.ErrorResponseDTO;
import com.sunrise.model.Appointment;
import com.sunrise.model.User;
import com.sunrise.service.AppointmentService;
import com.sunrise.service.PatientService;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/api/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppointmentResource {
    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();

    @GET
    public Response getAllAppointments(@Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        List<Appointment> appointments = appointmentService.getAllAppointments();
        List<AppointmentDTO> dtoList = new ArrayList<>();
        for (Appointment appt : appointments) {
            dtoList.add(convertToDTO(appt));
        }

        return Response.ok(dtoList).build();
    }

    @GET
    @Path("/{appointmentNo}")
    public Response getAppointmentByNumber(@PathParam("appointmentNo") String appointmentNo, 
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

        Appointment appointment = appointmentService.getAppointmentDetails(appointmentNo);
        if (appointment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponseDTO(404, "Not Found", "Appointment was not found"))
                    .build();
        }

        return Response.ok(convertToDTO(appointment)).build();
    }

    @POST
    public Response createAppointment(AppointmentDTO dto, @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        // Basic validation
        if (dto.getAppointmentNumber() == null || dto.getAppointmentNumber().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Appointment number is required"))
                    .build();
        }

        if (dto.getPatientId() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Patient must be selected"))
                    .build();
        }

        if (dto.getDentistName() == null || dto.getDentistName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Dentist name is required"))
                    .build();
        }

        if (dto.getTreatmentId() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Treatment must be selected"))
                    .build();
        }

        if (dto.getAppointmentDate() == null || dto.getAppointmentDate().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Appointment date is required"))
                    .build();
        }

        // Convert DTO to model
        Appointment appointment = new Appointment();
        appointment.setAppointmentNo(dto.getAppointmentNumber().trim());
        appointment.setPatientId(dto.getPatientId());
        appointment.setDentistName(dto.getDentistName().trim());
        appointment.setTreatmentId(dto.getTreatmentId());

        try {
            // Parse appointment date and time
            String dateTime = dto.getAppointmentDate();
            if (dto.getAppointmentTime() != null && !dto.getAppointmentTime().trim().isEmpty()) {
                dateTime = dateTime + " " + dto.getAppointmentTime();
            } else {
                dateTime = dateTime + " 10:00:00";
            }
            appointment.setAppointmentDate(Timestamp.valueOf(dateTime));
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Invalid date/time format"))
                    .build();
        }

        if (dto.getStatus() != null) {
            appointment.setStatus(dto.getStatus().trim().toUpperCase());
        }

        String validationError = appointmentService.validateAppointment(appointment);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", validationError))
                    .build();
        }

        if (appointmentService.createAppointment(appointment)) {
            return Response.status(Response.Status.CREATED)
                    .entity(convertToDTO(appointment))
                    .build();
        }

        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponseDTO(409, "Conflict", "Appointment already exists or dentist is not available"))
                .build();
    }

    @PUT
    @Path("/{appointmentNo}")
    public Response updateAppointment(@PathParam("appointmentNo") String appointmentNo, 
                                     AppointmentDTO dto, 
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

        Appointment existing = appointmentService.getAppointmentDetails(appointmentNo);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponseDTO(404, "Not Found", "Appointment was not found"))
                    .build();
        }

        // Update fields
        if (dto.getPatientId() > 0) {
            existing.setPatientId(dto.getPatientId());
        }
        if (dto.getDentistName() != null && !dto.getDentistName().trim().isEmpty()) {
            existing.setDentistName(dto.getDentistName().trim());
        }
        if (dto.getTreatmentId() > 0) {
            existing.setTreatmentId(dto.getTreatmentId());
        }
        if (dto.getAppointmentDate() != null && !dto.getAppointmentDate().trim().isEmpty()) {
            try {
                String dateTime = dto.getAppointmentDate();
                if (dto.getAppointmentTime() != null && !dto.getAppointmentTime().trim().isEmpty()) {
                    dateTime = dateTime + " " + dto.getAppointmentTime();
                } else {
                    dateTime = dateTime + " 10:00:00";
                }
                existing.setAppointmentDate(Timestamp.valueOf(dateTime));
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponseDTO(400, "Bad Request", "Invalid date/time format"))
                        .build();
            }
        }
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            existing.setStatus(dto.getStatus().trim().toUpperCase());
        }

        String validationError = appointmentService.validateAppointment(existing);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", validationError))
                    .build();
        }

        if (appointmentService.updateAppointment(existing)) {
            return Response.ok(convertToDTO(existing)).build();
        }

        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponseDTO(409, "Conflict", "Cannot update appointment - dentist not available"))
                .build();
    }

    @DELETE
    @Path("/{appointmentNo}")
    public Response deleteAppointment(@PathParam("appointmentNo") String appointmentNo, 
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

        Appointment appointment = appointmentService.getAppointmentDetails(appointmentNo);
        if (appointment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponseDTO(404, "Not Found", "Appointment was not found"))
                    .build();
        }

        if (appointmentService.deleteAppointment(appointmentNo)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponseDTO(500, "Internal Server Error", "Failed to delete appointment"))
                .build();
    }

    private AppointmentDTO convertToDTO(Appointment appt) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setAppointmentNumber(appt.getAppointmentNo());
        dto.setPatientId(appt.getPatientId());
        dto.setPatientName(appt.getPatientName());
        dto.setPatientContact(appt.getPatientContact());
        dto.setAddress(appt.getPatientContact());
        dto.setDentistName(appt.getDentistName());
        dto.setTreatmentId(appt.getTreatmentId());
        dto.setTreatmentName(appt.getTreatmentName());
        dto.setTreatmentCost(appt.getTreatmentCost());
        if (appt.getAppointmentDate() != null) {
            String dateTime = appt.getAppointmentDate().toString();
            String[] parts = dateTime.split(" ");
            dto.setAppointmentDate(parts[0]);
            if (parts.length > 1) {
                dto.setAppointmentTime(parts[1]);
            }
        }
        dto.setStatus(appt.getStatus());
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
