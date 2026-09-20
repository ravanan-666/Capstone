# DjMart — Multi-Seller E-Commerce Marketplace

[![build-and-test](https://github.com/djnir/djmart/actions/workflows/build.yml/badge.svg)](https://github.com/djnir/djmart/actions/workflows/build.yml)

> **Java Servlets · JDBC · Apache Tomcat 9.0.x · H2 Database**  
> Anna University R2025 Semester 3 Capstone Project  
> Checkpoint Window: Jul 27 – Oct 10, 2026 | Developer: solo

---

## 1. Problem Statement
**DjMart** is a production-grade multi-seller e-commerce web platform engineered with vanilla Java Servlets, JSP/JSTL, and pure JDBC on Apache Tomcat. The system eliminates heavy opaque frameworks (Spring Boot, Hibernate, JPA) in favor of explicit architectural control, high-performance database connection pooling via HikariCP, role-based access control (RBAC), and strict transactional integrity for multi-seller commerce.

---

## 2. Technology Stack

| Layer / Concern | Technology / Library | Version / Details |
|---|---|---|
| **Language** | Java SE (LTS) | Java 17 LTS (`release: 17`) |
| **Servlet Container** | Apache Tomcat | 9.0.x (`javax.servlet.*` Servlet 4.0 API) |
| **Build Tool** | Apache Maven | 3.9.x |
| **Database** | H2 Database Engine | 2.2.224 (Server mode for prod, in-memory for testing) |
| **Connection Pooling**| HikariCP | 5.1.0 (Managed via `AppContextListener`) |
| **Persistence** | Raw JDBC | `PreparedStatement` only, try-with-resources |
| **View Layer** | JSP + JSTL + Vanilla JS | Fetch API for AJAX endpoints |
| **Serialization** | Google Gson | 2.10.1 |
| **Password Hashing** | jBCrypt | 0.4 (Blowfish salt rounds = 10) |
| **Logging** | SLF4J + Logback | 2.0.12 / 1.4.14 (MDC request tracing) |
| **Automated Testing**| JUnit 5 + Mockito | 5.10.2 / 5.11.0 |
| **CI / CD** | GitHub Actions | Temurin JDK 17, `mvn -B clean verify` |

---

## 3. System Architecture & Layering

The application strictly implements **Layered MVC / Front Controller Architecture**:

```
Browser (JSP / Vanilla JS + Fetch)
   │
   ▼
[EncodingFilter] ──► [RequestLoggingFilter (MDC requestId)] ──► [AuthFilter (RBAC Guard)]
   │
   ▼
[Controller / Servlet Layer] (Thin HTTP orchestration, DTO translation, JSON envelope)
   │
   ▼
[Service Layer] (Business logic, input validation, transaction boundaries)
   │
   ▼
[DAO Layer] (PreparedStatements ONLY, clean mapping to Domain Models)
   │
   ▼
[HikariCP DataSource] (Managed singleton lifecycle in ServletContextListener)
   │
   ▼
[H2 Database Engine]
```

### Mandatory Engineering Rules Enforced:
1. **PreparedStatement Everywhere**: Zero string concatenation in SQL queries.
2. **jBCrypt Password Hashing**: Passwords stored with cryptographic salt; never logged or exposed.
3. **Session Hardening**: Managed via `HttpSession`, session ID rotated on login, 30-min timeout.
4. **Output Escaping**: All user-supplied output escaped (`<c:out>` or `fn:escapeXml`).
5. **HikariCP Lifecycle**: Single `AppContextListener` controls pool lifecycle.
6. **Currency Integrity**: All monetary values represented as `DECIMAL(10,2)` (`BigDecimal` in Java).

---

## 4. Database Design & Relational Model

The database comprises six core normalized tables:
- **`users`**: Platform actors (`BUYER`, `SELLER`, `ADMIN`), unique email, BCrypt password hashes.
- **`products`**: Seller inventory listings with stock constraints, categories, and image URLs.
- **`orders`**: Buyer purchase records with status workflow (`PENDING` $\to$ `CONFIRMED` $\to$ `SHIPPED` $\to$ `DELIVERED` $\to$ `CANCELLED`).
- **`order_items`**: Line items per order capturing historical product prices at time of checkout.
- **`cart_items`**: Persistent buyer shopping carts with composite unique constraint `(user_id, product_id)`.
- **`reviews`**: Verified buyer product reviews with ratings constrained between 1 and 5 stars.

### Architecture Diagrams:
- **D1 Entity Relationship Diagram**: Located at [`docs/diagrams/D1_ER_Diagram.puml`](docs/diagrams/D1_ER_Diagram.puml)
- **D2 Use Case Diagram**: Located at [`docs/diagrams/D2_UseCase_Diagram.puml`](docs/diagrams/D2_UseCase_Diagram.puml)
- **D3 Place Order Sequence Diagram**: Located at [`docs/diagrams/D3_Sequence_Diagram.puml`](docs/diagrams/D3_Sequence_Diagram.puml)

---

## 5. Getting Started (Local Development)

### Prerequisites:
- Java JDK 17 (or newer with Java 17 compatibility)
- Apache Maven 3.8+
- Apache Tomcat 9.0.x (for war deployment)

### 1. Clone & Build
```bash
git clone https://github.com/djnir/djmart.git
cd djmart
mvn clean test
```

### 2. Run Test Suite
```bash
mvn test
```
All integration tests run against an isolated in-memory H2 database (`jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=REGULAR`).

### 3. Package WAR File
```bash
mvn clean package
```
Generates `target/djmart.war` ready to drop into Tomcat's `webapps/` directory.

---

## 6. Seed Accounts (Development & Demo)

All seed user accounts are initialized with the default password: **`Password@123`**

| Role | Name | Email |
|---|---|---|
| **ADMIN** | System Administrator | `admin@djmart.com` |
| **SELLER** | Tech Trends Official | `seller.tech@djmart.com` |
| **SELLER** | Urban Style Studio | `seller.style@djmart.com` |
| **BUYER** | John Doe | `buyer.john@djmart.com` |
| **BUYER** | Sarah Connor | `buyer.sarah@djmart.com` |
