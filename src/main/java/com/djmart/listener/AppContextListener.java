package com.djmart.listener;

import com.djmart.config.DatabaseConfig;
import com.djmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;

/**
 * Application context listener managing the lifecycle of the application.
 * Owns connection pool initialization, schema migration, and graceful shutdown.
 * Strictly adheres to rule 5: Connection pool lifecycle owned by a single ServletContextListener.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContextListener.class);

    public AppContextListener() {
        System.out.println("[DJ Mart] AppContextListener instantiated successfully.");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("==================================================");
        System.out.println("       Starting DJ Mart E-Commerce Marketplace     ");
        System.out.println("==================================================");
        LOGGER.info("==================================================");
        LOGGER.info("       Starting DJ Mart E-Commerce Marketplace     ");
        LOGGER.info("==================================================");

        try {
            // 1. Initialize configuration and HikariCP connection pool
            DatabaseConfig dbConfig = new DatabaseConfig();
            DatabaseUtil.initDataSource(dbConfig);

            // 2. Execute database schema migrations and seed data
            try (Connection conn = DatabaseUtil.getConnection()) {
                LOGGER.info("Connected to database successfully. Applying schema migrations...");
                System.out.println("[DJ Mart] Connected to database successfully. Applying schema migrations...");
                DatabaseUtil.applyMigrations(conn);

                LOGGER.info("Checking and populating initial seed data...");
                System.out.println("[DJ Mart] Checking and populating initial seed data...");
                DatabaseUtil.applySeedDataIfEmpty(conn);

                LOGGER.info("Aligning table primary key identity sequences...");
                DatabaseUtil.alignIdentitySequences(conn);
            }

            LOGGER.info("DJ Mart database layer initialized and ready to serve requests.");
            System.out.println("[DJ Mart] Database layer initialized successfully and ready to serve requests.");
        } catch (Throwable t) {
            System.err.println("[DJ Mart CRITICAL] Failed to initialize application database layer: " + t.getMessage());
            t.printStackTrace(System.err);
            LOGGER.error("CRITICAL: Failed to initialize application database layer: {}", t.getMessage(), t);
            throw new RuntimeException("Application startup aborted due to database initialization failure: " + t.getMessage(), t);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[DJ Mart] Shutting down DJ Mart application context...");
        LOGGER.info("Shutting down DJ Mart application context...");

        // Gracefully close connection pool
        DatabaseUtil.closeDataSource();

        LOGGER.info("DJ Mart context destroyed cleanly.");
        System.out.println("[DJ Mart] Context destroyed cleanly.");
    }
}
