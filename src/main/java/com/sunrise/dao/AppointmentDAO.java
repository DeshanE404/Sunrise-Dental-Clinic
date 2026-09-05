package com.sunrise.dao;

import com.sunrise.model.Appointment;
import com.sunrise.model.Treatment;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AppointmentDAO {

    // Base fragment shared by all read queries. Because an appointment can hold
    // multiple treatments, we aggregate them into a comma separated name list
    // and a total cost. The old appointments.treatment_id (primary treatment)
    // is kept as a fallback for rows created before multi-treatment support.
    private static final String SELECT_BASE =
            "SELECT a.appointment_no, a.patient_id, a.dentist_name, a.treatment_id, "
            + "a.appointment_date, a.status, "
            + "p.name AS patient_name, p.contact_number AS patient_contact, "
            + "COALESCE((SELECT string_agg(t2.treatment_name, ', ' ORDER BY t2.treatment_name) "
            + "          FROM appointment_treatments at2 "
            + "          JOIN treatments t2 ON t2.treatment_id = at2.treatment_id "
            + "          WHERE at2.appointment_no = a.appointment_no), t.treatment_name) AS treatment_name, "
            + "COALESCE((SELECT COALESCE(SUM(t3.cost), 0) "
            + "          FROM appointment_treatments at3 "
            + "          JOIN treatments t3 ON t3.treatment_id = at3.treatment_id "
            + "          WHERE at3.appointment_no = a.appointment_no), t.cost) AS treatment_cost "
            + "FROM appointments a "
            + "JOIN patients p ON a.patient_id = p.patient_id "
            + "LEFT JOIN treatments t ON t.treatment_id = a.treatment_id ";

    public String getNextAppointmentNumber(int year) {
        String query = "SELECT COALESCE(MAX(CAST(SUBSTRING(appointment_no FROM 10 FOR 4) AS INTEGER)), 0) + 1 "
                + "FROM appointments WHERE appointment_no ~ ? AND appointment_no LIKE ?";
        String pattern = "^APT-" + year + "-[0-9]{4}$";
        String prefix = "APT-" + year + "-%";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, pattern);
            preparedStatement.setString(2, prefix);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return String.format("APT-%d-%04d", year, resultSet.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("APT-%d-%04d", year, 1);
    }

    public boolean registerAppointment(Appointment appt) {
        if (appt == null || appt.getAppointmentNo() == null) {
            return false;
        }

        String insertAppt = "INSERT INTO appointments (appointment_no, patient_id, dentist_name, treatment_id, appointment_date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String insertTreatment = "INSERT INTO appointment_treatments (appointment_no, treatment_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        List<Integer> treatmentIds = resolveTreatmentIds(appt);
        Integer primaryId = treatmentIds.isEmpty() ? null : treatmentIds.get(0);

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertAppt)) {
                    preparedStatement.setString(1, appt.getAppointmentNo());
                    preparedStatement.setInt(2, appt.getPatientId());
                    preparedStatement.setString(3, appt.getDentistName());
                    if (primaryId == null) {
                        preparedStatement.setNull(4, Types.INTEGER);
                    } else {
                        preparedStatement.setInt(4, primaryId);
                    }
                    preparedStatement.setTimestamp(5, appt.getAppointmentDate());
                    preparedStatement.setString(6, appt.getStatus() == null ? "SCHEDULED" : appt.getStatus().trim().toUpperCase());
                    preparedStatement.executeUpdate();
                }

                insertJunctionTreatments(connection, insertTreatment, appt.getAppointmentNo(), treatmentIds);

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAppointment(Appointment appt) {
        if (appt == null || appt.getAppointmentNo() == null) {
            return false;
        }

        String updateAppt = "UPDATE appointments SET patient_id = ?, dentist_name = ?, treatment_id = ?, appointment_date = ?, status = ? "
                + "WHERE appointment_no = ?";
        String deleteTreatments = "DELETE FROM appointment_treatments WHERE appointment_no = ?";
        String insertTreatment = "INSERT INTO appointment_treatments (appointment_no, treatment_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        List<Integer> treatmentIds = resolveTreatmentIds(appt);
        Integer primaryId = treatmentIds.isEmpty() ? null : treatmentIds.get(0);

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateAppt)) {
                    preparedStatement.setInt(1, appt.getPatientId());
                    preparedStatement.setString(2, appt.getDentistName());
                    if (primaryId == null) {
                        preparedStatement.setNull(3, Types.INTEGER);
                    } else {
                        preparedStatement.setInt(3, primaryId);
                    }
                    preparedStatement.setTimestamp(4, appt.getAppointmentDate());
                    preparedStatement.setString(5, appt.getStatus() == null ? "SCHEDULED" : appt.getStatus().trim().toUpperCase());
                    preparedStatement.setString(6, appt.getAppointmentNo());
                    preparedStatement.executeUpdate();
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteTreatments)) {
                    preparedStatement.setString(1, appt.getAppointmentNo());
                    preparedStatement.executeUpdate();
                }

                insertJunctionTreatments(connection, insertTreatment, appt.getAppointmentNo(), treatmentIds);

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void insertJunctionTreatments(Connection connection, String insertSql, String appointmentNo, List<Integer> treatmentIds)
            throws SQLException {
        if (treatmentIds.isEmpty()) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (Integer id : treatmentIds) {
                if (id == null || id <= 0) {
                    continue;
                }
                preparedStatement.setString(1, appointmentNo);
                preparedStatement.setInt(2, id);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private List<Integer> resolveTreatmentIds(Appointment appt) {
        Set<Integer> ids = new LinkedHashSet<>();
        if (appt.getTreatmentIds() != null) {
            for (Integer id : appt.getTreatmentIds()) {
                if (id != null && id > 0) {
                    ids.add(id);
                }
            }
        }
        if (ids.isEmpty() && appt.getTreatmentId() > 0) {
            ids.add(appt.getTreatmentId());
        }
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> ordered = new ArrayList<>(ids);
        appt.setTreatmentId(ordered.get(0));
        return ordered;
    }

    public boolean deleteAppointment(String appointmentNo) {
        String query = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_no = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appointmentNo);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean appointmentExists(String appointmentNo) {
        String query = "SELECT 1 FROM appointments WHERE appointment_no = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appointmentNo);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isDentistAvailable(String dentistName, Timestamp appointmentDate, String excludeAppointmentNo) {
        String query = "SELECT COUNT(*) AS count FROM appointments WHERE LOWER(dentist_name) = LOWER(?) AND appointment_date = ? AND status <> 'CANCELLED' "
                       + "AND (? IS NULL OR appointment_no <> ? )";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, dentistName.trim());
            preparedStatement.setTimestamp(2, appointmentDate);
            preparedStatement.setString(3, excludeAppointmentNo);
            preparedStatement.setString(4, excludeAppointmentNo);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countAppointmentsByDentist(String dentistName) {
        String query = "SELECT COUNT(*) AS count FROM appointments WHERE LOWER(dentist_name) = LOWER(?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dentistName.trim());
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public Appointment getAppointmentByNo(String appointmentNo) {
        String query = SELECT_BASE + "WHERE a.appointment_no = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appointmentNo);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Appointment appt = mapAppointment(rs);
                    appt.setTreatmentIds(getTreatmentIdsForAppointment(connection, appointmentNo));
                    return appt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String query = SELECT_BASE + "ORDER BY a.appointment_date DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public List<Appointment> getTodayAppointments() {
        List<Appointment> list = new ArrayList<>();
        String query = SELECT_BASE
                + "WHERE DATE(a.appointment_date) = CURRENT_DATE "
                + "ORDER BY a.appointment_date ASC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Treatment> getTreatmentsForAppointment(String appointmentNo) {
        List<Treatment> treatments = new ArrayList<>();
        String query = "SELECT t.treatment_id, t.treatment_name, t.cost "
                + "FROM appointment_treatments at "
                + "JOIN treatments t ON t.treatment_id = at.treatment_id "
                + "WHERE at.appointment_no = ? "
                + "ORDER BY t.treatment_name ASC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, appointmentNo);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Treatment t = new Treatment();
                    t.setTreatmentId(rs.getInt("treatment_id"));
                    t.setTreatmentName(rs.getString("treatment_name"));
                    t.setCost(rs.getDouble("cost"));
                    treatments.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return treatments;
    }

    private List<Integer> getTreatmentIdsForAppointment(Connection connection, String appointmentNo) {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT treatment_id FROM appointment_treatments WHERE appointment_no = ? ORDER BY treatment_id";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, appointmentNo);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("treatment_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment appt = new Appointment();
        appt.setAppointmentNo(rs.getString("appointment_no"));
        appt.setPatientId(rs.getInt("patient_id"));
        appt.setDentistName(rs.getString("dentist_name"));
        appt.setTreatmentId(rs.getInt("treatment_id"));
        appt.setAppointmentDate(rs.getTimestamp("appointment_date"));
        appt.setStatus(rs.getString("status"));

        appt.setPatientName(rs.getString("patient_name"));
        appt.setPatientContact(rs.getString("patient_contact"));
        appt.setTreatmentName(rs.getString("treatment_name"));
        appt.setTreatmentCost(rs.getDouble("treatment_cost"));
        return appt;
    }
}

