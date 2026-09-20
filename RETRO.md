# Sprint Retrospectives

## Sprint 0: Project Inception & Database Layer Foundation
- **Window**: Kickoff – Foundation Setup
- **What Worked**:
  - Clear separation of database concerns: schema definitions in versioned migrations and separate seed script.
  - Integration tests against in-memory H2 verified foreign key integrity, index creation, check constraints, and HikariCP connection pool cleanup.
  - Zero-dependency runtime architecture strictly relying on Java EE Servlet 4.0 and raw JDBC with HikariCP.
- **What Didn't**:
  - Command-line execution in PowerShell required careful argument quoting for Java properties.
- **One Change for Next Sprint**:
  - Implement thin BaseServlet and standard JSON envelope classes before feature controllers to standardize HTTP status codes and error responses across all endpoints.
