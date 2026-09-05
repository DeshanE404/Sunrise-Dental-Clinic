-- schema.sql
-- Create database manually in PostgreSQL: CREATE DATABASE sunrise_dentall;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    employee_number VARCHAR(50) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL -- 'ADMIN' or 'RECEPTION'
);

-- Persistent "remember me" login tokens (keeps users logged in across server restarts).
-- Only a SHA-256 hash of the cookie token is stored.
CREATE TABLE IF NOT EXISTS remember_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_remember_tokens_token_hash
    ON remember_tokens (token_hash);


CREATE TABLE IF NOT EXISTS patients (
    patient_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dentists (
    dentist_id SERIAL PRIMARY KEY,
    dentist_name VARCHAR(100) NOT NULL UNIQUE,
    specialization VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS treatments (
    treatment_id SERIAL PRIMARY KEY,
    treatment_name VARCHAR(100) NOT NULL,
    cost NUMERIC(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS appointments (
    appointment_id SERIAL PRIMARY KEY,
    appointment_no VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT REFERENCES patients(patient_id) ON DELETE CASCADE,
    dentist_name VARCHAR(100) NOT NULL,
    treatment_id INT REFERENCES treatments(treatment_id) ON DELETE SET NULL,
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bills (
    bill_no SERIAL PRIMARY KEY,
    appointment_no VARCHAR(30) NOT NULL UNIQUE REFERENCES appointments(appointment_no) ON DELETE CASCADE,
    consultation_fee NUMERIC(10, 2) NOT NULL,
    treatment_cost NUMERIC(10, 2) NOT NULL,
    total_bill NUMERIC(10, 2) NOT NULL,
    billing_date TIMESTAMP NOT NULL
);

-- Appointment audit trail (records every insert/update/cancel/delete via trigger).
CREATE TABLE IF NOT EXISTS appointment_audit (
    audit_id SERIAL PRIMARY KEY,
    appointment_no VARCHAR(30) NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'CANCEL', 'DELETE')),
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note TEXT
);

-- A single appointment can now have MULTIPLE treatments. This join table
-- stores every treatment registered for an appointment. The old
-- appointments.treatment_id column is kept as the "primary" treatment for
-- backward compatibility with the REST API and existing reports.
CREATE TABLE IF NOT EXISTS appointment_treatments (
    appointment_no VARCHAR(30) NOT NULL REFERENCES appointments(appointment_no) ON DELETE CASCADE,
    treatment_id INT NOT NULL REFERENCES treatments(treatment_id) ON DELETE CASCADE,
    PRIMARY KEY (appointment_no, treatment_id)
);

-- Populate the join table from existing appointments that were created while
-- the system only supported a single treatment per appointment.
INSERT INTO appointment_treatments (appointment_no, treatment_id)
SELECT a.appointment_no, a.treatment_id
FROM appointments a
WHERE a.treatment_id IS NOT NULL
ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_appointment_treatments_appointment
    ON appointment_treatments (appointment_no);

-- Upgrade existing databases created before appointment status tracking was added.
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED';

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_appointments_dentist_datetime
    ON appointments (dentist_name, appointment_date);

CREATE UNIQUE INDEX IF NOT EXISTS idx_appointments_single_booking
    ON appointments (dentist_name, appointment_date)
    WHERE status <> 'CANCELLED';

CREATE UNIQUE INDEX IF NOT EXISTS idx_bills_appointment_no_unique
    ON bills (appointment_no);

CREATE INDEX IF NOT EXISTS idx_appointments_status
    ON appointments (status);

CREATE OR REPLACE FUNCTION calculate_bill_total(p_consultation_fee NUMERIC, p_treatment_cost NUMERIC)
RETURNS NUMERIC
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_consultation_fee < 0 OR p_treatment_cost < 0 THEN
        RAISE EXCEPTION 'Consultation fee and treatment cost must be non-negative';
    END IF;

    RETURN p_consultation_fee + p_treatment_cost;
END;
$$;

CREATE OR REPLACE FUNCTION log_appointment_audit()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO appointment_audit (appointment_no, action, old_status, new_status, note)
        VALUES (NEW.appointment_no, 'INSERT', NULL, NEW.status, 'Appointment created');
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        IF NEW.status = 'CANCELLED' AND OLD.status <> 'CANCELLED' THEN
            INSERT INTO appointment_audit (appointment_no, action, old_status, new_status, note)
            VALUES (NEW.appointment_no, 'CANCEL', OLD.status, NEW.status, 'Appointment cancelled');
        ELSE
            INSERT INTO appointment_audit (appointment_no, action, old_status, new_status, note)
            VALUES (NEW.appointment_no, 'UPDATE', OLD.status, NEW.status, 'Appointment updated');
        END IF;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO appointment_audit (appointment_no, action, old_status, new_status, note)
        VALUES (OLD.appointment_no, 'DELETE', OLD.status, NULL, 'Appointment deleted');
        RETURN OLD;
    END IF;

    RETURN NULL;
END;
$$;

CREATE TRIGGER trg_appointment_audit
AFTER INSERT OR UPDATE OF patient_id, dentist_name, treatment_id, appointment_date, status OR DELETE
ON appointments
FOR EACH ROW
EXECUTE FUNCTION log_appointment_audit();

-- Default admin and reception users will be created via API / Postman.

INSERT INTO dentists (dentist_name, specialization) VALUES
('Dr. Perera', 'General Dentistry'),
('Dr. Silva', 'Orthodontics'),
('Dr. Fernando', 'Cosmetic Dentistry')
ON CONFLICT (dentist_name) DO NOTHING;

-- =====================================================================
-- TREATMENTS - Sunrise Dental Clinic price list (LKR / Rs.)
-- Each price is a representative figure from the clinic's published
-- treatment price range (minimum + maximum divided by two).
-- =====================================================================

-- Make sure legacy default rows ('Cleaning', 'Whitening', 'Filling',
-- 'Root Canal') are merged into the new standard list without breaking
-- any existing appointments that reference them.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'cleaning') THEN
        IF NOT EXISTS (SELECT 1 FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'teeth cleaning / scaling') THEN
            UPDATE treatments SET treatment_name = 'Teeth Cleaning / Scaling' WHERE LOWER(TRIM(treatment_name)) = 'cleaning';
        ELSE
            DELETE FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'cleaning'
                AND NOT EXISTS (SELECT 1 FROM appointments a WHERE a.treatment_id = treatments.treatment_id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'whitening') THEN
        IF NOT EXISTS (SELECT 1 FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'teeth whitening') THEN
            UPDATE treatments SET treatment_name = 'Teeth Whitening' WHERE LOWER(TRIM(treatment_name)) = 'whitening';
        ELSE
            DELETE FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'whitening'
                AND NOT EXISTS (SELECT 1 FROM appointments a WHERE a.treatment_id = treatments.treatment_id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'filling') THEN
        IF NOT EXISTS (SELECT 1 FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'tooth-colored filling') THEN
            UPDATE treatments SET treatment_name = 'Tooth-Colored Filling' WHERE LOWER(TRIM(treatment_name)) = 'filling';
        ELSE
            DELETE FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'filling'
                AND NOT EXISTS (SELECT 1 FROM appointments a WHERE a.treatment_id = treatments.treatment_id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'root canal') THEN
        IF NOT EXISTS (SELECT 1 FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'root canal treatment - molar') THEN
            UPDATE treatments SET treatment_name = 'Root Canal Treatment - Molar' WHERE LOWER(TRIM(treatment_name)) = 'root canal';
        ELSE
            DELETE FROM treatments WHERE LOWER(TRIM(treatment_name)) = 'root canal'
                AND NOT EXISTS (SELECT 1 FROM appointments a WHERE a.treatment_id = treatments.treatment_id);
        END IF;
    END IF;
END $$;

-- Remove any exact duplicates that may have been created by earlier schema runs.
DELETE FROM treatments a USING treatments b
WHERE a.treatment_id > b.treatment_id
  AND LOWER(TRIM(a.treatment_name)) = LOWER(TRIM(b.treatment_name));

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_treatments_treatment_name') THEN
        ALTER TABLE treatments ADD CONSTRAINT uq_treatments_treatment_name UNIQUE (treatment_name);
    END IF;
END $$;

-- Insert / refresh the complete treatment price list.
INSERT INTO treatments (treatment_name, cost) VALUES
('Dental Consultation', 1500),
('Dental X-Ray (per image)', 1750),
('Full Mouth X-Ray', 4000),
('Teeth Cleaning / Scaling', 4500),
('Deep Cleaning', 8500),
('Tooth-Colored Filling', 5000),
('Temporary Filling', 2250),
('Dental Sealant', 3000),
('Fluoride Treatment', 3000),
('Root Canal Treatment - Front Tooth', 20000),
('Root Canal Treatment - Premolar', 25000),
('Root Canal Treatment - Molar', 32500),
('Tooth Extraction', 5500),
('Surgical Tooth Extraction', 14000),
('Wisdom Tooth Extraction', 17500),
('Metal Dental Crown', 20000),
('Porcelain Crown', 32500),
('Zirconia Crown', 45000),
('Dental Bridge', 45000),
('Complete Denture', 60000),
('Partial Denture', 42500),
('Flexible Denture', 52500),
('Dental Implant', 150000),
('Implant Crown', 75000),
('Teeth Whitening', 32500),
('Composite Veneer', 15000),
('Porcelain Veneer', 45000),
('Braces - Metal', 140000),
('Braces - Ceramic', 200000),
('Retainer', 17500),
('Children''s Dental Checkup', 1500),
('Children''s Filling', 3750),
('Children''s Tooth Extraction', 3500),
('Emergency Dental Treatment', 6500)
ON CONFLICT (treatment_name) DO UPDATE SET cost = EXCLUDED.cost;

-- Drop any leftover treatments that are not part of the official list and are
-- not referenced by any existing appointment.
DELETE FROM treatments t
WHERE t.treatment_name NOT IN (
    'Dental Consultation',
    'Dental X-Ray (per image)',
    'Full Mouth X-Ray',
    'Teeth Cleaning / Scaling',
    'Deep Cleaning',
    'Tooth-Colored Filling',
    'Temporary Filling',
    'Dental Sealant',
    'Fluoride Treatment',
    'Root Canal Treatment - Front Tooth',
    'Root Canal Treatment - Premolar',
    'Root Canal Treatment - Molar',
    'Tooth Extraction',
    'Surgical Tooth Extraction',
    'Wisdom Tooth Extraction',
    'Metal Dental Crown',
    'Porcelain Crown',
    'Zirconia Crown',
    'Dental Bridge',
    'Complete Denture',
    'Partial Denture',
    'Flexible Denture',
    'Dental Implant',
    'Implant Crown',
    'Teeth Whitening',
    'Composite Veneer',
    'Porcelain Veneer',
    'Braces - Metal',
    'Braces - Ceramic',
    'Retainer',
    'Children''s Dental Checkup',
    'Children''s Filling',
    'Children''s Tooth Extraction',
    'Emergency Dental Treatment'
)
AND NOT EXISTS (SELECT 1 FROM appointments a WHERE a.treatment_id = t.treatment_id);
