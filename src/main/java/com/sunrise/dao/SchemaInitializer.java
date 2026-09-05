package com.sunrise.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SchemaInitializer {
    private static final Logger LOGGER = Logger.getLogger(SchemaInitializer.class.getName());
    private static volatile boolean initialized = false;
    private static final Object LOCK = new Object();

    private static final String CREATE_JUNCTION =
            "CREATE TABLE IF NOT EXISTS appointment_treatments (\n"
            + "    appointment_no VARCHAR(30) NOT NULL REFERENCES appointments(appointment_no) ON DELETE CASCADE,\n"
            + "    treatment_id INT NOT NULL REFERENCES treatments(treatment_id) ON DELETE CASCADE,\n"
            + "    PRIMARY KEY (appointment_no, treatment_id)\n"
            + ");";

    private static final String BACKFILL_JUNCTION =
            "INSERT INTO appointment_treatments (appointment_no, treatment_id)\n"
            + "SELECT a.appointment_no, a.treatment_id\n"
            + "FROM appointments a\n"
            + "WHERE a.treatment_id IS NOT NULL\n"
            + "ON CONFLICT DO NOTHING;";

    private static final String RENAME_CLEANING =
            "UPDATE treatments SET treatment_name = 'Teeth Cleaning / Scaling'\n"
            + "WHERE LOWER(TRIM(treatment_name)) = 'cleaning'\n"
            + "  AND NOT EXISTS (SELECT 1 FROM treatments t2 WHERE LOWER(TRIM(t2.treatment_name)) = 'teeth cleaning / scaling');";

    private static final String RENAME_WHITENING =
            "UPDATE treatments SET treatment_name = 'Teeth Whitening'\n"
            + "WHERE LOWER(TRIM(treatment_name)) = 'whitening'\n"
            + "  AND NOT EXISTS (SELECT 1 FROM treatments t2 WHERE LOWER(TRIM(t2.treatment_name)) = 'teeth whitening');";

    private static final String RENAME_FILLING =
            "UPDATE treatments SET treatment_name = 'Tooth-Colored Filling'\n"
            + "WHERE LOWER(TRIM(treatment_name)) = 'filling'\n"
            + "  AND NOT EXISTS (SELECT 1 FROM treatments t2 WHERE LOWER(TRIM(t2.treatment_name)) = 'tooth-colored filling');";

    private static final String RENAME_ROOT_CANAL =
            "UPDATE treatments SET treatment_name = 'Root Canal Treatment - Molar'\n"
            + "WHERE LOWER(TRIM(treatment_name)) = 'root canal'\n"
            + "  AND NOT EXISTS (SELECT 1 FROM treatments t2 WHERE LOWER(TRIM(t2.treatment_name)) = 'root canal treatment - molar');";

    private static final String DELETE_DUPLICATES =
            "DELETE FROM treatments a USING treatments b\n"
            + "WHERE a.treatment_id > b.treatment_id\n"
            + "  AND LOWER(TRIM(a.treatment_name)) = LOWER(TRIM(b.treatment_name));";

    private static final String ADD_UNIQUE_CONSTRAINT =
            "DO $$ BEGIN\n"
            + "    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_treatments_treatment_name') THEN\n"
            + "        ALTER TABLE treatments ADD CONSTRAINT uq_treatments_treatment_name UNIQUE (treatment_name);\n"
            + "    END IF;\n"
            + "END $$;";

    private static final String UPSERT_TREATMENTS =
            "INSERT INTO treatments (treatment_name, cost) VALUES\n"
            + "('Dental Consultation', 1500),\n"
            + "('Dental X-Ray (per image)', 1750),\n"
            + "('Full Mouth X-Ray', 4000),\n"
            + "('Teeth Cleaning / Scaling', 4500),\n"
            + "('Deep Cleaning', 8500),\n"
            + "('Tooth-Colored Filling', 5000),\n"
            + "('Temporary Filling', 2250),\n"
            + "('Dental Sealant', 3000),\n"
            + "('Fluoride Treatment', 3000),\n"
            + "('Root Canal Treatment - Front Tooth', 20000),\n"
            + "('Root Canal Treatment - Premolar', 25000),\n"
            + "('Root Canal Treatment - Molar', 32500),\n"
            + "('Tooth Extraction', 5500),\n"
            + "('Surgical Tooth Extraction', 14000),\n"
            + "('Wisdom Tooth Extraction', 17500),\n"
            + "('Metal Dental Crown', 20000),\n"
            + "('Porcelain Crown', 32500),\n"
            + "('Zirconia Crown', 45000),\n"
            + "('Dental Bridge', 45000),\n"
            + "('Complete Denture', 60000),\n"
            + "('Partial Denture', 42500),\n"
            + "('Flexible Denture', 52500),\n"
            + "('Dental Implant', 150000),\n"
            + "('Implant Crown', 75000),\n"
            + "('Teeth Whitening', 32500),\n"
            + "('Composite Veneer', 15000),\n"
            + "('Porcelain Veneer', 45000),\n"
            + "('Braces - Metal', 140000),\n"
            + "('Braces - Ceramic', 200000),\n"
            + "('Retainer', 17500),\n"
            + "('Children''s Dental Checkup', 1500),\n"
            + "('Children''s Filling', 3750),\n"
            + "('Children''s Tooth Extraction', 3500),\n"
            + "('Emergency Dental Treatment', 6500)\n"
            + "ON CONFLICT (treatment_name) DO UPDATE SET cost = EXCLUDED.cost;";

    private static final String DELETE_LEFT_OVER_TREATMENTS =
            "DELETE FROM treatments t\n"
            + "WHERE t.treatment_name NOT IN (\n"
            + "    'Dental Consultation','Dental X-Ray (per image)','Full Mouth X-Ray',\n"
            + "    'Teeth Cleaning / Scaling','Deep Cleaning','Tooth-Colored Filling',\n"
            + "    'Temporary Filling','Dental Sealant','Fluoride Treatment',\n"
            + "    'Root Canal Treatment - Front Tooth','Root Canal Treatment - Premolar',\n"
            + "    'Root Canal Treatment - Molar','Tooth Extraction','Surgical Tooth Extraction',\n"
            + "    'Wisdom Tooth Extraction','Metal Dental Crown','Porcelain Crown','Zirconia Crown',\n"
            + "    'Dental Bridge','Complete Denture','Partial Denture','Flexible Denture',\n"
            + "    'Dental Implant','Implant Crown','Teeth Whitening','Composite Veneer',\n"
            + "    'Porcelain Veneer','Braces - Metal','Braces - Ceramic','Retainer',\n"
            + "    'Children''s Dental Checkup','Children''s Filling','Children''s Tooth Extraction',\n"
            + "    'Emergency Dental Treatment'\n"
            + ")\n"
            + "AND NOT EXISTS (SELECT 1 FROM appointments a WHERE a.treatment_id = t.treatment_id);";


    private static final String CREATE_REMEMBER_TOKENS =
            "CREATE TABLE IF NOT EXISTS remember_tokens (\n"
            + "    id SERIAL PRIMARY KEY,\n"
            + "    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,\n"
            + "    token_hash VARCHAR(64) NOT NULL UNIQUE,\n"
            + "    expires_at TIMESTAMP NOT NULL,\n"
            + "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n"
            + ");";

    private static final String CREATE_REMEMBER_TOKENS_INDEX =
            "CREATE INDEX IF NOT EXISTS idx_remember_tokens_token_hash ON remember_tokens (token_hash);";

    private SchemaInitializer() {
    }

    public static void ensureSchema(Connection connection) {
        if (initialized) {
            return;
        }
        synchronized (LOCK) {
            if (initialized) {
                return;
            }
            try {
                run(connection, CREATE_JUNCTION);
                run(connection, CREATE_REMEMBER_TOKENS);
                run(connection, CREATE_REMEMBER_TOKENS_INDEX);
                run(connection, BACKFILL_JUNCTION);
                run(connection, RENAME_CLEANING);
                run(connection, RENAME_WHITENING);
                run(connection, RENAME_FILLING);
                run(connection, RENAME_ROOT_CANAL);
                run(connection, DELETE_DUPLICATES);
                run(connection, ADD_UNIQUE_CONSTRAINT);
                run(connection, UPSERT_TREATMENTS);
                run(connection, DELETE_LEFT_OVER_TREATMENTS);
                initialized = true;
                LOGGER.info("Schema auto-upgrade completed successfully.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Schema auto-upgrade failed. " + e.getMessage(), e);
            }
        }
    }

    private static void run(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
