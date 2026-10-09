# Sprint Retrospectives — DJ Mart Development

This document logs retrospectives across all sprints of the **DJ Mart** Anna University R2025 Semester 3 Capstone project.

---

## Sprint 0: Inception & Database Layer Foundation
- **Window**: Kickoff – Foundation Setup
- **Objectives**: Initialize Maven project structure, configure H2 database engine, set up HikariCP connection pooling, define normalized schema with migrations and seed data.
- **What Worked**:
  - Clean schema separation between `schema.sql`, Flyway-style migrations, and demo `seed.sql`.
  - In-memory H2 test configuration verifying foreign keys, check constraints, and indexes before writing application code.
  - Automated CI workflow configured with GitHub Actions.
- **What Didn't**:
  - Windows PowerShell command line argument escaping for Java system properties required specific quoting.
- **Key Learnings & Changes**:
  - Centralize database connection acquisition in `DatabaseUtil` with graceful pool shutdown via `ServletContextListener`.

---

## Sprint 1: Persistence & Data Access Object (DAO) Layer
- **Window**: Foundation – DAO Implementation
- **Objectives**: Implement raw JDBC DAOs for `User`, `Product`, `Order`, `OrderItem`, `CartItem`, and `Review` without ORM overhead.
- **What Worked**:
  - 100% adherence to parameterized `PreparedStatement` with try-with-resources.
  - Query filtering, pagination, and sorting cleanly encapsulated in `ProductDAOImpl`.
- **What Didn't**:
  - Mapping complex result sets with joined columns required careful column alias indexing.
- **Key Learnings & Changes**:
  - Extracted shared row mapper helper methods to keep DAO methods clean and reusable.

---

## Sprint 2: Business Logic & Transactional Service Layer
- **Window**: DAO – Business Logic
- **Objectives**: Implement service interfaces (`AuthService`, `ProductService`, `CartService`, `OrderService`, `ReviewService`) and transactional checkout.
- **What Worked**:
  - 10-step atomic checkout transaction using raw JDBC connection control (`setAutoCommit(false)` / `commit()` / `rollback()`).
  - Unit tests with Mockito validating business rules without hitting the database.
- **What Didn't**:
  - Handling stock race conditions required checking current inventory against cart quantities within the same transactional connection.
- **Key Learnings & Changes**:
  - Passed the active JDBC `Connection` into transactional DAO operations so the entire order placement commits or rolls back together.

---

## Sprint 3: Authentication, Authorization & Security Hardening
- **Window**: Service – Security Implementation
- **Objectives**: Implement BCrypt password hashing, session fixation defense, role-based access control (`AuthFilter`), and CSRF protection (`CsrfFilter`).
- **What Worked**:
  - `AuthFilter` cleanly isolates routes for `BUYER`, `SELLER`, and `ADMIN`.
  - Cryptographic session rotation (`request.changeSessionId()`) eliminates session fixation vulnerabilities.
- **What Didn't**:
  - Certain AJAX endpoints needed exemptions or header-based CSRF token support (`X-CSRF-Token`) alongside standard form parameters.
- **Key Learnings & Changes**:
  - Enhanced `CsrfFilter` to inspect both `_csrf` / `csrfToken` request parameters and `X-CSRF-Token` HTTP headers.

---

## Sprint 4: Controller & REST API Layer
- **Window**: Security – Servlets & APIs
- **Objectives**: Implement thin Servlets (`AuthServlet`, `ProductServlet`, `CartServlet`, `OrderServlet`, `ReviewServlet`, `SellerServlet`, `AdminServlet`) and standardize JSON envelopes (`ApiResponse<T>`).
- **What Worked**:
  - `BaseServlet` centralized JSON serialization and exception-to-HTTP-status mapping.
  - RESTful URLs cleanly coexist with traditional JSP forward routes.
- **What Didn't**:
  - Parsing JSON bodies from `request.getReader()` required UTF-8 character encoding enforcement before reading.
- **Key Learnings & Changes**:
  - Placed `EncodingFilter` as the top-level filter in `web.xml` to ensure all request bodies and query parameters are decoded as UTF-8.

---

## Sprint 5: Frontend UI, AI Concierge & Final Capstone Release
- **Window**: Servlets – Final Capstone Delivery
- **Objectives**: Build the bespoke "Classic Luxury Commerce" UI, embed the AI Concierge customer support widget, add health check endpoint, execute end-to-end integration tests, and finalize project documentation.
- **What Worked**:
  - Distinctive luxury editorial theme achieved without heavy CSS frameworks (pure CSS3 and semantic HTML5).
  - AI Concierge implemented with offline `MockChatProvider` and Google Gemini LLM provider fallback.
  - End-to-end integration test suite passed 100% (125 tests, 0 failures, 0 errors).
  - Standalone WAR packaged cleanly for Apache Tomcat 9.0.x deployment.
- **What Didn't**:
  - Minor JSP scriptlet / JSTL bean property name discrepancies resolved with explicit getter aliases in DTOs.
- **Key Learnings & Changes**:
  - Layered MVC architecture without heavy frameworks yields sub-second test execution and instantaneous Tomcat startup times.
