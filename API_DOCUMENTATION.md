# Sunrise Dental Clinic - REST API Documentation

## Overview

The Sunrise Dental Clinic REST API provides programmatic access to appointment management, billing, dentist information, treatments, and reporting features. The API uses JSON for request/response bodies and follows RESTful principles with proper HTTP status codes.

**Base URL:** `http://localhost:8081/SunriseDentalClinic/api`

**API Version:** 1.0

---

## Authentication

All REST API endpoints require authentication. Clients must have an active session or provide valid credentials.

### Session-Based Authentication (Recommended)
1. Login via the web interface or `/api/appointments` endpoints
2. Session cookie will be sent with response
3. Include session cookie in subsequent requests

### Implementation Notes
- All endpoints check for active `HttpSession`
- User object must be present in session
- Unauthenticated requests return `401 Unauthorized`

---

## Response Format

### Success Response
```json
{
  "appointmentNumber": "APT-2026-001",
  "patientName": "John Perera",
  "dentistName": "Dr. Silva",
  "treatmentName": "Cleaning",
  "appointmentDate": "2026-09-01",
  "appointmentTime": "10:00",
  "status": "SCHEDULED"
}
```

### Error Response
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Appointment was not found",
  "timestamp": 1693526400000
}
```

---

## HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST (resource created) |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid input or missing required fields |
| 401 | Unauthorized | No authentication or invalid session |
| 403 | Forbidden | Insufficient permissions (e.g., non-admin trying to access reports) |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate appointment or dentist not available |
| 500 | Internal Server Error | Unexpected server error |

---

## Appointment Management API

### Get All Appointments

**Request:**
```
GET /api/appointments
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Query Parameters:** None

**Response:**
```json
[
  {
    "appointmentNumber": "APT-2026-001",
    "patientId": 1,
    "patientName": "John Perera",
    "patientContact": "0771234567",
    "address": "Colombo",
    "dentistName": "Dr. Silva",
    "treatmentId": 2,
    "treatmentName": "Cleaning",
    "treatmentCost": 100.0,
    "appointmentDate": "2026-09-01",
    "appointmentTime": "10:00:00",
    "status": "SCHEDULED"
  }
]
```

**Status Codes:** 200, 401

---

### Get Appointment by Number

**Request:**
```
GET /api/appointments/{appointmentNumber}
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Path Parameters:**
- `appointmentNumber` (string, required): Appointment number (e.g., APT-2026-001)

**Response:**
```json
{
  "appointmentNumber": "APT-2026-001",
  "patientId": 1,
  "patientName": "John Perera",
  "dentistName": "Dr. Silva",
  "treatmentName": "Cleaning",
  "treatmentCost": 100.0,
  "appointmentDate": "2026-09-01",
  "appointmentTime": "10:00:00",
  "status": "SCHEDULED"
}
```

**Status Codes:** 200, 400, 401, 404

**Example Error (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Appointment was not found",
  "timestamp": 1693526400000
}
```

---

### Create Appointment

**Request:**
```
POST /api/appointments
Content-Type: application/json

{
  "appointmentNumber": "APT-2026-001",
  "patientId": 1,
  "dentistName": "Dr. Silva",
  "treatmentId": 2,
  "appointmentDate": "2026-09-01",
  "appointmentTime": "10:00:00",
  "status": "SCHEDULED"
}
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Request Body:**
- `appointmentNumber` (string, required): Must match format `APT-YYYY-NNNN`
- `patientId` (integer, required): Valid patient ID
- `dentistName` (string, required): Name of dentist (case-insensitive)
- `treatmentId` (integer, required): Valid treatment ID
- `appointmentDate` (string, required): Date in format `YYYY-MM-DD`
- `appointmentTime` (string, optional): Time in format `HH:MM:SS`. Defaults to `10:00:00`
- `status` (string, optional): One of `SCHEDULED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`. Defaults to `SCHEDULED`

**Response (201 Created):**
```json
{
  "appointmentNumber": "APT-2026-001",
  "patientId": 1,
  "patientName": "John Perera",
  "dentistName": "Dr. Silva",
  "treatmentId": 2,
  "treatmentName": "Cleaning",
  "treatmentCost": 100.0,
  "appointmentDate": "2026-09-01",
  "appointmentTime": "10:00:00",
  "status": "SCHEDULED"
}
```

**Validation Rules:**
- Appointment number must be unique
- Appointment number must match format `APT-YYYY-NNNN`
- Patient ID must be valid
- Treatment ID must be valid
- Appointment date must be in the future
- Dentist must not have conflicting appointments at the same date/time
- Only valid statuses accepted

**Status Codes:** 201, 400, 401, 409

**Example Error (409 - Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Appointment already exists or dentist is not available",
  "timestamp": 1693526400000
}
```

---

### Update Appointment

