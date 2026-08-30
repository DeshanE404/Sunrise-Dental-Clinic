package com.sunrise.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database credentials and URL (Update according to the local postgres setup)
    private static final String URL = "jdbc:postgresql://localhost:5432/sunrise_dental";
    private static final String USER = "postgres";
    private static final String PASSWORD = "2002De@$"; // Update password accordingly

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
