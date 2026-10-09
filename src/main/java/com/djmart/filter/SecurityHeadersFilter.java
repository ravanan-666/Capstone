package com.djmart.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that applies standard OWASP security response headers to safeguard against
 * clickjacking, MIME sniffing, and cross-site scripting attacks.
 */
@WebFilter(filterName = "SecurityHeadersFilter", urlPatterns = "/*")
public class SecurityHeadersFilter implements Filter {

    private static final String CSP_POLICY =
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self';";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
            if (path.startsWith("/h2-console")) {
                httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
                chain.doFilter(request, response);
                return;
            }
        }
        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            httpResponse.setHeader("Content-Security-Policy", CSP_POLICY);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
