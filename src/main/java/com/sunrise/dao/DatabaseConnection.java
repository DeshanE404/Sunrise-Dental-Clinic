package com.sunrise.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/sunrise_dental";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "2002De@$";

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = System.getenv().getOrDefault("DB_URL", DEFAULT_URL);
            String user = System.getenv().getOrDefault("DB_USER", DEFAULT_USER);
            String password = System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
