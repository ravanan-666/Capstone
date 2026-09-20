package com.djmart.filter;

import com.djmart.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CsrfFilterTest {

    private CsrfFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        filter = new CsrfFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    @DisplayName("GET and safe requests pass through and generate session CSRF token")
    void testSafeMethods_PassThrough() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/products");
        when(request.getMethod()).thenReturn("GET");
        when(session.getAttribute(SecurityUtil.SESSION_CSRF_TOKEN)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(session).setAttribute(eq(SecurityUtil.SESSION_CSRF_TOKEN), anyString());
        verify(request).setAttribute(eq(SecurityUtil.SESSION_CSRF_TOKEN), anyString());
        verify(response).setHeader(eq(SecurityUtil.CSRF_HEADER), anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("POST with valid CSRF token in HTTP header passes validation")
    void testPostWithValidHeaderToken() throws IOException, ServletException {
        String token = "valid-token-1234567890-abcdef";
        when(request.getRequestURI()).thenReturn("/api/v1/cart/items");
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute(SecurityUtil.SESSION_CSRF_TOKEN)).thenReturn(token);
        when(request.getHeader(SecurityUtil.CSRF_HEADER)).thenReturn(token);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("POST with valid CSRF token in form parameter passes validation")
    void testPostWithValidFormParamToken() throws IOException, ServletException {
        String token = "valid-token-1234567890-abcdef";
        when(request.getRequestURI()).thenReturn("/cart/add");
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute(SecurityUtil.SESSION_CSRF_TOKEN)).thenReturn(token);
        when(request.getHeader(SecurityUtil.CSRF_HEADER)).thenReturn(null);
        when(request.getParameter(SecurityUtil.CSRF_PARAM)).thenReturn(token);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("POST with missing CSRF token is rejected with 403 Forbidden")
    void testPostWithMissingToken_Rejected() throws IOException, ServletException {
        String sessionToken = "session-csrf-token";
        when(request.getRequestURI()).thenReturn("/api/v1/cart/items");
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute(SecurityUtil.SESSION_CSRF_TOKEN)).thenReturn(sessionToken);
        when(request.getHeader(SecurityUtil.CSRF_HEADER)).thenReturn(null);
        when(request.getParameter(SecurityUtil.CSRF_PARAM)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("CSRF_ERROR"));
    }

    @Test
    @DisplayName("POST with mismatched CSRF token is rejected with 403 Forbidden")
    void testPostWithInvalidToken_Rejected() throws IOException, ServletException {
        String sessionToken = "correct-token";
        when(request.getRequestURI()).thenReturn("/cart/update");
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute(SecurityUtil.SESSION_CSRF_TOKEN)).thenReturn(sessionToken);
        when(request.getHeader(SecurityUtil.CSRF_HEADER)).thenReturn("forged-attacker-token");

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Exempt authentication endpoints (/api/v1/auth/login, /auth/login) bypass CSRF check")
    void testExemptPaths_BypassValidation() throws IOException, ServletException {
        String[] exemptPaths = {
                "/auth/login", "/auth/register",
                "/api/v1/auth/login", "/api/v1/auth/register"
        };

        for (String path : exemptPaths) {
            reset(chain);
            when(request.getRequestURI()).thenReturn(path);
            when(request.getMethod()).thenReturn("POST");
            when(session.getAttribute(SecurityUtil.SESSION_CSRF_TOKEN)).thenReturn("some-token");
            when(request.getHeader(SecurityUtil.CSRF_HEADER)).thenReturn(null);
            when(request.getParameter(SecurityUtil.CSRF_PARAM)).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(chain, times(1)).doFilter(request, response);
        }
    }
}
