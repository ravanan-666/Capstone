# Changelog

All notable changes to the **DJ Mart** multi-seller marketplace project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-09-20 (Capstone Final Release)

### Added
- **AI Customer Support Concierge**:
  - Pluggable `ChatProvider` interface with `MockChatProvider` (offline intelligent answers for catalog, shipping, returns, order inquiries) and `GeminiChatProvider` (Google Gemini REST API with graceful degradation).
  - `ChatService` / `ChatServiceImpl` enforcing per-session rate-limiting (10 requests/min), length cap (500 chars), and in-memory caching.
  - `ChatServlet` mapped to `/api/chat` and `/api/v1/chat`.
  - Floating concierge chat drawer in `footer.jsp` with luxury styling and dynamic conversation append.
- **System Health Check**:
  - `HealthServlet` mapped to `/api/v1/health` verifying database pool connectivity and system status.
- **Comprehensive Documentation & Diagrams**:
  - Updated `README.md` with complete architecture, API tables, deployment guides, and inline Mermaid diagrams.
  - Architecture Diagrams: D1 (ER Diagram), D2 (Use Case Diagram), D3 (Place Order Sequence Diagram), and Layered Architecture Diagram in Mermaid and PlantUML formats.
  - Requirement compliance matrix cross-referencing all Anna University R2025 Capstone criteria.
  - Production-ready `CONTRIBUTING.md`, `RETRO.md`, and `.env.example`.
- **End-to-End Integration Tests**:
  - `EndToEndFlowIntegrationTest` validating Buyer, Seller, and Admin workflows on in-memory H2 database. Total test suite expanded to 125 tests with 100% pass rate.

---

## [1.0.0] - 2026-09-20 (Main Business Flows & Classic Luxury UI)

### Added
- **Classic Luxury Frontend User Interface**:
  - Editorial luxury aesthetic using pure CSS3 custom variables, Google Fonts (`Playfair Display` & `Plus Jakarta Sans`), and zero external framework bloat.
  - Reusable layout partials (`header.jsp`, `footer.jsp`), dynamic navigation, and responsive mobile drawer.
  - Toast notification system (`toast.js`) and API client utility (`api-client.js`).
- **Complete Business Flow Views & Scripts**:
  - **Buyer Flow**: Interactive product catalog (`products.jsp`) with multi-criteria filtering and sorting, rich product details (`product-details.jsp`) with verified buyer reviews (`reviews.js`), shopping cart (`cart.jsp`), and multi-step checkout (`checkout.jsp` & `checkout.js`).
  - **Buyer Order Management**: Order history page (`orders.jsp`) and comprehensive order details (`order-details.jsp`) with real-time cancellation for pending orders.
  - **Seller Dashboard**: Dedicated seller portal (`seller/dashboard.jsp` & `seller.js`) featuring real-time catalog KPIs, low-stock warnings, inline product CRUD modal, and order fulfillment status transitions.
  - **Admin Control Panel**: Platform overview (`admin/dashboard.jsp`) displaying aggregate gross merchandise value (GMV), order counts, user auditing, and catalog monitoring.
- **Automated Servlet Test Suite**:
  - `SellerServletTest`, `AdminServletTest`, `OrderServletTest`, `ReviewServletTest`, and `AuthServletTest` achieving comprehensive branch coverage.

---

## [0.5.0] - 2026-09-20 (Servlet & REST Controller Layer)

### Added
- Standardized REST envelope `ApiResponse<T>` with `success`, `message`, `data`, and `errorCode` properties.
- `BaseServlet` providing standardized JSON serialization via Google Gson, custom `LocalDateTime` type adapters, and centralized exception-to-HTTP-status mapping (400, 401, 403, 404, 409, 500).
- Controllers / Servlets:
  - `AuthServlet` (`/auth/*`, `/api/v1/auth/*`)
  - `ProductServlet` (`/products/*`, `/api/v1/products/*`)
  - `CartServlet` (`/cart/*`, `/api/v1/cart/*`)
  - `OrderServlet` (`/orders/*`, `/checkout/*`, `/api/v1/orders/*`, `/api/v1/checkout/*`)
  - `ReviewServlet` (`/reviews/*`, `/api/v1/reviews/*`)
  - `SellerServlet` (`/seller/*`, `/api/v1/seller/*`)
  - `AdminServlet` (`/admin/*`, `/api/v1/admin/*`)

---

## [0.4.0] - 2026-09-20 (Authentication, Authorization & Security Hardening)

### Added
- Role-based authorization via `AuthFilter` enforcing access rules for `BUYER`, `SELLER`, and `ADMIN` roles.
- `CsrfFilter` providing double-submit and session-stored cryptographic anti-CSRF tokens for all state-changing HTTP methods (`POST`, `PUT`, `DELETE`).
- Session fixation protection: automatic session ID rotation upon login (`changeSessionId` / fallback session regeneration) and complete invalidation upon logout.
- Secure password hashing with `jBCrypt` (10 salt rounds).
- XSS prevention via `<c:out>` and XML entity escaping across all JSP views.
- Parameterized `PreparedStatement` enforcement across all database operations.

---

## [0.3.0] - 2026-09-20 (Business Logic & Transactional Service Layer)

### Added
- `OrderServiceImpl` featuring a 10-step atomic checkout transaction with `Connection.setAutoCommit(false)` and automatic rollback on stock depletion or processing error.
- `ProductServiceImpl` enforcing seller ownership verification, stock sanity checks, and category validation.
- `CartServiceImpl` providing server-calculated totals, stock boundary enforcement, and quantity validation.
- `ReviewServiceImpl` restricting review submissions strictly to verified buyers with delivered orders.
- `AuthServiceImpl` handling user registration, duplicate email prevention, and BCrypt credential verification.
- Comprehensive unit tests mocking DAO interactions via Mockito.

---

## [0.2.0-beta] - 2026-09-20 (Data Access Object Layer)

### Added
- Complete DAO interfaces and JDBC implementations:
  - `UserDAO` / `UserDAOImpl`
  - `ProductDAO` / `ProductDAOImpl` (supporting pagination, keyword search, category filtering, and sorting)
  - `OrderDAO` / `OrderDAOImpl`
  - `OrderItemDAO` / `OrderItemDAOImpl`
  - `CartDAO` / `CartDAOImpl`
  - `ReviewDAO` / `ReviewDAOImpl`
- Unit and integration tests for all DAO classes using in-memory H2 database.

---

## [0.1.0-alpha] - 2026-09-20 (Project Inception & Database Foundation)

### Added
- Maven project configured for Java 17 LTS and Apache Tomcat 9 (`javax.servlet` 4.0.1).
- HikariCP connection pooling (`DjMartHikariPool`) configured via `DatabaseUtil` and `AppContextListener`.
- Database schema scripts: `db/schema.sql`, `db/migrations/V1__init_schema.sql`, and `db/migrations/V2__add_performance_indexes.sql`.
- Pre-seeded test accounts and mock marketplace inventory in `db/seed.sql`.
- Domain model POJOs (`User`, `Product`, `Order`, `OrderItem`, `CartItem`, `Review`, `Role`, `OrderStatus`).
- Utility classes: `PasswordUtil` (jBCrypt), `DatabaseUtil`, `ValidationUtil`.
- GitHub Actions CI workflow `.github/workflows/build.yml` running `mvn -B clean verify`.
