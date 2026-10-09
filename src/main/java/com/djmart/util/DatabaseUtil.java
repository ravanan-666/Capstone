package com.djmart.util;

import com.djmart.config.DatabaseConfig;
import com.djmart.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Thread-safe utility managing the HikariCP connection pool lifecycle and schema execution.
 * Enforces pure JDBC access, try-with-resources, and zero connection leaks.
 */
public final class DatabaseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtil.class);

    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    private DatabaseUtil() {
        // Prevent instantiation
    }

    /**
     * Initializes the HikariCP connection pool using the provided database configuration.
     *
     * @param config Database configuration settings
     */
    public static void initDataSource(DatabaseConfig config) {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.info("HikariCP DataSource is already initialized");
            return;
        }

        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) {
                return;
            }

            try {
                // Ensure JDBC driver is loaded
                try {
                    Class.forName(config.getDriverClassName());
                } catch (ClassNotFoundException cnfe) {
                    LOGGER.warn("Driver class {} not found via Class.forName: {}", config.getDriverClassName(), cnfe.getMessage());
                }

                // Ensure H2 target directory exists
                ensureH2DirectoryExists(config.getJdbcUrl());

                LOGGER.info("Initializing HikariCP DataSource with URL: {}", config.getJdbcUrl());
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setPoolName("DjMartHikariPool");
                hikariConfig.setDriverClassName(config.getDriverClassName());
                hikariConfig.setJdbcUrl(config.getJdbcUrl());
                hikariConfig.setUsername(config.getUsername());
                hikariConfig.setPassword(config.getPassword());

                hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
                hikariConfig.setMinimumIdle(config.getMinimumIdle());
                hikariConfig.setIdleTimeout(config.getIdleTimeoutMs());
                hikariConfig.setConnectionTimeout(config.getConnectionTimeoutMs());
                hikariConfig.setMaxLifetime(config.getMaxLifetimeMs());
                hikariConfig.setLeakDetectionThreshold(config.getLeakDetectionThresholdMs());

                // Additional robust H2 / Pool optimizations
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

                dataSource = new HikariDataSource(hikariConfig);
                LOGGER.info("HikariCP connection pool successfully initialized. Max pool size: {}, Min idle: {}",
                        config.getMaximumPoolSize(), config.getMinimumIdle());
            } catch (Exception e) {
                LOGGER.error("Failed to initialize HikariCP connection pool: {}", e.getMessage(), e);
                throw new DatabaseException("Failed to initialize HikariCP connection pool", e);
            }
        }
    }

    /**
     * Obtains a connection from the pool.
     *
     * @return an active SQL connection
     * @throws SQLException if a pool error occurs or DataSource is not initialized
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            synchronized (LOCK) {
                if (dataSource == null || dataSource.isClosed()) {
                    LOGGER.info("DataSource was not initialized; initializing with default configuration");
                    initDataSource(new DatabaseConfig());
                }
            }
        }
        return dataSource.getConnection();
    }

    /**
     * Returns the underlying HikariDataSource.
     *
     * @return the active HikariDataSource
     */
    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Checks if the pool is active and ready.
     *
     * @return true if initialized and not closed
     */
    public static boolean isPoolInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Gracefully shuts down the HikariCP connection pool.
     */
    public static void closeDataSource() {
        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) {
                LOGGER.info("Shutting down HikariCP connection pool...");
                dataSource.close();
                dataSource = null;
                LOGGER.info("HikariCP connection pool successfully shut down");
            }
        }
    }

    /**
     * Executes a SQL script file loaded from the classpath.
     *
     * @param conn active database connection
     * @param resourcePath classpath path to the SQL script
     */
    public static void runScript(Connection conn, String resourcePath) {
        LOGGER.info("Executing SQL script from classpath: {}", resourcePath);
        InputStream in = DatabaseUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            // Also try leading slash if absent
            String alt = resourcePath.startsWith("/") ? resourcePath.substring(1) : "/" + resourcePath;
            in = DatabaseUtil.class.getClassLoader().getResourceAsStream(alt);
        }

        if (in == null) {
            throw new DatabaseException("Could not find SQL script resource: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Ignore empty lines and full line comments
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("//")) {
                    continue;
                }

                // Handle inline comments
                int commentIndex = line.indexOf("--");
                if (commentIndex >= 0) {
                    line = line.substring(0, commentIndex);
                }

                sqlBuilder.append(line).append("\n");

                if (trimmed.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                    sqlBuilder.setLength(0);
                }
            }

            // Execute any remaining statement
            String remaining = sqlBuilder.toString().trim();
            if (!remaining.isEmpty()) {
                if (remaining.endsWith(";")) {
                    remaining = remaining.substring(0, remaining.length() - 1);
                }
                stmt.execute(remaining);
            }

            LOGGER.info("Successfully executed SQL script: {}", resourcePath);
        } catch (Exception e) {
            LOGGER.error("Failed executing SQL script {}: {}", resourcePath, e.getMessage(), e);
            throw new DatabaseException("Error executing SQL script: " + resourcePath, e);
        }
    }

    /**
     * Initializes database schema using migrations.
     *
     * @param conn active database connection
     */
    public static void applyMigrations(Connection conn) {
        LOGGER.info("Applying database migrations...");
        runScript(conn, "db/migrations/V1__init_schema.sql");
        runScript(conn, "db/migrations/V2__add_performance_indexes.sql");
        runScript(conn, "db/migrations/V3__expand_catalog_and_features.sql");
        LOGGER.info("All database migrations applied successfully");
        alignIdentitySequences(conn);
    }

    /**
     * Populates database with seed data if products table has fewer than 20 records.
     *
     * @param conn active database connection
     */
    public static void applySeedDataIfEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
            if (rs.next() && rs.getInt(1) < 20) {
                LOGGER.info("Products count is < 20 (found: {}); populating full 24-product seed catalog...", rs.getInt(1));
                runScript(conn, "db/seed.sql");
                LOGGER.info("Seed data applied successfully");
            } else {
                LOGGER.info("Products table already populated with {} products; skipping seed data", rs.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.warn("Could not check products table count: {}", e.getMessage());
            // Attempt seed run
            runScript(conn, "db/seed.sql");
        }
        alignIdentitySequences(conn);
    }

    /**
     * Aligns table primary key auto-increment / identity sequences with current MAX(id)
     * to prevent collisions after explicit ID inserts in migrations or seed data.
     * Compatible with H2 1.x/2.x and PostgreSQL.
     *
     * @param conn active database connection
     */
    public static void alignIdentitySequences(Connection conn) {
        String[] tables = {
            "users", "categories", "products", "orders", "order_items",
            "cart_items", "reviews", "chat_conversations", "chat_messages"
        };

        try {
            String dbProduct = "";
            try {
                if (conn.getMetaData() != null && conn.getMetaData().getDatabaseProductName() != null) {
                    dbProduct = conn.getMetaData().getDatabaseProductName();
                }
            } catch (Exception e) {
                LOGGER.debug("Could not determine DB product name: {}", e.getMessage());
            }

            boolean isPostgres = dbProduct.toUpperCase().contains("POSTGRES");
            LOGGER.info("Synchronizing auto-increment sequences for {} tables (DB: {})...", tables.length, dbProduct);

            for (String table : tables) {
                try (Statement stmt = conn.createStatement()) {
                    long maxId = 0;
                    try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(id), 0) FROM " + table)) {
                        if (rs.next()) {
                            maxId = rs.getLong(1);
                        }
                    }

                    long nextId = maxId + 1;
                    if (isPostgres) {
                        try {
                            stmt.execute("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), " + Math.max(maxId, 1) + ")");
                            LOGGER.debug("PostgreSQL sequence for {} synchronized to {}", table, maxId);
                        } catch (SQLException pe) {
                            LOGGER.debug("PostgreSQL sequence setval failed for {}: {}", table, pe.getMessage());
                        }
                    } else {
                        // Default to H2 syntax
                        try {
                            stmt.executeUpdate("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + nextId);
                            LOGGER.debug("H2 sequence for {} restarted with {}", table, nextId);
                        } catch (SQLException h2e) {
                            LOGGER.debug("H2 sequence restart failed for {}: {}", table, h2e.getMessage());
                        }
                    }
                } catch (SQLException sqle) {
                    LOGGER.debug("Notice while checking table {} for sequence alignment: {}", table, sqle.getMessage());
                }
            }
            LOGGER.info("Primary key identity sequences successfully synchronized.");
        } catch (Exception e) {
            LOGGER.warn("Exception during sequence synchronization: {}", e.getMessage());
        }
    }

    /**
     * Ensures that the directory for an H2 file database exists on the filesystem.
     *
     * @param jdbcUrl H2 JDBC connection string
     */
    private static void ensureH2DirectoryExists(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:h2:")) {
            return;
        }
        try {
            String pathPart = jdbcUrl.substring("jdbc:h2:".length());
            if (pathPart.startsWith("mem:") || pathPart.startsWith("tcp:")) {
                return;
            }
            if (pathPart.startsWith("file:")) {
                pathPart = pathPart.substring("file:".length());
            }
            int semicolonIdx = pathPart.indexOf(';');
            if (semicolonIdx > 0) {
                pathPart = pathPart.substring(0, semicolonIdx);
            }
            File dbFile = new File(pathPart);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                LOGGER.info("Created H2 database directory: {} (success: {})", parentDir.getAbsolutePath(), created);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to ensure H2 database directory for URL {}: {}", jdbcUrl, e.getMessage());
        }
    }
}
