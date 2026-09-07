# 🦷 Sunrise Dental Clinic Management System

A modern, role-based **Dental Clinic Management System** built with **Java**, **JSP/Servlets**, **PostgreSQL** and **JDBC**. It replaces the clinic's paper-based workflow with a fast, validated and auditable digital system for booking appointments, managing doctors and staff, generating bills and producing management reports.

![Java](https://img.shields.io/badge/Java-17-orange)
![JSP-Servlets](https://img.shields.io/badge/UI-JSP%20%2B%20Servlets-blue)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-336791)
![REST](https://img.shields.io/badge/API-REST%20%2F%20JSON-green)
![Tests](https://img.shields.io/badge/tests-27%20passing-brightgreen)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-success)

---

## ✨ Features

- 🔐 **Secure login** with BCrypt password hashing, role-based access (ADMIN / RECEPTION), sessions and **Remember-Me** auto-login
- 📅 **Appointment management** — register, search, edit and cancel appointments with automatic booking numbers
- 👨‍⚕️ **Doctor selection from a registered doctor list** and **multi-treatment selection** using a searchable tick-panel (34 treatment price list in LKR)
- 🧾 **Billing** — automatic invoice generation (registration fee + each treatment), print-ready receipts, one bill per appointment
- 🗂 **User management** — administrators can create/remove staff accounts (with last-admin protection)
- 🩺 **Doctor management** — administrators can register/remove doctors
- 📊 **Reports** — daily appointments, dentist workload, treatment statistics and revenue, with date filters (admin only)
- ✉️ **Email notifications** (Brevo API) — appointment confirmation, update and cancellation emails
- 🛡 **Security & audit** — authentication filter, double-booking prevention and a **database audit trail** via triggers
- 🧪 **Tested** — 27 automated JUnit tests + GitHub Actions CI

---

## 🏗 Architecture

The application follows a clean **3-tier architecture** and uses well-known design patterns:

| Tier | Technology | Packages |
|------|------------|----------|
| **Presentation** | JSP + Servlets | `webapp/`, `com.sunrise.controller` |
| **Business** | Service Layer | `com.sunrise.service` |
| **Data** | JDBC + DAO | `com.sunrise.dao` |
| **Web Services** | JAX-RS / RESTEasy | `com.sunrise.rest` |

Patterns: **MVC**, **DAO**, **Service Layer**, **DTO**, **Intercepting Filter**, **Exception Mapper**.

### Advanced Database
- Stored function `calculate_bill_total()` used during bill creation
- Trigger + `appointment_audit` table to log every appointment change
- Partial unique index to prevent **double booking** at the database level
- Transactional inserts for appointments with multiple treatments

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Apache Maven
- PostgreSQL 13+
- Apache Tomcat 10.1 (or Eclipse with WTP)

### 1. Create the database
```sql
CREATE DATABASE sunrise_dentall;
```
Then run [`src/main/resources/schema.sql`](src/main/resources/schema.sql) — it creates the tables, indexes, stored function, trigger and seeds the treatment price list. The app also auto-runs a safe schema upgrade on first start.

### 2. Configure environment (recommended)
```bash
DB_URL=jdbc:postgresql://localhost:5432/sunrise_dentall
DB_USER=postgres
DB_PASSWORD=yourpassword
EMAIL_API_KEY=your-brevo-key     # optional
EMAIL_FROM=your-verified-sender  # optional
```
> 🔒 Secrets are read from environment variables or the **git-ignored** `src/main/resources/email.properties`. Never commit API keys or passwords.

### 3. Build and run
```bash
mvn clean package
```
Deploy `target/SunriseDentalClinic.war` to Tomcat and open `http://localhost:8080/SunriseDentalClinic/`.

**First admin:** if no admin exists yet, create one via the web UI or:
```bash
curl -d "name=Admin&email=admin@clinic.com&password=Admin123&employee_number=EMP001&phone_number=0771234567&role=ADMIN" \
  http://localhost:8080/SunriseDentalClinic/UserManagementServlet
```

---

## 🧪 Testing

```bash
mvn test
```

- **27 automated JUnit tests** covering appointment validation, billing totals, email templates, password hashing and remember-me tokens
- Tests are **database-free and network-free**, so they run anywhere and in CI
- Reports are generated automatically in `target/surefire-reports`

## 🤖 CI/CD

[GitHub Actions](.github/workflows/ci.yml) builds the project on every push to `development`/`main`:

1. Sets up Java 17 + Maven cache
2. Runs `mvn clean verify` (all tests)
3. Confirms the WAR artifact is produced
4. Uploads the WAR as a build artifact

---

## 🔌 REST API (summary)

| Method | Endpoint | Purpose | Access |
|--------|----------|---------|--------|
| GET | `/api/appointments` | List appointments | Auth |
| POST | `/api/appointments` | Create appointment | Auth |
| PUT | `/api/appointments/{no}` | Update appointment | Auth |
| DELETE | `/api/appointments/{no}` | Cancel appointment | Auth |
| GET | `/api/bills/appointment/{no}` | Get bill | Auth |
| GET | `/api/dentists` | List doctors | Auth |
| GET | `/api/treatments` | List treatments | Auth |
| GET | `/api/reports/*` | Reports | Admin |

All endpoints use **session-based authentication**. See [README_REST_API.md](README_REST_API.md) and [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for full details.

---

## 📁 Project Structure

```
src/main/java/com/sunrise/
├── controller/   # Servlets (Login, Appointment, Bill, Report, User, Dentist, Dashboard)
├── service/      # Business rules (AppointmentService, BillingService, ...)
├── dao/          # JDBC/DAO layer (PostgreSQL)
├── model/        # Entities & report models
├── dto/          # REST DTOs
├── rest/         # JAX-RS resources (Appointments, Bills, Dentists, Treatments, Reports)
├── filter/       # AuthenticationFilter
└── util/         # PasswordUtil, TokenUtil
src/main/webapp/  # JSP views
src/test/java/    # JUnit 5 tests
```

---

## 📚 Additional Documentation

- [README_REST_API.md](README_REST_API.md) — REST API guide
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) — API reference
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) — feature summary
- [CI_CD.md](CI_CD.md) — CI/CD notes

---

## 📌 Screenshots

*(Add your UI screenshots here — e.g., Login, Dashboard, Register Appointment, Bill, Reports.)*

---

## 👨‍💻 Author

Sunrise Dental Clinic — Advanced Java assignment project.
