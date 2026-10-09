// generator/ch4_to_5.js
// Chapter 4: Folder & File Structure, Chapter 5: File-by-File Detailed Explanation

module.exports = {
  getChapter4: () => `
    <div class="page-break"></div>
    <h1>4. Folder &amp; File Structure</h1>

    <p>
      The DJ Mart project adheres to the standard <strong>Maven Web Application (war)</strong> directory layout. Components are modularized cleanly across layers, allowing developers to immediately locate configuration, business logic, persistence entities, and presentation assets.
    </p>

    <h2>4.1 Directory Tree</h2>
    <pre><code>d:/Dj Mart/
├── pom.xml                               # Maven project configuration &amp; dependencies
├── schema.sql                            # Complete database schema DDL &amp; constraints
├── seed.sql                              # Initial verified seed data (24 Products &amp; Accounts)
├── src/
│   ├── main/
│   │   ├── java/com/djmart/
│   │   │   ├── config/                   # Database &amp; connection configuration (DatabaseConfig)
│   │   │   ├── controller/               # HTTP request servlets (AuthServlet, ProductServlet, etc.)
│   │   │   ├── dao/                      # DAO interfaces &amp; JDBC implementations
│   │   │   ├── dto/                      # Data Transfer Objects (DTOs) for API contracts
│   │   │   ├── exception/                # Domain and HTTP exception hierarchy
│   │   │   ├── filter/                   # Security, CSRF, logging, and RBAC filters
│   │   │   ├── listener/                 # Lifecycle listeners (AppContextListener)
│   │   │   ├── model/                    # Domain models and entities (User, Product, Order)
│   │   │   ├── service/                  # Business logic services &amp; AI engine
│   │   │   └── util/                     # Security, password hashing, validation utilities
│   │   ├── resources/                    # Logback config &amp; migration scripts (V1, V2, V3)
│   │   └── webapp/
│   │       ├── favicon.ico               # Official browser favicon
│   │       ├── index.jsp                 # Editorial homepage &amp; category showcase
│   │       ├── manifest.json             # PWA metadata &amp; mobile icons
│   │       ├── static/
│   │       │   ├── css/style.css         # Luxury editorial stylesheet &amp; design tokens
│   │       │   ├── images/dj_mart_logo.jpg# Official brand logo
│   │       │   └── js/                   # Vanilla JavaScript client modules
│   │       └── WEB-INF/
│   │           ├── web.xml               # Servlet descriptor &amp; filter chain mapping
│   │           └── views/                # Server-rendered JSP templates
│   │               ├── admin/            # Admin control &amp; analytics (dashboard.jsp, analytics.jsp)
│   │               ├── auth/             # Authentication views (login.jsp, register.jsp)
│   │               ├── buyer/            # Buyer views (products.jsp, cart.jsp, checkout.jsp)
│   │               ├── common/           # Shared views (header.jsp, footer.jsp)
│   │               ├── error/            # Error pages (400, 401, 403, 404, 500.jsp)
│   │               └── seller/           # Merchant portal (dashboard.jsp)
</code></pre>

    <h2>4.2 Key Files Breakdown</h2>
    <table>
      <thead>
        <tr>
          <th>File</th>
          <th>Primary Purpose</th>
          <th>Connected Components</th>
          <th>Impact if Missing</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><code>pom.xml</code></td>
          <td>Maven build configuration; manages Jakarta Servlet, H2, PostgreSQL, HikariCP dependencies.</td>
          <td>All compiled Java classes across src/main and src/test.</td>
          <td>The project cannot compile or resolve dependencies.</td>
        </tr>
        <tr>
          <td><code>web.xml</code></td>
          <td>Deployment descriptor defining Servlets, Filter mappings, and custom Error Pages.</td>
          <td>All Servlets, Filters, and JSP views.</td>
          <td>Tomcat cannot route requests to controllers or filters.</td>
        </tr>
        <tr>
          <td><code>AppContextListener.java</code></td>
          <td>Bootstrap listener initializing HikariCP, database migrations, and initial seed data.</td>
          <td>DatabaseUtil, DatabaseConfig, migrations V1-V3.</td>
          <td>The database pool is never initialized; application fails to boot.</td>
        </tr>
        <tr>
          <td><code>DatabaseUtil.java</code></td>
          <td>HikariCP Connection Pool manager and script runner.</td>
          <td>DatabaseConfig and all DAO implementations.</td>
          <td>DAOs cannot acquire SQL connections; database operations fail.</td>
        </tr>
        <tr>
          <td><code>schema.sql</code></td>
          <td>Core database schema defining tables, primary keys, foreign keys, and indexes.</td>
          <td>DatabaseUtil startup migrations and all DAO layers.</td>
          <td>Relational tables do not exist; no queries can run.</td>
        </tr>
        <tr>
          <td><code>CsrfFilter.java</code></td>
          <td>Security filter validating anti-forgery tokens on all state-changing requests.</td>
          <td>SecurityUtil, all JSP forms, api-client.js.</td>
          <td>Cross-site request forgery attacks could manipulate user accounts.</td>
        </tr>
        <tr>
          <td><code>AuthFilter.java</code></td>
          <td>Role-based access control protecting administrative and merchant endpoints.</td>
          <td>User, Role, web.xml.</td>
          <td>Unauthenticated users could access protected administrative routes.</td>
        </tr>
        <tr>
          <td><code>style.css</code></td>
          <td>Comprehensive design system with modern luxury design tokens.</td>
          <td>All JSP templates (imported via header.jsp).</td>
          <td>The website renders without layout styling or brand typography.</td>
        </tr>
      </tbody>
    </table>
  `,

  getChapter5: () => `
    <div class="page-break"></div>
    <h1>5. File-by-File Detailed Explanation</h1>

    <p>
      This chapter provides a detailed, file-by-file walkthrough of the core Java, web descriptor, and client script components in DJ Mart.
    </p>

    <!-- 5.1 AppContextListener.java -->
    <h2>5.1 AppContextListener.java</h2>
    <p><strong>Path:</strong> <code>src/main/java/com/djmart/listener/AppContextListener.java</code></p>
    <p><strong>Description:</strong> A lifecycle <code>ServletContextListener</code> invoked by Tomcat on container startup and graceful shutdown.</p>
    <p><strong>Key Responsibility:</strong> Initializes the HikariCP connection pool, executes version-controlled SQL migrations (V1, V2, V3), verifies the 24-product catalog, and gracefully closes connections on shutdown.</p>
    <p><strong>Lifecycle Triggers:</strong> <code>contextInitialized()</code> on server start; <code>contextDestroyed()</code> on server undeploy or shutdown.</p>
    <p><strong>Connected Files:</strong> <code>DatabaseUtil.java</code>, <code>DatabaseConfig.java</code>, <code>web.xml</code>.</p>

    <div class="no-break">
      <p><strong>Key Code Snippet:</strong></p>
      <pre><code>@Override
public void contextInitialized(ServletContextEvent sce) {
    logger.info("Initializing DJ Mart Application Context...");
    try {
        // 1. Initialize HikariCP Connection Pool and Relational Database
        DatabaseUtil.initDataSource();

        // 2. Execute database migrations and seed catalog verification
        DatabaseUtil.runMigrations();

        logger.info("DJ Mart Database and Application Context Initialized Successfully.");
    } catch (Exception e) {
        logger.error("FATAL: Failed to initialize application context", e);
        throw new RuntimeException("Application startup aborted due to DB failure", e);
    }
}</code></pre>
    </div>
    <p><strong>Code Explanation:</strong> On Tomcat boot, <code>DatabaseUtil.initDataSource()</code> initializes the HikariCP pool. Migrations verify schema consistency. Any startup exception halts the deployment gracefully to avoid inconsistent state.</p>

    <hr style="border: 0; border-top: 1px solid #E2E8F0; margin: 25px 0;">

    <!-- 5.2 DatabaseUtil.java -->
    <h2>5.2 DatabaseUtil.java</h2>
    <p><strong>Path:</strong> <code>src/main/java/com/djmart/util/DatabaseUtil.java</code></p>
    <p><strong>Description:</strong> Core database utility orchestrating JDBC connection pooling, schema migrations, and catalog seeding.</p>
    <p><strong>Key Responsibility:</strong> Manages the thread-safe <strong>HikariCP</strong> pool and distributes connections to DAOs.</p>
    <p><strong>Key Code Snippet:</strong></p>
    <pre><code>public static Connection getConnection() throws SQLException {
    if (dataSource == null) {
        initDataSource();
    }
    return dataSource.getConnection();
}</code></pre>
    <p><strong>Code Explanation:</strong> Instead of opening heavy physical connections per query, HikariCP maintains active pre-authenticated pooled connections, leasing them instantly to DAOs and reclaiming them upon close.</p>

    <hr style="border: 0; border-top: 1px solid #E2E8F0; margin: 25px 0;">

    <!-- 5.3 CsrfFilter.java -->
    <h2>5.3 CsrfFilter.java</h2>
    <p><strong>Path:</strong> <code>src/main/java/com/djmart/filter/CsrfFilter.java</code></p>
    <p><strong>Description:</strong> Security filter defending all state-mutating requests (POST, PUT, DELETE) against Cross-Site Request Forgery.</p>
    <p><strong>Key Responsibility:</strong> Validates cryptographically random session tokens against incoming form parameters or request headers.</p>
    <p><strong>Key Code Snippet:</strong></p>
    <pre><code>String sessionToken = (String) session.getAttribute("csrfToken");
String requestToken = req.getHeader("X-CSRF-TOKEN");
if (requestToken == null) {
    requestToken = req.getParameter("csrfToken");
}

if (!SecurityUtil.constantTimeEquals(sessionToken, requestToken)) {
    logger.warn("CSRF token validation failed! Blocking request.");
    res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
    return;
}
chain.doFilter(request, response);</code></pre>
    <p><strong>Code Explanation:</strong> Uses constant-time string comparison to prevent timing attacks. Rejects invalid requests with <strong>403 Forbidden</strong>.</p>

    <hr style="border: 0; border-top: 1px solid #E2E8F0; margin: 25px 0;">

    <!-- 5.4 AuthFilter.java -->
    <h2>5.4 AuthFilter.java</h2>
    <p><strong>Path:</strong> <code>src/main/java/com/djmart/filter/AuthFilter.java</code></p>
    <p><strong>Description:</strong> Role-Based Access Control (RBAC) security filter enforcing endpoint access privileges.</p>
    <p><strong>Key Responsibility:</strong> Inspects active HTTP sessions and blocks unauthorized access to <code>/admin/*</code> and <code>/seller/*</code> routes.</p>
    <p><strong>Key Code Snippet:</strong></p>
    <pre><code>User user = (User) session.getAttribute("user");
if (path.startsWith("/seller") && (user == null || user.getRole() != Role.SELLER)) {
    res.sendRedirect(req.getContextPath() + "/auth/login?error=unauthorized");
    return;
}
if (path.startsWith("/admin") && (user == null || user.getRole() != Role.ADMIN)) {
    res.sendRedirect(req.getContextPath() + "/auth/login?error=unauthorized");
    return;
}</code></pre>
    <p><strong>Code Explanation:</strong> Validates user credentials stored in the HTTP session. Redirects unauthorized users to the secure login prompt.</p>

    <hr style="border: 0; border-top: 1px solid #E2E8F0; margin: 25px 0;">

    <!-- 5.5 OrderServlet.java & OrderServiceImpl.java -->
    <h2>5.5 OrderServlet.java &amp; OrderServiceImpl.java</h2>
    <p><strong>Path:</strong> <code>src/main/java/com/djmart/controller/OrderServlet.java</code> &amp; <code>service/impl/OrderServiceImpl.java</code></p>
    <p><strong>Description:</strong> Order processing engine managing atomic checkout, stock reduction, and order history.</p>
    <p><strong>Key Responsibility:</strong> Executes atomic ACID transactions ensuring inventory is verified, stock is decremented, and orders are persisted without data inconsistency.</p>
    <p><strong>Key Code Snippet (OrderServiceImpl.java):</strong></p>
    <pre><code>connection.setAutoCommit(false); // Begin ACID Transaction
try {
    for (CartItem item : cart.getItems()) {
        Product product = productDao.findById(connection, item.getProductId());
        if (product.getStockQty() < item.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for: " + product.getName());
        }
        // Decrement product inventory
        productDao.decrementStock(connection, item.getProductId(), item.getQuantity());
    }
    // Persist order and line items
    Long orderId = orderDao.createOrder(connection, order);
    orderItemDao.batchInsert(connection, orderId, cart.getItems());
    cartDao.clearCart(connection, user.getId());

    connection.commit(); // Transaction Committed Successfully
} catch (Exception e) {
    connection.rollback(); // Rollback on any failure
    throw e;
}</code></pre>
    <p><strong>Code Explanation:</strong> <code>setAutoCommit(false)</code> guarantees atomic execution. If any single item fails stock validation, the entire transaction rolls back, preventing partial orders or orphaned inventory states.</p>

    <hr style="border: 0; border-top: 1px solid #E2E8F0; margin: 25px 0;">

    <!-- 5.6 api-client.js -->
    <h2>5.6 api-client.js</h2>
    <p><strong>Path:</strong> <code>src/main/webapp/static/js/api-client.js</code></p>
    <p><strong>Description:</strong> Lightweight asynchronous HTTP fetch wrapper managing AJAX interactions with backend REST APIs.</p>
    <p><strong>Key Responsibility:</strong> Automatically attaches CSRF tokens from document meta tags to state-changing requests, parses JSON responses, and dispatches UI toast notifications.</p>
    <p><strong>Key Code Snippet:</strong></p>
    <pre><code>async function request(endpoint, options = {}) {
    const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
    const headers = {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
        ...(csrfToken && { 'X-CSRF-TOKEN': csrfToken }),
        ...options.headers
    };
    const response = await fetch(endpoint, { ...options, headers });
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || 'Request failed');
    }
    return data;
}</code></pre>
    <p><strong>Code Explanation:</strong> Centralizes CSRF token attachment and HTTP error handling across all frontend modules without duplicate boilerplate.</p>
  `
};
