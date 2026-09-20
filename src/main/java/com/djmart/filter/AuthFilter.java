package com.djmart.filter;

import com.djmart.dto.ApiResponse;
import com.djmart.dto.UserResponse;
import com.djmart.model.Role;
import com.djmart.util.JsonUtil;
import com.djmart.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Security filter enforcing session authentication and Role-Based Access Control (RBAC).
 * Prevents unauthorized privilege escalation between BUYER, SELLER, and ADMIN actors.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/*")
public class AuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String method = httpRequest.getMethod().toUpperCase();

        // 1. Static resources and public endpoints are unrestricted
        if (isPublicPath(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Verify active session authentication
        HttpSession session = httpRequest.getSession(false);
        UserResponse user = session != null ? (UserResponse) session.getAttribute(SecurityUtil.SESSION_USER) : null;

        boolean isApi = path.startsWith("/api/") ||
                (httpRequest.getHeader("Accept") != null && httpRequest.getHeader("Accept").contains("application/json"));

        if (user == null) {
            LOGGER.warn("Unauthenticated access attempt to protected route: {} {}", method, SecurityUtil.sanitizeForLog(path));
            if (isApi) {
                sendApiError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required to access this resource", "UNAUTHENTICATED");
            } else {
                String redirectUrl = httpRequest.getContextPath() + "/auth/login?redirect=" +
                        URLEncoder.encode(path, StandardCharsets.UTF_8);
                httpResponse.sendRedirect(redirectUrl);
            }
            return;
        }

        // 3. Role-Based Access Control (RBAC) authorization checks
        Role role = user.getRole();

        // Admin-only routes
        if (isAdminPath(path)) {
            if (role != Role.ADMIN) {
                LOGGER.warn("Access denied: User ID {} with role {} attempted to access admin route {}",
                        user.getId(), role, SecurityUtil.sanitizeForLog(path));
                handleForbidden(httpRequest, httpResponse, isApi);
                return;
            }
        }

        // Seller-only routes (Permitted for SELLER and ADMIN)
        if (isSellerPath(path, method)) {
            if (role != Role.SELLER && role != Role.ADMIN) {
                LOGGER.warn("Access denied: User ID {} with role {} attempted to access seller route {}",
                        user.getId(), role, SecurityUtil.sanitizeForLog(path));
                handleForbidden(httpRequest, httpResponse, isApi);
                return;
            }
        }

        // Buyer-specific routes (Permitted for BUYER and ADMIN)
        if (isBuyerPath(path, method)) {
            if (role != Role.BUYER && role != Role.ADMIN) {
                LOGGER.warn("Access denied: User ID {} with role {} attempted to access buyer route {}",
                        user.getId(), role, SecurityUtil.sanitizeForLog(path));
                handleForbidden(httpRequest, httpResponse, isApi);
                return;
            }
        }

        // Access authorized
        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path, String method) {
        if (path.startsWith("/static/") || path.equals("/favicon.ico")) {
            return true;
        }
        if (path.equals("/") || path.equals("/index.jsp")) {
            return true;
        }
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
            return true;
        }
        if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register")) {
            return true;
        }
        if (path.equals("/api/v1/health")) {
            return true;
        }
        // Public browse catalog endpoints (GET only)
        if ("GET".equals(method)) {
            if (path.equals("/products") || path.startsWith("/products/")) {
                return true;
            }
            if (path.equals("/api/products") || path.startsWith("/api/products/")) {
                return true;
            }
            if (path.equals("/api/v1/products") || path.startsWith("/api/v1/products/")) {
                return true;
            }
            if (path.startsWith("/api/reviews/product/") || path.startsWith("/api/v1/reviews/product/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdminPath(String path) {
        return path.startsWith("/admin") || path.startsWith("/api/v1/admin") || path.startsWith("/api/admin");
    }

    private boolean isSellerPath(String path, String method) {
        if (path.startsWith("/seller") || path.startsWith("/api/v1/seller") || path.startsWith("/api/seller")) {
            return true;
        }
        // Creating, modifying, deleting products via API
        if (path.startsWith("/api/products") || path.startsWith("/api/v1/products")) {
            return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
        }
        return false;
    }

    private boolean isBuyerPath(String path, String method) {
        if (path.startsWith("/cart") || path.startsWith("/api/cart") || path.startsWith("/api/v1/cart")) {
            return true;
        }
        if (path.startsWith("/checkout") || path.startsWith("/api/checkout") || path.startsWith("/api/v1/checkout")) {
            return true;
        }
        if (path.equals("/orders") || path.startsWith("/orders/")) {
            return true;
        }
        if ((path.startsWith("/api/orders") || path.startsWith("/api/v1/orders"))
                && !path.startsWith("/api/orders/seller") && !path.startsWith("/api/v1/orders/seller")) {
            return true;
        }
        if ((path.startsWith("/api/reviews") || path.startsWith("/api/v1/reviews")) && "POST".equals(method)) {
            return true;
        }
        return false;
    }

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response, boolean isApi) throws IOException {
        if (isApi) {
            sendApiError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Access forbidden: Insufficient privileges for your role", "FORBIDDEN");
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access forbidden: Insufficient privileges for your role");
        }
    }

    private void sendApiError(HttpServletResponse response, int status, String message, String errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiError = ApiResponse.error(message, errorCode);
        response.getWriter().write(JsonUtil.toJson(apiError));
    }

    @Override
    public void destroy() {
    }
}
