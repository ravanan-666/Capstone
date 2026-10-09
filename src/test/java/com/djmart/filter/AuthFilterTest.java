package com.djmart.filter;

import com.djmart.dto.UserResponse;
import com.djmart.model.Role;
import com.djmart.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthFilterTest {

    private AuthFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        filter = new AuthFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
    }

    private UserResponse createUser(Long id, Role role) {
        return new UserResponse(id, "User " + id, "user" + id + "@djmart.com", role, Timestamp.from(Instant.now()));
    }

    @Test
    @DisplayName("Public paths allow access without an active session")
    void testPublicPaths_Allowed() throws IOException, ServletException {
        String[] publicPaths = {
                "/", "/index.jsp", "/auth/login", "/auth/register",
                "/api/v1/auth/login", "/api/v1/auth/register",
                "/api/v1/health", "/static/css/style.css",
                "/products", "/api/v1/products"
        };

        for (String path : publicPaths) {
            reset(chain, request);
            when(request.getContextPath()).thenReturn("");
            when(request.getRequestURI()).thenReturn(path);
            when(request.getMethod()).thenReturn("GET");

            filter.doFilter(request, response, chain);

            verify(chain, times(1)).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Unauthenticated access to protected API endpoint returns 401 JSON envelope")
    void testUnauthenticated_Api_Returns401() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("UNAUTHENTICATED"));
        assertTrue(responseWriter.toString().contains("\"success\":false"));
    }

    @Test
    @DisplayName("Unauthenticated access to protected browser endpoint redirects to login with return path")
    void testUnauthenticated_Browser_RedirectsToLogin() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        verify(response).sendRedirect("/auth/login?redirect=%2Forders");
    }

    @Test
    @DisplayName("Admin routes: Accessible by ADMIN, rejected with 403 for BUYER and SELLER")
    void testAdminRoutes_RBAC() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/admin/dashboard");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(session);

        // 1. ADMIN allowed
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(1L, Role.ADMIN));
        filter.doFilter(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);

        // 2. BUYER forbidden
        reset(chain, response);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(2L, Role.BUYER));
        filter.doFilter(request, response, chain);
        verify(chain, never()).doFilter(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());

        // 3. SELLER forbidden
        reset(chain, response);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(3L, Role.SELLER));
        filter.doFilter(request, response, chain);
        verify(chain, never()).doFilter(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Seller routes: Accessible by SELLER, rejected with 403 for BUYER")
    void testSellerRoutes_RBAC() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/seller/dashboard");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(session);

        // 1. SELLER allowed
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(2L, Role.SELLER));
        filter.doFilter(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);

        // 2. BUYER forbidden
        reset(chain, response);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(3L, Role.BUYER));
        filter.doFilter(request, response, chain);
        verify(chain, never()).doFilter(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Buyer routes: Accessible by BUYER, rejected with 403 for SELLER")
    void testBuyerRoutes_RBAC() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/cart");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(session);

        // 1. BUYER allowed
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(3L, Role.BUYER));
        filter.doFilter(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);

        // 2. SELLER forbidden
        reset(chain, response);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(2L, Role.SELLER));
        filter.doFilter(request, response, chain);
        verify(chain, never()).doFilter(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Product modifications (POST/PUT/DELETE /api/v1/products) require SELLER or ADMIN role")
    void testProductModifications_RequireSellerOrAdmin() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);

        // BUYER denied
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(3L, Role.BUYER));
        filter.doFilter(request, response, chain);
        verify(chain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("FORBIDDEN"));

        // SELLER allowed
        reset(chain, response);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(createUser(2L, Role.SELLER));
        filter.doFilter(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);
    }
}
