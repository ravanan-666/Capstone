# Contributing to DJ Mart

Thank you for contributing to **DJ Mart**, a high-performance multi-seller e-commerce web platform engineered for Anna University R2025 Semester 3 Capstone Evaluation.

---

## 1. Code of Conduct & Architecture Principles

To maintain architectural integrity and enterprise quality, all contributions must uphold these core principles:
1. **Framework-Free Core**: Zero heavy opaque frameworks (No Spring Boot, Hibernate, or JPA). Stick to Java Servlets 4.0, JSP/JSTL, and raw JDBC.
2. **Strict Layered Separation**:
   `Browser / UI -> Filter -> Servlet / Controller -> Service -> DAO -> HikariCP / DB`.
   - Never place SQL queries inside Servlets or Services.
   - Never place presentation logic or HTML rendering inside Services or DAOs.
3. **Security by Design**:
   - Every database query must use parameterized `PreparedStatement`.
   - All user inputs rendered in JSP must use `<c:out>` or XML escaping.
   - All state-changing requests (`POST`, `PUT`, `DELETE`) require valid anti-CSRF tokens.
   - Never log passwords, API keys, or personal identifiable information.

---

## 2. Development Setup

### Prerequisites
- **JDK 17** (Temurin or Oracle) configured in your path (`java -version`).
- **Apache Maven 3.8+** (`mvn -version`).
- **Git** (`git --version`).
- **Apache Tomcat 9.0.x** (optional, for local WAR deployment).

### Setup Steps
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/djnir/DJ Mart.git
   cd DJ Mart
   ```

2. **Configure Environment Variables**:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

3. **Verify Build and Tests**:
   Run the test suite to ensure your local environment is configured properly:
   ```bash
   mvn clean test
   ```

4. **Package WAR Archive**:
   ```bash
   mvn clean package
   ```
   Artifact will be placed at `target/DJ Mart.war`.

---

## 3. Branching & Commit Guidelines

### Git Branching Model
- `main`: Production-ready branch. All commits must pass CI.
- `feature/<feature-name>`: Topic branches for new capabilities.
- `fix/<bug-name>`: Topic branches for defect fixes.

### Conventional Commit Messages
Commit messages must follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:
- `feat: <description>` — Introduces a new feature.
- `fix: <description>` — Solves a bug or defect.
- `test: <description>` — Adds or updates automated tests.
- `docs: <description>` — Documentation or diagram additions.
- `refactor: <description>` — Code restructuring without behavior changes.
- `chore: <description>` — Build scripts, dependencies, or configuration updates.

Example:
```bash
git commit -m "feat: add seller order status update endpoint and validation"
```

---

## 4. Testing Standards

- **Unit Tests**: Place in `src/test/java/com/DJ Mart/service/` or `controller/`. Use JUnit 5 and Mockito.
- **Integration Tests**: Place in `src/test/java/com/DJ Mart/dao/` or root test package. Run against an isolated in-memory H2 database (`jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=REGULAR`).
- **Target Coverage**: All new business logic must include test cases covering happy path, validation failures, and edge cases.
- **Continuous Integration**: The GitHub Actions workflow (`.github/workflows/build.yml`) automatically runs `mvn -B clean verify` on all pushes and PRs.

---

## 5. Definition of Done (DoD) Checklist

Before submitting a Pull Request or completing a task, verify the following:
- [ ] Code compiles without warnings (`mvn clean compile`).
- [ ] All 125+ automated tests pass with zero errors (`mvn clean test`).
- [ ] Full build and WAR assembly succeeds (`mvn clean verify`).
- [ ] Database queries exclusively use `PreparedStatement` with try-with-resources.
- [ ] All currency values are modeled as `BigDecimal` / `DECIMAL(10,2)`.
- [ ] No hardcoded passwords, tokens, or API keys in source files.
- [ ] Any new JSP output uses `<c:out>` to prevent XSS.
- [ ] Commit message conforms to conventional commit formatting.
