package com.djmart.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Filter that enforces UTF-8 character encoding on all incoming requests and outgoing responses.
 */
@WebFilter(filterName = "EncodingFilter", urlPatterns = "/*")
public class EncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(ENCODING);
        response.setCharacterEncoding(ENCODING);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
