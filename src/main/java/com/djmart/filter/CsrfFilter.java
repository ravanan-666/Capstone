package com.djmart.filter;

import com.djmart.dto.ApiResponse;
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
import java.security.MessageDigest;

/**
 * Filter providing Cross-Site Request Forgery (CSRF) defense for state-changing HTTP requests.
 * Uses the Synchronizer Token Pattern with constant-time equality checking.
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);

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

        // 1. Ensure a CSRF token exists for the session
        HttpSession session = httpRequest.getSession(true);
        String sessionToken = (String) session.getAttribute(SecurityUtil.SESSION_CSRF_TOKEN);
        if (sessionToken == null) {
            sessionToken = SecurityUtil.generateSecureToken();
            session.setAttribute(SecurityUtil.SESSION_CSRF_TOKEN, sessionToken);
        }

        // Make token available to JSPs and AJAX clients
        httpRequest.setAttribute(SecurityUtil.SESSION_CSRF_TOKEN, sessionToken);
        httpResponse.setHeader(SecurityUtil.CSRF_HEADER, sessionToken);

        String method = httpRequest.getMethod().toUpperCase();

        // 2. Safe idempotent methods require no CSRF validation
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method) || "TRACE".equals(method)) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Check for exempt public paths (e.g. initial login / registration API before session establishment)
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if (isExemptPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 4. Validate submitted CSRF token from header or form parameter
        String clientToken = httpRequest.getHeader(SecurityUtil.CSRF_HEADER);
        if (clientToken == null || clientToken.trim().isEmpty()) {
            clientToken = httpRequest.getParameter(SecurityUtil.CSRF_PARAM);
        }

        if (clientToken == null || !constantTimeEquals(sessionToken, clientToken)) {
            LOGGER.warn("CSRF token validation failed for {} {} from IP {}",
                    method,
                    SecurityUtil.sanitizeForLog(httpRequest.getRequestURI()),
                    httpRequest.getRemoteAddr());

            boolean isApi = path.startsWith("/api/") ||
                    (httpRequest.getHeader("Accept") != null && httpRequest.getHeader("Accept").contains("application/json"));

            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (isApi) {
                httpResponse.setContentType("application/json;charset=UTF-8");
                ApiResponse<Void> errorResp = ApiResponse.error("Invalid or missing CSRF token", "CSRF_ERROR");
                httpResponse.getWriter().write(JsonUtil.toJson(errorResp));
            } else {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isExemptPath(String path) {
        return path.equals("/api/v1/auth/login") ||
               path.equals("/api/v1/auth/register") ||
               path.equals("/auth/login") ||
               path.equals("/auth/register");
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }

    @Override
    public void destroy() {
    }
}
