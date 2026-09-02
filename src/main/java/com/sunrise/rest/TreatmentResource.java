package com.sunrise.rest;

import com.sunrise.dao.TreatmentDAO;
import com.sunrise.dto.TreatmentDTO;
import com.sunrise.dto.ErrorResponseDTO;
import com.sunrise.model.Treatment;
import com.sunrise.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Path("/api/treatments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TreatmentResource {
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    @GET
    public Response getAllTreatments(@Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        List<Treatment> treatments = treatmentDAO.getAllTreatments();
        List<TreatmentDTO> dtoList = new ArrayList<>();
        for (Treatment treatment : treatments) {
            dtoList.add(convertToDTO(treatment));
        }

        return Response.ok(dtoList).build();
    }

    @GET
    @Path("/{treatmentId}")
    public Response getTreatmentById(@PathParam("treatmentId") int treatmentId, 
                                     @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        if (treatmentId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Invalid treatment ID"))
                    .build();
        }

        Treatment treatment = treatmentDAO.getTreatmentById(treatmentId);
        if (treatment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponseDTO(404, "Not Found", "Treatment was not found"))
                    .build();
        }

        return Response.ok(convertToDTO(treatment)).build();
    }

    private TreatmentDTO convertToDTO(Treatment treatment) {
        return new TreatmentDTO(treatment.getTreatmentId(), treatment.getTreatmentName(), treatment.getCost());
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }
}
