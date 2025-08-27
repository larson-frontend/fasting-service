package com.larslab.fasting.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${rate.limit.capacity:100}")
    private int capacity;

    @Value("${rate.limit.window.ms:60000}")
    private long windowMs;

    private static class Counter {
        AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart;
    }

    private final Map<String, Counter> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = key(request);
        Counter c = buckets.computeIfAbsent(key, k -> {
            Counter nc = new Counter();
            nc.windowStart = Instant.now().toEpochMilli();
            return nc;
        });
        long now = Instant.now().toEpochMilli();
        if (now - c.windowStart > windowMs) {
            c.windowStart = now;
            c.count.set(0);
        }
        int current = c.count.incrementAndGet();
        if (current > capacity) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limited\",\"retryAfterMs\":" + (windowMs - (now - c.windowStart)) + "}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String key(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) ip = ip.split(",")[0].trim();
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        // Include coarse endpoint path to avoid one noisy route starving others
        String path = request.getRequestURI();
        return ip + '|' + path;
    }
}
