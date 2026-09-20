package com.djmart.listener;

import com.djmart.config.DatabaseConfig;
import com.djmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;

/**
 * Application context listener managing the lifecycle of the application.
 * Owns connection pool initialization, schema migration, and graceful shutdown.
 * Strictly adheres to rule 5: Connection pool lifecycle owned by a single ServletContextListener.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("==================================================");
        LOGGER.info("       Starting DjMart E-Commerce Marketplace     ");
        LOGGER.info("==================================================");

        try {
            // 1. Initialize configuration and HikariCP connection pool
            DatabaseConfig dbConfig = new DatabaseConfig();
            DatabaseUtil.initDataSource(dbConfig);

            // 2. Execute database schema migrations and seed data
            try (Connection conn = DatabaseUtil.getConnection()) {
                LOGGER.info("Connected to database successfully. Applying schema migrations...");
                DatabaseUtil.applyMigrations(conn);

                LOGGER.info("Checking and populating initial seed data...");
                DatabaseUtil.applySeedDataIfEmpty(conn);
            }

            LOGGER.info("DjMart database layer initialized and ready to serve requests.");
        } catch (Exception e) {
            LOGGER.error("CRITICAL: Failed to initialize application database layer: {}", e.getMessage(), e);
            throw new RuntimeException("Application startup aborted due to database initialization failure", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Shutting down DjMart application context...");

        // Gracefully close connection pool
        DatabaseUtil.closeDataSource();

        LOGGER.info("DjMart context destroyed cleanly.");
    }
}
