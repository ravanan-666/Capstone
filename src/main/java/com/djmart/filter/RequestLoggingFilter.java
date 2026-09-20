package com.djmart.filter;

import com.djmart.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter generating a unique request ID for each HTTP request.
 * Populates SLF4J MDC so all log events throughout the request lifecycle include the request ID.
 */
@WebFilter(filterName = "RequestLoggingFilter", urlPatterns = "/*")
public class RequestLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String requestId = httpRequest.getHeader("X-Request-ID");
            if (requestId == null || requestId.trim().isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            MDC.put(SecurityUtil.MDC_REQUEST_ID, requestId);
            httpResponse.setHeader("X-Request-ID", requestId);

            long startTime = System.currentTimeMillis();
            try {
                chain.doFilter(request, response);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.debug("{} {} finished in {} ms with status {}",
                        httpRequest.getMethod(),
                        SecurityUtil.sanitizeForLog(httpRequest.getRequestURI()),
                        duration,
                        httpResponse.getStatus());
                MDC.remove(SecurityUtil.MDC_REQUEST_ID);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