**Request:**
```
PUT /api/appointments/{appointmentNumber}
Content-Type: application/json

{
  "patientId": 2,
  "dentistName": "Dr. Fernando",
  "treatmentId": 3,
  "appointmentDate": "2026-09-02",
  "appointmentTime": "14:00:00",
  "status": "CONFIRMED"
}
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Path Parameters:**
- `appointmentNumber` (string, required): Appointment number to update

**Request Body:** Same as Create, but all fields optional

**Response (200 OK):**
```json
{
  "appointmentNumber": "APT-2026-001",
  "patientId": 2,
  "patientName": "Jane Silva",
  "dentistName": "Dr. Fernando",
  "treatmentId": 3,
  "treatmentName": "Whitening",
  "treatmentCost": 150.0,
  "appointmentDate": "2026-09-02",
  "appointmentTime": "14:00:00",
  "status": "CONFIRMED"
}
```

**Status Codes:** 200, 400, 401, 404, 409

---

### Delete Appointment

**Request:**
```
DELETE /api/appointments/{appointmentNumber}
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Path Parameters:**
- `appointmentNumber` (string, required): Appointment number to delete

**Response (204 No Content):** Empty body

**Deletion Behavior:** Sets appointment status to `CANCELLED` (soft delete)

**Status Codes:** 204, 400, 401, 404, 500

---

## Billing API

### Get Bill by Appointment Number

**Request:**
```
GET /api/bills/appointment/{appointmentNumber}
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Path Parameters:**
- `appointmentNumber` (string, required): Appointment number

**Response (200 OK):**
```json
{
  "billNo": 5,
  "appointmentNo": "APT-2026-001",
  "consultationFee": 50.0,
  "treatmentCost": 100.0,
  "totalBill": 150.0,
  "billingDate": "2026-09-01 10:30:00"
}
```

**Behavior:**
- If bill doesn't exist for appointment, returns 404
- Bill is auto-generated when appointment is confirmed (via web UI)
- Duplicate bills are prevented

**Status Codes:** 200, 400, 401, 404

---

## Dentist API

### Get All Dentists

**Request:**
```
GET /api/dentists
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Response (200 OK):**
```json
[
  {
    "dentistId": 1,
    "dentistName": "Dr. Perera",
    "specialization": "General Dentistry"
  },
  {
    "dentistId": 2,
    "dentistName": "Dr. Silva",
    "specialization": "Orthodontics"
  },
  {
    "dentistId": 3,
    "dentistName": "Dr. Fernando",
    "specialization": "Cosmetic Dentistry"
  }
]
```

**Status Codes:** 200, 401

---

### Get Dentist by ID

**Request:**
```
GET /api/dentists/{dentistId}
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Path Parameters:**
- `dentistId` (integer, required): Dentist ID

**Response (200 OK):**
```json
{
  "dentistId": 2,
  "dentistName": "Dr. Silva",
  "specialization": "Orthodontics"
}
```

**Status Codes:** 200, 400, 401, 404

---

## Treatment API

### Get All Treatments

**Request:**
```
GET /api/treatments
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Response (200 OK):**
```json
[
  {
    "treatmentId": 1,
    "treatmentName": "Cleaning",
    "cost": 100.0
  },
  {
    "treatmentId": 2,
    "treatmentName": "Whitening",
    "cost": 150.0
  },
  {
    "treatmentId": 3,
    "treatmentName": "Filling",
    "cost": 200.0
  },
  {
    "treatmentId": 4,
    "treatmentName": "Root Canal",
    "cost": 500.0
  }
]
```

**Status Codes:** 200, 401

---

### Get Treatment by ID

**Request:**
```
GET /api/treatments/{treatmentId}
```

**Authentication:** Required (ADMIN, RECEPTIONIST)

**Path Parameters:**
- `treatmentId` (integer, required): Treatment ID

**Response (200 OK):**
```json
{
  "treatmentId": 2,
  "treatmentName": "Whitening",
  "cost": 150.0
}
```

**Status Codes:** 200, 400, 401, 404

---

## Reports API

### Get Daily Appointments Report

**Request:**
```
GET /api/reports/appointments?date=2026-09-01
```

**Authentication:** Required (ADMIN only)

**Authorization:** Only ADMIN users can access reports

**Query Parameters:**
- `date` (string, optional): Report date in format `YYYY-MM-DD`. Defaults to today

**Response (200 OK):**
```json
{
  "reportDate": "2026-09-01",
  "appointmentCount": 3,
  "appointments": [
    {
      "appointmentNo": "APT-2026-001",
      "patientName": "John Perera",
      "dentistName": "Dr. Silva",
      "treatmentName": "Cleaning",
      "appointmentDateTime": "2026-09-01 10:00:00",
      "status": "SCHEDULED"
    }
  ]
}
```

**Status Codes:** 200, 400, 401, 403

