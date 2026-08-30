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
    contact_number VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS treatments (
    treatment_id SERIAL PRIMARY KEY,
    treatment_name VARCHAR(100) NOT NULL,
    cost NUMERIC(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS appointments (
    appointment_no VARCHAR(20) PRIMARY KEY,
    patient_id INT REFERENCES patients(patient_id) ON DELETE CASCADE,
    dentist_name VARCHAR(100) NOT NULL,
    treatment_id INT REFERENCES treatments(treatment_id) ON DELETE SET NULL,
    appointment_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS bills (
    bill_no SERIAL PRIMARY KEY,
    appointment_no VARCHAR(20) REFERENCES appointments(appointment_no) ON DELETE CASCADE,
    consultation_fee NUMERIC(10, 2) NOT NULL,
    treatment_cost NUMERIC(10, 2) NOT NULL,
    total_bill NUMERIC(10, 2) NOT NULL,
    billing_date TIMESTAMP NOT NULL
);

-- Default admin and reception users will be created via API / Postman.

-- Insert default treatments
INSERT INTO treatments (treatment_name, cost) VALUES
('Cleaning', 100.00),
('Whitening', 150.00),
('Filling', 200.00),
('Root Canal', 500.00)
ON CONFLICT DO NOTHING;
