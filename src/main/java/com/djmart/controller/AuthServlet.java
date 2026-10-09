package com.djmart.controller;

import com.djmart.dao.jdbc.UserDAOImpl;
import com.djmart.dto.LoginRequest;
import com.djmart.dto.RegisterRequest;
import com.djmart.dto.UserResponse;
import com.djmart.exception.AppException;
import com.djmart.exception.AuthenticationException;
import com.djmart.exception.ConflictException;
import com.djmart.exception.ValidationException;
import com.djmart.model.Role;
import com.djmart.service.AuthService;
import com.djmart.service.impl.AuthServiceImpl;
import com.djmart.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller managing user authentication, registration, session lifecycles, and identity inspection.
 * Serves both browser JSP form submissions (/auth/*) and REST JSON clients (/api/v1/auth/*).
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/*", "/api/v1/auth/*"})
public class AuthServlet extends BaseServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServlet.class);

    private final AuthService authService;

    public AuthServlet() {
        this(new AuthServiceImpl(new UserDAOImpl()));
    }

    public AuthServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = getPath(request);

        switch (path) {
            case "/auth/login" -> showLoginForm(request, response);
            case "/auth/register" -> showRegisterForm(request, response);
            case "/auth/logout" -> processLogout(request, response, false);
            case "/api/v1/auth/me" -> handleGetMe(request, response);
            default -> {
                if (isJsonRequest(request)) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found", "NOT_FOUND");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = getPath(request);

        switch (path) {
            case "/auth/login" -> processLogin(request, response, false);
            case "/api/v1/auth/login" -> processLogin(request, response, true);
            case "/auth/register" -> processRegister(request, response, false);
            case "/api/v1/auth/register" -> processRegister(request, response, true);
            case "/auth/logout" -> processLogout(request, response, false);
            case "/api/v1/auth/logout" -> processLogout(request, response, true);
            default -> {
                if (isJsonRequest(request)) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found", "NOT_FOUND");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        }
    }

    // ==========================================
    // VIEW DISPATCHERS
    // ==========================================

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isAuthenticated(request)) {
            redirectToUserHome(request, response, getAuthenticatedUser(request).getRole());
            return;
        }

        String redirect = getStringParam(request, "redirect");
        if (isSafeRedirect(redirect)) {
            request.setAttribute("redirect", redirect);
        }

        if ("true".equalsIgnoreCase(request.getParameter("registered"))) {
            request.setAttribute("successMessage", "Account created successfully! Please sign in.");
        } else if ("true".equalsIgnoreCase(request.getParameter("loggedOut"))) {
            request.setAttribute("infoMessage", "You have been logged out securely.");
        }

        forwardToJsp(request, response, "auth/login");
    }

    private void showRegisterForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isAuthenticated(request)) {
            redirectToUserHome(request, response, getAuthenticatedUser(request).getRole());
            return;
        }
        forwardToJsp(request, response, "auth/register");
    }

    // ==========================================
    // AUTHENTICATION WORKFLOWS
    // ==========================================

    private void processLogin(HttpServletRequest request, HttpServletResponse response, boolean isApi)
            throws ServletException, IOException {
        LoginRequest loginRequest;
        String redirect = null;

        if (isApi && request.getContentType() != null && request.getContentType().contains("application/json")) {
            loginRequest = parseRequestBody(request, LoginRequest.class);
        } else {
            String email = getStringParam(request, "email");
            String password = request.getParameter("password");
            redirect = getStringParam(request, "redirect");
            loginRequest = new LoginRequest(email, password);
        }

        try {
            UserResponse user = authService.login(loginRequest);

            // SECURITY: Prevent Session Fixation attacks by regenerating the session identifier
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                request.changeSessionId();
            }
            HttpSession session = request.getSession(true);

            // Bind authenticated user and fresh CSRF token to the rotated session
            session.setAttribute(SecurityUtil.SESSION_USER, user);
            String newCsrfToken = SecurityUtil.generateSecureToken();
            session.setAttribute(SecurityUtil.SESSION_CSRF_TOKEN, newCsrfToken);
            response.setHeader(SecurityUtil.CSRF_HEADER, newCsrfToken);

            LOGGER.info("User {} (ID: {}, Role: {}) logged in successfully. Session secured.",
                    user.getEmail(), user.getId(), user.getRole());

            if (isApi) {
                sendSuccess(response, HttpServletResponse.SC_OK, user, "Login successful");
            } else {
                if (isSafeRedirect(redirect)) {
                    redirect(request, response, redirect);
                } else {
                    redirectToUserHome(request, response, user.getRole());
                }
            }
        } catch (AuthenticationException | ValidationException ex) {
            LOGGER.warn("Login failed: {}", ex.getMessage());
            if (isApi) {
                handleException(request, response, ex);
            } else {
                request.setAttribute("errorMessage", ex.getMessage());
                request.setAttribute("email", loginRequest != null ? loginRequest.getEmail() : null);
                if (isSafeRedirect(redirect)) {
                    request.setAttribute("redirect", redirect);
                }
                forwardToJsp(request, response, "auth/login");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during login: ", ex);
            if (isApi) {
                handleException(request, response, ex);
            } else {
                request.setAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
                forwardToJsp(request, response, "auth/login");
            }
        }
    }

    private void processRegister(HttpServletRequest request, HttpServletResponse response, boolean isApi)
            throws ServletException, IOException {
        RegisterRequest registerRequest;

        if (isApi && request.getContentType() != null && request.getContentType().contains("application/json")) {
            registerRequest = parseRequestBody(request, RegisterRequest.class);
        } else {
            String name = getStringParam(request, "name");
            String email = getStringParam(request, "email");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String role = getStringParam(request, "role");
            registerRequest = new RegisterRequest(name, email, password, confirmPassword, role);
        }

        try {
            UserResponse user = authService.register(registerRequest);

            // Establish secure session for the newly registered user
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                request.changeSessionId();
            }
            HttpSession session = request.getSession(true);

            session.setAttribute(SecurityUtil.SESSION_USER, user);
            String newCsrfToken = SecurityUtil.generateSecureToken();
            session.setAttribute(SecurityUtil.SESSION_CSRF_TOKEN, newCsrfToken);
            response.setHeader(SecurityUtil.CSRF_HEADER, newCsrfToken);

            LOGGER.info("User {} (ID: {}, Role: {}) registered and authenticated successfully.",
                    user.getEmail(), user.getId(), user.getRole());

            if (isApi) {
                sendSuccess(response, HttpServletResponse.SC_CREATED, user, "User registered successfully");
            } else {
                redirectToUserHome(request, response, user.getRole());
            }
        } catch (ValidationException ex) {
            LOGGER.warn("Registration validation failed: {}", ex.getMessage());
            if (isApi) {
                handleException(request, response, ex);
            } else {
                request.setAttribute("errorMessage", ex.getMessage());
                request.setAttribute("fieldErrors", ex.getFieldErrors());
                populateFormAttributes(request, registerRequest);
                forwardToJsp(request, response, "auth/register");
            }
        } catch (ConflictException ex) {
            LOGGER.warn("Registration conflict: {}", ex.getMessage());
            if (isApi) {
                handleException(request, response, ex);
            } else {
                request.setAttribute("errorMessage", ex.getMessage());
                populateFormAttributes(request, registerRequest);
                forwardToJsp(request, response, "auth/register");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during registration: ", ex);
            if (isApi) {
                handleException(request, response, ex);
            } else {
                request.setAttribute("errorMessage", "Registration failed due to a system error. Please try again.");
                populateFormAttributes(request, registerRequest);
                forwardToJsp(request, response, "auth/register");
            }
        }
    }

    private void processLogout(HttpServletRequest request, HttpServletResponse response, boolean isApi)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            UserResponse user = (UserResponse) session.getAttribute(SecurityUtil.SESSION_USER);
            if (user != null) {
                LOGGER.info("User {} (ID: {}) logged out.", user.getEmail(), user.getId());
            }
            session.removeAttribute(SecurityUtil.SESSION_USER);
            session.removeAttribute(SecurityUtil.SESSION_CSRF_TOKEN);
            session.invalidate();
        }

        if (isApi) {
            sendSuccess(response, HttpServletResponse.SC_OK, null, "Logout successful");
        } else {
            redirect(request, response, "/auth/login?loggedOut=true");
        }
    }

    private void handleGetMe(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserResponse user = getAuthenticatedUser(request);
        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required to inspect current user", "UNAUTHENTICATED");
            return;
        }
        sendSuccess(response, HttpServletResponse.SC_OK, user);
    }

    // ==========================================
    // HELPERS & VALIDATIONS
    // ==========================================

    private void redirectToUserHome(HttpServletRequest request, HttpServletResponse response, Role role)
            throws IOException {
        if (role == Role.ADMIN) {
            redirect(request, response, "/admin/dashboard");
        } else if (role == Role.SELLER) {
            redirect(request, response, "/seller/dashboard");
        } else {
            redirect(request, response, "/");
        }
    }

    /**
     * Prevents Open Redirect attacks by confirming path starts with single forward slash
     * and contains no protocol or double slashes.
     */
    private boolean isSafeRedirect(String redirect) {
        if (redirect == null || redirect.trim().isEmpty()) {
            return false;
        }
        String trimmed = redirect.trim();
        return trimmed.startsWith("/") && !trimmed.startsWith("//") && !trimmed.startsWith("/\\") && !trimmed.contains(":");
    }

    private void populateFormAttributes(HttpServletRequest request, RegisterRequest req) {
        if (req != null) {
            request.setAttribute("name", req.getName());
            request.setAttribute("email", req.getEmail());
            request.setAttribute("role", req.getRole());
        }
    }
}
