package com.djmart.util;

import com.djmart.config.DatabaseConfig;
import com.djmart.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
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
        LOGGER.info("All database migrations applied successfully");
    }

    /**
     * Populates database with seed data if users table is empty.
     *
     * @param conn active database connection
     */
    public static void applySeedDataIfEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("Users table is empty; seeding development data...");
                runScript(conn, "db/seed.sql");
                LOGGER.info("Seed data applied successfully");
            } else {
                LOGGER.info("Users table already populated; skipping seed data");
            }
        } catch (SQLException e) {
            LOGGER.warn("Could not check users table count: {}", e.getMessage());
            // Attempt seed run
            runScript(conn, "db/seed.sql");
        }
    }
}
