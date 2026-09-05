package com.sunrise.rest;

import com.sunrise.dao.DentistDAO;
import com.sunrise.dto.DentistDTO;
import com.sunrise.dto.ErrorResponseDTO;
import com.sunrise.model.Dentist;
import com.sunrise.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Path("/api/dentists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DentistResource {
    private final DentistDAO dentistDAO = new DentistDAO();

    @GET
    public Response getAllDentists(@Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        List<Dentist> dentists = dentistDAO.getAllDentists();
        List<DentistDTO> dtoList = new ArrayList<>();
        for (Dentist dentist : dentists) {
            dtoList.add(convertToDTO(dentist));
        }

        return Response.ok(dtoList).build();
    }

    @GET
    @Path("/{dentistId}")
    public Response getDentistById(@PathParam("dentistId") int dentistId, 
                                   @Context HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponseDTO(401, "Unauthorized", "Authentication required"))
                    .build();
        }

        if (dentistId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Bad Request", "Invalid dentist ID"))
                    .build();
        }

        Dentist dentist = dentistDAO.getDentistById(dentistId);
        if (dentist == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponseDTO(404, "Not Found", "Dentist was not found"))
                    .build();
        }

        return Response.ok(convertToDTO(dentist)).build();
    }

    private DentistDTO convertToDTO(Dentist dentist) {
        return new DentistDTO(dentist.getDentistId(), dentist.getDentistName(), dentist.getSpecialization());
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }
}
