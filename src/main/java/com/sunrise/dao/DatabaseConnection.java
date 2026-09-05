package com.sunrise.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static volatile String lastFailure;
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/sunrise_dentall";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "2002De@$";

    private DatabaseConnection() {
        // Utility class: prevent instantiation.
    }

    public static Connection getConnection() {
        try {
            loadDriver();
            String url = getSetting("DB_URL", DEFAULT_URL);
            String user = getSetting("DB_USER", DEFAULT_USER);
            String password = getSetting("DB_PASSWORD", DEFAULT_PASSWORD);
            Connection connection = DriverManager.getConnection(url, user, password);
            // One-time idempotent migration (creates appointment_treatments,
            // refreshes the treatment price list, etc.) so newly deployed
            // builds work on databases created by older schema versions.
            SchemaInitializer.ensureSchema(connection);
            lastFailure = null;
            LOGGER.info("PostgreSQL connection established for " + url + " as " + user);
            return connection;
        } catch (ClassNotFoundException e) {
            lastFailure = "PostgreSQL JDBC driver (org.postgresql.Driver) is missing from the runtime classpath. "
                    + "Add the postgresql-*.jar to WEB-INF/lib of the deployed application.";
            LOGGER.log(Level.SEVERE, lastFailure, e);
        } catch (SQLException e) {
            lastFailure = e.getMessage();
            LOGGER.log(Level.SEVERE, "Database connection failed for configured PostgreSQL URL", e);
        }
        return null;
    }

    /**
     * Explicitly registers the PostgreSQL driver before opening a connection.
     * This is a safety net: some servlet containers do not perform the JDBC 4.0
     * ServiceLoader auto-registration for drivers packed inside a web application.
     */
    private static void loadDriver() throws ClassNotFoundException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                Class.forName("org.postgresql.Driver", true, contextLoader);
            } else {
                throw e;
            }
        }
    }

    public static boolean isDatabaseAvailable() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getLastFailure() {
        return lastFailure;
    }

    private static String getSetting(String name, String defaultValue) {
        String systemValue = System.getProperty(name);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }
        return System.getenv().getOrDefault(name, defaultValue);
    }
}
