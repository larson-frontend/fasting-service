package com.larslab.fasting.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Protects Swagger UI and OpenAPI endpoints with simple HTTP Basic credentials from env/properties.
 * Paths protected: /swagger-ui/**, /v3/api-docs/**, /swagger-ui.html
 *
 * Enable by providing SWAGGER_BASIC_USER and SWAGGER_BASIC_PASS env variables (or Spring properties).
 */
@Component
public class SwaggerBasicAuthFilter extends OncePerRequestFilter {

    private static final AntPathMatcher matcher = new AntPathMatcher();
    private static final Logger log = LoggerFactory.getLogger(SwaggerBasicAuthFilter.class);

    private final String username;
    private final String password;
    private final boolean enabled;

    public SwaggerBasicAuthFilter(
            @Value("${SWAGGER_BASIC_USER:}") String username,
            @Value("${SWAGGER_BASIC_PASS:}") String password,
            @Value("${SWAGGER_BASIC_ENABLED:true}") boolean enabled
    ) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        if (enabled) {
            if (username == null || username.isBlank()) {
                log.info("Swagger BasicAuth enabled but username is not set");
            } else {
                log.info("Swagger BasicAuth enabled for user '{}'", username);
            }
        } else {
            log.info("Swagger BasicAuth disabled");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Filter only Swagger/OpenAPI paths
        boolean isSwaggerPath = matcher.match("/swagger-ui/**", path)
                || matcher.match("/v3/api-docs/**", path)
                || matcher.match("/swagger-ui.html", path);
        // Skip filtering if not swagger path or if disabled
        return !isSwaggerPath || !enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isAuthorized(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Basic realm=Swagger");
        response.getWriter().write("Unauthorized");
    }

    private boolean isAuthorized(HttpServletRequest request) {
        // if no creds configured, deny by default when enabled
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Basic ")) {
            return false;
        }
        try {
            String base64Credentials = header.substring(6);
            byte[] decoded = Base64.getDecoder().decode(base64Credentials);
            String pair = new String(decoded, StandardCharsets.UTF_8);
            int idx = pair.indexOf(':');
            if (idx < 0) return false;
            String user = pair.substring(0, idx);
            String pass = pair.substring(idx + 1);
            return username.equals(user) && password.equals(pass);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
