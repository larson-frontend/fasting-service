package com.larslab.fasting.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Ensures every request has a correlation ID for log tracing.
 * Header: X-Request-ID (incoming respected, otherwise generated)
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String HEADER_NAME = "X-Request-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String cid = extractOrGenerate(request.getHeader(HEADER_NAME));
        MDC.put(MDC_KEY, cid);
        try {
            response.setHeader(HEADER_NAME, cid);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String extractOrGenerate(String headerVal) {
        if (headerVal != null && headerVal.matches("[A-Za-z0-9-]{8,128}")) {
            return headerVal;
        }
        return UUID.randomUUID().toString();
    }
}
