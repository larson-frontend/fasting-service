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
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;

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
    private final boolean twoFactorEnabled;
    private final String twoFactorCode; // static header-code fallback
    private final String totpSecret;    // Base32 TOTP secret

    public SwaggerBasicAuthFilter(
            @Value("${SWAGGER_BASIC_USER:}") String username,
            @Value("${SWAGGER_BASIC_PASS:}") String password,
            @Value("${SWAGGER_BASIC_ENABLED:true}") boolean enabled,
            @Value("${SWAGGER_2FA_ENABLED:false}") boolean twoFactorEnabled,
            @Value("${SWAGGER_2FA_CODE:}") String twoFactorCode,
            @Value("${SWAGGER_TOTP_SECRET:}") String totpSecret
    ) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.twoFactorEnabled = twoFactorEnabled;
        this.twoFactorCode = twoFactorCode;
        this.totpSecret = totpSecret;
        if (enabled) {
            if (username == null || username.isBlank()) {
                log.info("Swagger BasicAuth enabled but username is not set");
            } else {
                log.info("Swagger BasicAuth enabled for user '{}'", username);
            }
            if (twoFactorEnabled) {
                if ((totpSecret == null || totpSecret.isBlank()) && (twoFactorCode == null || twoFactorCode.isBlank())) {
                    log.warn("Swagger 2FA enabled but neither SWAGGER_TOTP_SECRET nor SWAGGER_2FA_CODE is set; access will be denied until one is configured");
                } else if (totpSecret != null && !totpSecret.isBlank()) {
                    log.info("Swagger 2FA enabled (TOTP required via X-2FA header; Google Authenticator compatible)");
                } else {
                    log.info("Swagger 2FA enabled (static header code via X-2FA)");
                }
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
        response.getWriter().write(twoFactorEnabled ? "Unauthorized: 2FA required" : "Unauthorized");
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
            boolean basicOk = username.equals(user) && password.equals(pass);
            if (!basicOk) return false;

            if (!twoFactorEnabled) return true;
            // header-based second factor; prefer X-2FA, allow X-SWAGGER-2FA as alias
            String code = request.getHeader("X-2FA");
            if (code == null || code.isBlank()) {
                code = request.getHeader("X-SWAGGER-2FA");
            }
            if (code == null || code.isBlank()) return false;

            // If TOTP secret present, validate as TOTP; else fallback to static code
                } catch (Exception e) {
                    log.warn("TOTP verification failed due to exception: {}", e.toString());
                    return false;
                }
            }
            // Static fallback
            if (twoFactorCode == null || twoFactorCode.isBlank()) return false;
            return twoFactorCode.equals(code);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String ensureBase32(String raw) {
        try {
            // If it decodes fine, assume it's already Base32
            Base32.decode(raw);
            return raw;
        } catch (Exception ex) {
            // Otherwise, encode ASCII bytes to Base32
            return Base32.encode(raw.getBytes(StandardCharsets.US_ASCII));
        }
    }
}
