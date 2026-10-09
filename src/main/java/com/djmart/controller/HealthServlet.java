package com.djmart.controller;

import com.djmart.dto.ApiResponse;
import com.djmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoint for system monitoring, container liveness probes, and test diagnostics.
 * Exposed at GET /api/v1/health.
 */
@WebServlet(name = "HealthServlet", urlPatterns = {"/api/v1/health"})
public class HealthServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthServlet.class);

    private final DataSource dataSource;

    public HealthServlet() {
        this(DatabaseUtil.getDataSource());
    }

    public HealthServlet(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            if (rs.next()) {
                health.put("db", "UP");
                health.put("database", "CONNECTED");
                sendSuccess(response, HttpServletResponse.SC_OK, health, "DJ Mart system healthy");
            } else {
                health.put("status", "DEGRADED");
                health.put("database", "NO_RESULT");
                ApiResponse<Map<String, Object>> errResp = ApiResponse.error("Database query returned unexpected result", "DB_DEGRADED");
                errResp.setData(health);
                sendJsonResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, errResp);
            }
        } catch (Exception e) {
            LOGGER.error("Health check failed: database connectivity issue", e);
            health.put("status", "DOWN");
            health.put("database", "DISCONNECTED");
            ApiResponse<Map<String, Object>> errResp = ApiResponse.error("Database service unavailable", "DB_UNAVAILABLE");
            errResp.setData(health);
            sendJsonResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, errResp);
        }
    }
}
