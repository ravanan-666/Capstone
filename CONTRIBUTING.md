# Contributing to DjMart

## Development Workflow & Guidelines

### 1. Branching Model
- `main`: Production-ready, always deployable branch.
- `feature/<name>`: Topic branch for specific features or bug fixes.
- Pull requests must pass automated CI (`mvn -B clean verify`) before merging.

### 2. Conventional Commit Messages
Commit messages strictly adhere to conventional formats:
- `feat: <description>` — A new feature
- `fix: <description>` — A bug fix
- `test: <description>` — Adding or updating automated tests
- `docs: <description>` — Documentation updates
- `refactor: <description>` — Code change that neither fixes a bug nor adds a feature

### 3. Step-by-Step Local Setup
1. **Clone repository**:
   ```bash
   git clone <repo-url>
   cd djmart
   ```
2. **Configure Environment**:
   Copy `.env.example` to `.env` or set `config.properties`.
3. **Compile & Run Unit/Integration Tests**:
   ```bash
   mvn clean test
   ```
4. **Package Application**:
   ```bash
   mvn clean package
   ```
5. **Deploy to Apache Tomcat 9.0.x**:
   Copy `target/djmart.war` to Tomcat's `webapps/` folder.
   Access at `http://localhost:8080/djmart`.

### 4. Definition of Done (DoD) Checklist
- [ ] Code strictly follows layered MVC architecture (No SQL in Servlets or Services).
- [ ] All database queries use `PreparedStatement` with try-with-resources.
- [ ] Zero plain-text passwords or secret credentials committed.
- [ ] Monetary figures handled as `BigDecimal` / `DECIMAL(10,2)`.
- [ ] Automated tests written and passing in CI pipeline.
