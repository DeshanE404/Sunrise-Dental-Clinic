-- schema.sql
-- Create database manually in PostgreSQL: CREATE DATABASE sunrise_dental;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    employee_number VARCHAR(50) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL -- 'ADMIN' or 'RECEPTION'
);

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

CREATE TABLE IF NOT EXISTS appointment_audit (
    audit_id SERIAL PRIMARY KEY,
    appointment_no VARCHAR(30) NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'CANCEL', 'DELETE')),
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note TEXT
);

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

-- Insert default treatments
INSERT INTO treatments (treatment_name, cost) VALUES
('Cleaning', 100.00),
('Whitening', 150.00),
('Filling', 200.00),
('Root Canal', 500.00)
ON CONFLICT DO NOTHING;
