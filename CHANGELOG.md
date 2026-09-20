# Changelog

All notable changes to the **DjMart** project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0-alpha] - 2026-09-20
### Added
- Maven project skeleton configured for Java 17 LTS and Tomcat 9 (`javax.servlet` 4.0.1).
- Complete H2 Database layer with HikariCP connection pooling (`DjMartHikariPool`).
- Database schema scripts: `db/schema.sql`, `db/migrations/V1__init_schema.sql`, and `db/migrations/V2__add_performance_indexes.sql`.
- Comprehensive seed data in `db/seed.sql` with BCrypt hashed credentials (`Password@123`).
- `AppContextListener` managing connection pool lifecycle and schema auto-migration.
- Domain model POJOs: `User`, `Product`, `Order`, `OrderItem`, `CartItem`, `Review`, `Role`, and `OrderStatus`.
- Utility classes: `PasswordUtil` (jBCrypt), `DatabaseUtil`, and `DatabaseConfig`.
- Automated test suite verifying schema creation, seed data, constraints, indexes, and connection acquisition.
- CI/CD workflow `.github/workflows/build.yml` for GitHub Actions.
- Architectural diagrams (PlantUML D1 ER, D2 Use Case, D3 Sequence).
