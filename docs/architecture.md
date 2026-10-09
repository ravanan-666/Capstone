# DJ Mart System Architecture Documentation

## 1. Architectural Philosophy
DJ Mart implements the classic **Layered MVC / Front Controller Pattern** over Java Servlets, JSP/JSTL, and raw JDBC.

```
+-------------------------------------------------------------------+
|                        Client Tier                                |
|  - JSP / JSTL Views (Server-rendered HTML)                       |
|  - Vanilla JavaScript & AJAX Fetch API                            |
+---------------------------------+---------------------------------+
                                  | HTTP (REST & Form POST)
                                  v
+-------------------------------------------------------------------+
|                        Filter Pipeline                            |
|  - EncodingFilter: Enforces UTF-8 request & response encoding     |
|  - RequestLoggingFilter: Generates UUID request ID for SLF4J MDC  |
|  - AuthFilter: RBAC session validation (BUYER, SELLER, ADMIN)     |
+---------------------------------+---------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                     Controller Tier (Servlets)                    |
|  - Thin HTTP dispatchers                                          |
|  - Unpacks HTTP requests, queries, and JSON payloads              |
|  - Translates domain results into DTOs & standardized envelopes   |
+---------------------------------+---------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                        Service Tier                               |
|  - Pure business logic and domain orchestration                   |
|  - Input validation prior to DAO invocation                       |
|  - Transaction boundaries (Connection.setAutoCommit(false))       |
|  - Zero SQL queries or JDBC dependencies                          |
+---------------------------------+---------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                          DAO Tier                                 |
|  - Data Access Object interfaces & implementations               |
|  - ONLY layer containing SQL statements                           |
|  - Enforces PreparedStatement and try-with-resources             |
+---------------------------------+---------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                   Connection Pooling & Database                   |
|  - HikariCP (Managed via AppContextListener)                      |
|  - H2 Database Engine                                             |
+-------------------------------------------------------------------+
```

## 2. Key Design Patterns Applied
- **Data Access Object (DAO)**: Abstract database access operations from business services.
- **Singleton**: Connection pool managed via `DatabaseUtil` and owned by `AppContextListener`.
- **Front Controller / Layered Dispatch**: Centralized filter pipeline routing into thin resource servlets.
- **DTO (Data Transfer Object)**: Separation of internal database entities from external API request/response shapes.
- **Strategy Pattern**: Swappable notification/payment mock and AI chatbot providers (`GeminiChatProvider` vs `MockChatProvider`).

## 3. Database Connection Lifecycle
- **Initialization**: When Tomcat boots DJ Mart, `AppContextListener.contextInitialized` initializes HikariCP using `DatabaseConfig`.
- **Acquisition**: `BaseDAO` or Service calls `DatabaseUtil.getConnection()`.
- **Release**: Handled strictly via Java `try-with-resources` blocks (`try (Connection conn = ...) { ... }`), guaranteeing no leaks.
- **Shutdown**: When Tomcat stops DJ Mart, `AppContextListener.contextDestroyed` closes the pool gracefully.
