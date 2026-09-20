package com.djmart.controller;

import com.djmart.dto.LoginRequest;
import com.djmart.dto.RegisterRequest;
import com.djmart.dto.UserResponse;
import com.djmart.exception.AuthenticationException;
import com.djmart.exception.ConflictException;
import com.djmart.exception.ValidationException;
import com.djmart.model.Role;
import com.djmart.service.AuthService;
import com.djmart.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServletTest {

    private AuthService authService;
    private AuthServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        authService = mock(AuthService.class);
        servlet = new AuthServlet(authService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private UserResponse createSampleUser(Long id, String email, Role role) {
        return new UserResponse(id, "Test User", email, role, Timestamp.from(Instant.now()));
    }

    @Test
    @DisplayName("Browser Login: Successful authentication rotates session ID, binds user, and redirects")
    void testBrowserLogin_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("email")).thenReturn("buyer@djmart.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("redirect")).thenReturn(null);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);

        UserResponse sampleUser = createSampleUser(1L, "buyer@djmart.com", Role.BUYER);
        when(authService.login(any(LoginRequest.class))).thenReturn(sampleUser);

        servlet.doPost(request, response);

        // Verify session fixation prevention
        verify(request).changeSessionId();
        verify(session).setAttribute(eq(SecurityUtil.SESSION_USER), eq(sampleUser));
        verify(session).setAttribute(eq(SecurityUtil.SESSION_CSRF_TOKEN), anyString());
        verify(response).sendRedirect("/");
    }

    @Test
    @DisplayName("Browser Login: Safe redirect parameter is respected on login")
    void testBrowserLogin_SafeRedirect() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("email")).thenReturn("buyer@djmart.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("redirect")).thenReturn("/cart");
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(session);

        UserResponse sampleUser = createSampleUser(1L, "buyer@djmart.com", Role.BUYER);
        when(authService.login(any(LoginRequest.class))).thenReturn(sampleUser);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/cart");
    }

    @Test
    @DisplayName("Browser Login: Open redirect attempt is neutralized and directed to home")
    void testBrowserLogin_OpenRedirectNeutralized() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("email")).thenReturn("buyer@djmart.com");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getParameter("redirect")).thenReturn("//evil.com/phish");
        when(request.getSession(true)).thenReturn(session);

        UserResponse sampleUser = createSampleUser(1L, "buyer@djmart.com", Role.BUYER);
        when(authService.login(any(LoginRequest.class))).thenReturn(sampleUser);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/");
    }

    @Test
    @DisplayName("Browser Login: Invalid credentials re-renders login form with error message")
    void testBrowserLogin_InvalidCredentials() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("email")).thenReturn("buyer@djmart.com");
        when(request.getParameter("password")).thenReturn("WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid email or password"));

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), eq("Invalid email or password"));
        verify(request).getRequestDispatcher("/WEB-INF/views/auth/login.jsp");
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("API Login: Successful JSON login returns 200 with UserResponse and CSRF header")
    void testApiLogin_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json");

        String json = "{\"email\":\"seller@djmart.com\",\"password\":\"Password@123\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);

        UserResponse sellerUser = createSampleUser(2L, "seller@djmart.com", Role.SELLER);
        when(authService.login(any(LoginRequest.class))).thenReturn(sellerUser);

        servlet.doPost(request, response);

        verify(request).changeSessionId();
        verify(session).setAttribute(eq(SecurityUtil.SESSION_USER), eq(sellerUser));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setHeader(eq(SecurityUtil.CSRF_HEADER), anyString());

        String output = responseWriter.toString();
        assertTrue(output.contains("\"success\":true"));
        assertTrue(output.contains("\"email\":\"seller@djmart.com\""));
        assertFalse(output.contains("password"));
    }

    @Test
    @DisplayName("API Login: Invalid credentials returns 401 UNAUTHENTICATED JSON envelope")
    void testApiLogin_InvalidCredentials() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json");

        String json = "{\"email\":\"seller@djmart.com\",\"password\":\"WrongPassword\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid email or password"));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String output = responseWriter.toString();
        assertTrue(output.contains("\"success\":false"));
        assertTrue(output.contains("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("API Register: Successful registration returns 201 CREATED with UserResponse")
    void testApiRegister_Success() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json");

        String json = "{\"name\":\"Alice Smith\",\"email\":\"alice@djmart.com\",\"password\":\"Password@123\",\"confirmPassword\":\"Password@123\",\"role\":\"BUYER\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(request.getSession(true)).thenReturn(session);

        UserResponse newUser = createSampleUser(10L, "alice@djmart.com", Role.BUYER);
        when(authService.register(any(RegisterRequest.class))).thenReturn(newUser);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        String output = responseWriter.toString();
        assertTrue(output.contains("\"success\":true"));
        assertTrue(output.contains("\"email\":\"alice@djmart.com\""));
    }

    @Test
    @DisplayName("API Register: Duplicate email returns 409 CONFLICT JSON envelope")
    void testApiRegister_DuplicateEmail() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json");

        String json = "{\"name\":\"Duplicate User\",\"email\":\"buyer@djmart.com\",\"password\":\"Password@123\",\"confirmPassword\":\"Password@123\",\"role\":\"BUYER\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("Email address is already registered"));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        String output = responseWriter.toString();
        assertTrue(output.contains("\"success\":false"));
        assertTrue(output.contains("CONFLICT"));
    }

    @Test
    @DisplayName("Logout: Session is invalidated and client redirected or answered with 200")
    void testLogout_BrowserAndApi() throws ServletException, IOException {
        // 1. Browser logout
        when(request.getRequestURI()).thenReturn("/auth/logout");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(session);

        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/auth/login?loggedOut=true");

        // 2. API logout
        reset(session, response);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getRequestURI()).thenReturn("/api/v1/auth/logout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).invalidate();
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
    }

    @Test
    @DisplayName("/api/v1/auth/me: Returns current user when authenticated, 401 when not")
    void testGetMe() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/me");
        when(request.getMethod()).thenReturn("GET");

        // Unauthenticated
        when(request.getSession(false)).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("UNAUTHENTICATED"));

        // Authenticated
        reset(response);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getSession(false)).thenReturn(session);
        UserResponse user = createSampleUser(5L, "me@djmart.com", Role.ADMIN);
        when(session.getAttribute(SecurityUtil.SESSION_USER)).thenReturn(user);

        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"email\":\"me@djmart.com\""));
        assertTrue(responseWriter.toString().contains("\"role\":\"ADMIN\""));
    }
}
