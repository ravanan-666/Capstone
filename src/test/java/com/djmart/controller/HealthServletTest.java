package com.djmart.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class HealthServletTest {

    private DataSource dataSource;
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private HealthServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        resultSet = mock(ResultSet.class);
        servlet = new HealthServlet(dataSource);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getRequestURI()).thenReturn("/api/v1/health");
        when(request.getContextPath()).thenReturn("");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
    }

    @Test
    @DisplayName("Health check returns 200 OK when database is accessible")
    void testHealthCheck_Healthy() throws IOException, SQLException {
        when(resultSet.next()).thenReturn(true);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String output = responseWriter.toString();
        assertTrue(output.contains("\"status\":\"UP\""));
        assertTrue(output.contains("\"database\":\"CONNECTED\""));
    }

    @Test
    @DisplayName("Health check returns 503 Service Unavailable when database connection fails")
    void testHealthCheck_Unhealthy() throws IOException, SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        String output = responseWriter.toString();
        assertTrue(output.contains("\"status\":\"DOWN\""));
        assertTrue(output.contains("\"database\":\"DISCONNECTED\""));
    }
}
