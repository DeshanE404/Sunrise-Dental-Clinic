package com.sunrise.controller;

import com.sunrise.model.User;
import com.sunrise.service.DentistService;
import java.io.IOException;
import java.util.List;
import com.sunrise.model.Dentist;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/DentistServlet")
public class DentistServlet extends HttpServlet {
    private final DentistService dentistService = new DentistService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session == null) ? null : (User) session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect("DashboardServlet?error=unauthorized");
            return;
        }

        List<Dentist> dentists = dentistService.getAllDentists();
        request.setAttribute("dentists", dentists);
        request.getRequestDispatcher("manage_dentists.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session == null) ? null : (User) session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect("DashboardServlet?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if ("add".equals(action)) {
            String name = request.getParameter("dentistName");
            String specialization = request.getParameter("specialization");
            if (name == null || name.trim().isEmpty()) {
                response.sendRedirect("DentistServlet?error=name_required");
                return;
            }
            boolean added = dentistService.addDentist(name, specialization);
            if (added) {
                response.sendRedirect("DentistServlet?success=added");
            } else {
                response.sendRedirect("DentistServlet?error=add_failed");
            }
            return;
        }

        if ("delete".equals(action)) {
            String idParam = request.getParameter("dentistId");
            try {
                int dentistId = Integer.parseInt(idParam);
                boolean deleted = dentistService.deleteDentist(dentistId);
                if (deleted) {
                    response.sendRedirect("DentistServlet?success=deleted");
                } else {
                    response.sendRedirect("DentistServlet?error=delete_blocked");
                }
            } catch (NumberFormatException e) {
                response.sendRedirect("DentistServlet?error=invalid");
            }
            return;
        }

        response.sendRedirect("DentistServlet");
    }
}