**Example Error (403 - Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Only administrators can access reports",
  "timestamp": 1693526400000
}
```

---

### Get Dentist Workload Report

**Request:**
```
GET /api/reports/dentist-workload?from=2026-08-26&to=2026-09-01
```

**Authentication:** Required (ADMIN only)

**Query Parameters:**
- `from` (string, optional): Start date in format `YYYY-MM-DD`. Defaults to 6 days ago
- `to` (string, optional): End date in format `YYYY-MM-DD`. Defaults to today

**Response (200 OK):**
```json
{
  "startDate": "2026-08-26",
  "endDate": "2026-09-01",
  "dentistCount": 3,
  "dentists": [
    {
      "dentistName": "Dr. Silva",
      "totalAppointments": 5,
      "completedAppointments": 3,
      "scheduledAppointments": 2,
      "cancelledAppointments": 0,
      "noShowAppointments": 0
    }
  ]
}
```

**Status Codes:** 200, 400, 401, 403

---

### Get Treatment Statistics Report

**Request:**
```
GET /api/reports/treatments?from=2026-08-26&to=2026-09-01
```

**Authentication:** Required (ADMIN only)

**Query Parameters:**
- `from` (string, optional): Start date in format `YYYY-MM-DD`. Defaults to 6 days ago
- `to` (string, optional): End date in format `YYYY-MM-DD`. Defaults to today

**Response (200 OK):**
```json
{
  "startDate": "2026-08-26",
  "endDate": "2026-09-01",
  "treatmentCount": 4,
  "treatments": [
    {
      "treatmentName": "Cleaning",
      "totalAppointments": 5,
      "completedAppointments": 3,
      "revenue": 500.0
    }
  ]
}
```

**Status Codes:** 200, 400, 401, 403

---

### Get Revenue Report

**Request:**
```
GET /api/reports/revenue?from=2026-08-26&to=2026-09-01
```

**Authentication:** Required (ADMIN only)

**Query Parameters:**
- `from` (string, optional): Start date in format `YYYY-MM-DD`. Defaults to 6 days ago
- `to` (string, optional): End date in format `YYYY-MM-DD`. Defaults to today

**Response (200 OK):**
```json
{
  "startDate": "2026-08-26",
  "endDate": "2026-09-01",
  "totalRevenue": 2150.0,
  "dateWiseRevenue": [
    {
      "reportDate": "2026-09-01",
      "billCount": 3,
      "treatmentRevenue": 450.0,
      "consultationRevenue": 150.0,
      "totalRevenue": 600.0
    }
  ]
}
```

**Status Codes:** 200, 400, 401, 403

---

## Authorization Rules

### ADMIN User
- ✅ Get all appointments
- ✅ Get specific appointment
- ✅ Create appointment
- ✅ Update appointment
- ✅ Delete appointment
- ✅ Get bills
- ✅ Get dentists
- ✅ Get treatments
- ✅ Access reports (all endpoints)

### RECEPTIONIST User
- ✅ Get all appointments
- ✅ Get specific appointment
- ✅ Create appointment
- ✅ Update appointment
- ✅ Delete appointment
- ✅ Get bills
- ✅ Get dentists
- ✅ Get treatments
- ❌ Access reports (returns 403 Forbidden)

---

## Error Handling

All errors follow the standard error response format:

```json
{
  "status": <HTTP_STATUS_CODE>,
  "error": "<ERROR_TYPE>",
  "message": "<DETAILED_MESSAGE>",
  "timestamp": <UNIX_TIMESTAMP_MS>
}
```

**Common Error Messages:**

| Scenario | Status | Message |
|----------|--------|---------|
| Missing authentication | 401 | "Authentication required" |
| Non-admin accessing admin resource | 403 | "Only administrators can access reports" |
| Invalid appointment number | 400 | "Appointment number format is invalid" |
| Dentist unavailable | 409 | "Appointment already exists or dentist is not available" |
| Invalid date format | 400 | "Invalid date format. Use YYYY-MM-DD" |
| Patient not found | 400 | "Patient must be selected" |
| Treatment not found | 400 | "Treatment not found" |

---

## Postman Collection

### Headers (All Requests)
```
Content-Type: application/json
Accept: application/json
```

### Authentication
Ensure you have an active session by logging in via the web UI or obtaining a session cookie first.

### Example Postman Environment Variables
```
base_url = http://localhost:8081/SunriseDentalClinic
api_url = {{base_url}}/api
```

### Sample Requests

**1. Get All Appointments**
```
GET {{api_url}}/appointments
```

**2. Create Appointment**
```
POST {{api_url}}/appointments
Body (JSON):
{
  "appointmentNumber": "APT-2026-001",
  "patientId": 1,
  "dentistName": "Dr. Silva",
  "treatmentId": 2,
  "appointmentDate": "2026-09-15",
  "appointmentTime": "14:00:00",
  "status": "SCHEDULED"
}
```

**3. Get Dentist Workload Report**
```
GET {{api_url}}/reports/dentist-workload?from=2026-08-25&to=2026-09-01
```

**4. Get Bill**
```
GET {{api_url}}/bills/appointment/APT-2026-001
```

---

## Changelog

### Version 1.0 (2026-08-31)
- Initial REST API implementation
- Appointment CRUD operations
- Bill retrieval
- Dentist and treatment listing
- Admin-only reports
- Session-based authentication
- Proper HTTP status codes and error responses

---

## Support & Notes

- All timestamps are in UTC
- Date format: `YYYY-MM-DD`
- DateTime format: `YYYY-MM-DD HH:MM:SS`
- Currency is in decimal format (e.g., 150.0 for Rs. 150.00)
- Appointment deletion is a soft delete (status set to CANCELLED)
- Bills are immutable after creation
- No DELETE operation for bills (by design)

