package com.djmart.controller;

import com.djmart.dto.ApiResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.AppException;
import com.djmart.exception.AuthenticationException;
import com.djmart.exception.AuthorizationException;
import com.djmart.exception.ValidationException;
import com.djmart.model.Role;
import com.djmart.util.JsonUtil;
import com.djmart.util.SecurityUtil;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Abstract base servlet providing centralized JSON serialization, request parsing,
 * parameter extraction, session security helpers, and standardized error handling.
 */
public abstract class BaseServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServlet.class);

    /**
     * Resolves the request path relative to the application context.
     * E.g. for context "/djmart" and URI "/djmart/api/v1/products", returns "/api/v1/products".
     */
    protected String getPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    /**
     * Checks if the current request expects or supplies a JSON payload.
     */
    protected boolean isJsonRequest(HttpServletRequest request) {
        String path = getPath(request);
        if (path.startsWith("/api/")) {
            return true;
        }
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    /**
     * Parses the incoming HTTP request body into a typed DTO object using Gson.
     *
     * @param request HTTP request containing JSON body
     * @param clazz Target DTO class
     * @return deserialized instance or null if body is empty
     * @throws ValidationException if the body contains invalid or malformed JSON
     * @throws IOException on I/O failure
     */
    protected <T> T parseRequestBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        String json = buffer.toString().trim();
        if (json.isEmpty()) {
            return null;
        }
        try {
            return JsonUtil.fromJson(json, clazz);
        } catch (JsonParseException e) {
            LOGGER.warn("Malformed JSON in request payload: {}", e.getMessage());
            throw new ValidationException("Invalid JSON format in request payload: " + e.getMessage());
        }
    }

    /**
     * Sends a structured JSON response conforming to the standard {@link ApiResponse} envelope.
     */
    protected void sendJsonResponse(HttpServletResponse response, int statusCode, ApiResponse<?> apiResponse)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtil.toJson(apiResponse));
        response.getWriter().flush();
    }

    /**
     * Sends a successful JSON response with HTTP 200/201 status and payload data.
     */
    protected <T> void sendSuccess(HttpServletResponse response, int statusCode, T data) throws IOException {
        sendJsonResponse(response, statusCode, ApiResponse.success(data));
    }

    /**
     * Sends a successful JSON response with HTTP status, custom message, and payload data.
     */
    protected <T> void sendSuccess(HttpServletResponse response, int statusCode, T data, String message)
            throws IOException {
        sendJsonResponse(response, statusCode, ApiResponse.success(message, data));
    }

    /**
     * Sends a standard error JSON response.
     */
    protected void sendError(HttpServletResponse response, int statusCode, String message, String errorCode)
            throws IOException {
        sendJsonResponse(response, statusCode, ApiResponse.error(message, errorCode));
    }

    /**
     * Centralized exception dispatcher ensuring clean responses and preventing stack trace leakage.
     */
    protected void handleException(HttpServletRequest request, HttpServletResponse response, Exception ex)
            throws IOException, ServletException {
        boolean isApi = isJsonRequest(request);

        if (ex instanceof ValidationException valEx) {
            LOGGER.warn("Validation failure on {}: {}", SecurityUtil.sanitizeForLog(request.getRequestURI()), valEx.getMessage());
            if (isApi) {
                if (valEx.getFieldErrors() != null && !valEx.getFieldErrors().isEmpty()) {
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            ApiResponse.validationError(valEx.getMessage(), valEx.getFieldErrors()));
                } else {
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            ApiResponse.error(valEx.getMessage(), valEx.getErrorCode()));
                }
            } else {
                request.setAttribute("errorMessage", valEx.getMessage());
                request.setAttribute("fieldErrors", valEx.getFieldErrors());
                throw new ServletException(valEx);
            }
        } else if (ex instanceof AppException appEx) {
            LOGGER.warn("Application exception [{}] on {}: {}",
                    appEx.getErrorCode(), SecurityUtil.sanitizeForLog(request.getRequestURI()), appEx.getMessage());
            if (isApi) {
                sendJsonResponse(response, appEx.getStatusCode(),
                        ApiResponse.error(appEx.getMessage(), appEx.getErrorCode()));
            } else {
                request.setAttribute("errorMessage", appEx.getMessage());
                response.sendError(appEx.getStatusCode(), appEx.getMessage());
            }
        } else {
            LOGGER.error("Unhandled server exception processing {}: ", SecurityUtil.sanitizeForLog(request.getRequestURI()), ex);
            if (isApi) {
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ApiResponse.error("An unexpected internal error occurred. Please try again later.", "INTERNAL_ERROR"));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred.");
            }
        }
    }

    /**
     * Retrieves the authenticated user from the current session, or null if not logged in.
     */
    protected UserResponse getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (UserResponse) session.getAttribute(SecurityUtil.SESSION_USER);
        }
        return null;
    }

    /**
     * Checks if the current session represents an authenticated user.
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        return getAuthenticatedUser(request) != null;
    }

    /**
     * Enforces that the current request is from an authenticated user; throws {@link AuthenticationException} otherwise.
     */
    protected UserResponse requireAuthenticatedUser(HttpServletRequest request) {
        UserResponse user = getAuthenticatedUser(request);
        if (user == null) {
            throw new AuthenticationException("Authentication required to access this resource");
        }
        return user;
    }

    /**
     * Enforces that the authenticated user possesses at least one of the required roles.
     */
    protected void requireRole(HttpServletRequest request, Role... allowedRoles) {
        UserResponse user = requireAuthenticatedUser(request);
        for (Role allowedRole : allowedRoles) {
            if (user.getRole() == allowedRole) {
                return;
            }
        }
        throw new AuthorizationException("Access denied: Insufficient privileges for role: " + user.getRole());
    }

    /**
     * Parses an integer query or form parameter with a fallback default value.
     */
    protected int getIntParam(HttpServletRequest request, String paramName, int defaultValue) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses a Long parameter, returning null if missing or empty.
     */
    protected Long getLongParam(HttpServletRequest request, String paramName) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid integer format for parameter: " + paramName);
        }
    }

    /**
     * Retrieves a trimmed string parameter or null if not present.
     */
    protected String getStringParam(HttpServletRequest request, String paramName) {
        String val = request.getParameter(paramName);
        return val != null ? val.trim() : null;
    }

    /**
     * Parses a BigDecimal parameter, returning null if missing.
     */
    protected BigDecimal getBigDecimalParam(HttpServletRequest request, String paramName) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(val.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid decimal format for parameter: " + paramName);
        }
    }

    /**
     * Forwards request to a JSP view within /WEB-INF/views/.
     */
    protected void forwardToJsp(HttpServletRequest request, HttpServletResponse response, String viewName)
            throws ServletException, IOException {
        String jspPath = "/WEB-INF/views/" + viewName + ".jsp";
        request.getRequestDispatcher(jspPath).forward(request, response);
    }

    /**
     * Performs a redirect relative to context path.
     */
    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }
}
