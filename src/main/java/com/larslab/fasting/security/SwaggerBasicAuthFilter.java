package com.larslab.fasting.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.Duration;

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
    private final int sessionMinutes;   // minutes for cookie session
    private final byte[] sessionKey;    // HMAC key for cookie signing

    public SwaggerBasicAuthFilter(
            @Value("${SWAGGER_BASIC_USER:}") String username,
            @Value("${SWAGGER_BASIC_PASS:}") String password,
            @Value("${SWAGGER_BASIC_ENABLED:true}") boolean enabled,
            @Value("${SWAGGER_2FA_ENABLED:false}") boolean twoFactorEnabled,
            @Value("${SWAGGER_2FA_CODE:}") String twoFactorCode,
            @Value("${SWAGGER_TOTP_SECRET:}") String totpSecret,
            @Value("${SWAGGER_2FA_SESSION_MINUTES:15}") int sessionMinutes,
            @Value("${JWT_SECRET:}") String jwtSecret
    ) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.twoFactorEnabled = twoFactorEnabled;
        this.twoFactorCode = twoFactorCode;
        this.totpSecret = totpSecret;
        this.sessionMinutes = sessionMinutes > 0 ? sessionMinutes : 15;
        // Prefer JWT_SECRET for signing; if empty, generate ephemeral key (valid until restart)
        byte[] key;
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            key = jwtSecret.getBytes(StandardCharsets.UTF_8);
        } else {
            key = ("ephemeral-" + System.identityHashCode(this) + "-" + Instant.now().toEpochMilli())
                    .getBytes(StandardCharsets.UTF_8);
            log.warn("No JWT_SECRET available; using ephemeral key for 2FA session cookie (valid only until restart)");
        }
        this.sessionKey = key;
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
                    log.info("Swagger 2FA enabled (TOTP via X-2FA). Session cookie enabled for {} minutes", this.sessionMinutes);
                } else {
                    log.info("Swagger 2FA enabled (static header code via X-2FA). Session cookie enabled for {} minutes", this.sessionMinutes);
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
        if (isAuthorized(request, response)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Basic realm=Swagger");
        response.getWriter().write(twoFactorEnabled ? "Unauthorized: 2FA required" : "Unauthorized");
    }

    private boolean isAuthorized(HttpServletRequest request, HttpServletResponse response) {
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

            // 1) Cookie session shortcut
            if (hasValid2faSessionCookie(request, user)) {
                return true;
            }
            // header-based second factor; prefer X-2FA, allow X-SWAGGER-2FA as alias
            String code = request.getHeader("X-2FA");
            if (code == null || code.isBlank()) {
                code = request.getHeader("X-SWAGGER-2FA");
            }
            if (code == null || code.isBlank()) return false;

            // If TOTP secret present, validate as TOTP; else fallback to static code
            if (totpSecret != null && !totpSecret.isBlank()) {
                try {
                    String normalizedSecret = ensureBase32(totpSecret.trim().replace(" ", "").toUpperCase());
                    Totp totp = new Totp(normalizedSecret);
                    if (totp.verify(code)) {
                        // Issue session cookie
                        issue2faSessionCookie(response, user);
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    log.warn("TOTP verification failed due to exception: {}", e.toString());
                    return false;
                }
            }
            // Static fallback
            if (twoFactorCode == null || twoFactorCode.isBlank()) return false;
            boolean ok = twoFactorCode.equals(code);
            if (ok) {
                issue2faSessionCookie(response, user);
            }
            return ok;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean hasValid2faSessionCookie(HttpServletRequest request, String user) {
        if (request.getCookies() == null) return false;
        for (Cookie c : request.getCookies()) {
            if ("SWAGGER_2FA".equals(c.getName())) {
                String value = c.getValue();
                try {
                    String[] parts = value.split("\\.");
                    if (parts.length != 2) return false;
                    long exp = Long.parseLong(parts[0]);
                    if (Instant.now().getEpochSecond() > exp) return false;
                    String expectedSig = hmacHex(user + "|" + exp, sessionKey);
                    return constantTimeEquals(parts[1], expectedSig);
                } catch (Exception ex) {
                    return false;
                }
            }
        }
        return false;
    }

    private void issue2faSessionCookie(HttpServletResponse response, String user) {
        long exp = Instant.now().plus(Duration.ofMinutes(sessionMinutes)).getEpochSecond();
        String sig = hmacHex(user + "|" + exp, sessionKey);
        String token = exp + "." + sig;
        Cookie cookie = new Cookie("SWAGGER_2FA", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(sessionMinutes * 60);
        response.addCookie(cookie);
        // Add SameSite=Strict manually if needed by some proxies
        response.addHeader("Set-Cookie", String.format("SWAGGER_2FA=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=Lax", token, sessionMinutes * 60));
    }

    private static String hmacHex(String data, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] h = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int res = 0;
        for (int i = 0; i < a.length(); i++) {
            res |= a.charAt(i) ^ b.charAt(i);
        }
        return res == 0;
    }

    private static String ensureBase32(String raw) {
        try {
            // If it decodes fine, check if decoded length is valid for TOTP (16-20 bytes)
            byte[] decoded = Base32.decode(raw);
            if (decoded.length >= 16 && decoded.length <= 20) {
                return raw;
            }
        } catch (Exception ex) {
            // Ignore and fallback to encoding below
        }
        // Otherwise, encode ASCII bytes to Base32
        return Base32.encode(raw.getBytes(StandardCharsets.US_ASCII));
    }
}
