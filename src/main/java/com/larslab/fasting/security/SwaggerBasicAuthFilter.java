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
import java.net.URLEncoder;
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
        boolean is2faPath = matcher.match("/swagger-2fa", path);
        // Skip filtering if not swagger path or if disabled
        return !(isSwaggerPath || is2faPath) || !enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // HTML 2FA flow endpoint
        if (twoFactorEnabled && "/swagger-2fa".equals(path)) {
            String user = getBasicUserIfValid(request);
            if (user == null) {
                // Ask for Basic first
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "Basic realm=Swagger");
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Authentication required");
                return;
            }

            String returnTo = sanitizeReturnTo(request.getParameter("returnTo"));

            if ("GET".equalsIgnoreCase(request.getMethod())) {
                if (hasValid2faSessionCookie(request, user)) {
                    response.sendRedirect(returnTo);
                    return;
                }
                render2faForm(response, returnTo, null);
                return;
            }
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                String code = request.getParameter("code");
                if (code == null || code.isBlank()) {
                    render2faForm(response, returnTo, "Bitte Code eingeben.");
                    return;
                }
                boolean ok = verifySecondFactorCode(code);
                if (ok) {
                    issue2faSessionCookie(response, user);
                    response.sendRedirect(returnTo);
                } else {
                    render2faForm(response, returnTo, "Ungültiger Code. Bitte erneut versuchen.");
                }
                return;
            }
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        if (isAuthorized(request, response)) {
            filterChain.doFilter(request, response);
            return;
        }

        // If Basic is correct but 2FA is missing, offer HTML flow for browsers
        if (twoFactorEnabled) {
            String user = getBasicUserIfValid(request);
            if (user != null && !hasValid2faSessionCookie(request, user)) {
                String accept = request.getHeader("Accept");
                boolean wantsHtml = accept != null && accept.contains("text/html");
                if (wantsHtml) {
                    String returnTo = sanitizeReturnTo(originalUrl(request));
                    String loc = "/swagger-2fa?returnTo=" + URLEncoder.encode(returnTo, StandardCharsets.UTF_8);
                    response.sendRedirect(loc);
                    return;
                }
                // Hint for API clients
                response.setHeader("X-Require-2FA", "true");
            }
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

    private String getBasicUserIfValid(HttpServletRequest request) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) return null;
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Basic ")) return null;
        try {
            String base64Credentials = header.substring(6);
            byte[] decoded = Base64.getDecoder().decode(base64Credentials);
            String pair = new String(decoded, StandardCharsets.UTF_8);
            int idx = pair.indexOf(':');
            if (idx < 0) return null;
            String user = pair.substring(0, idx);
            String pass = pair.substring(idx + 1);
            return (username.equals(user) && password.equals(pass)) ? user : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String sanitizeReturnTo(String rt) {
        if (rt == null || rt.isBlank()) return "/swagger-ui/index.html";
        // Prevent open redirects: allow only swagger paths
        if (rt.startsWith("/swagger-ui") || rt.startsWith("/v3/api-docs") || "/swagger-ui.html".equals(rt)) {
            return rt;
        }
        return "/swagger-ui/index.html";
    }

    private String originalUrl(HttpServletRequest request) {
        String qs = request.getQueryString();
        return request.getRequestURI() + (qs != null ? ("?" + qs) : "");
    }

        private void render2faForm(HttpServletResponse response, String returnTo, String errorMsg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        String safeReturn = returnTo; // already sanitized
                String errorHtml = (errorMsg == null) ? "" : ("<p style='color:#b00020'>" + escapeHtml(errorMsg) + "</p>");
                String html = """
                                <!doctype html>
                                <html lang="de">
                                <head>
                                    <meta charset="utf-8">
                                    <meta name="viewport" content="width=device-width, initial-scale=1">
                                    <title>Swagger 2FA</title>
                                    <style>
                                        body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Ubuntu,Cantarell,Noto Sans,sans-serif;margin:2rem;}
                                        .card{max-width:420px;padding:1.2rem;border:1px solid #e5e7eb;border-radius:8px;}
                                        h1{font-size:1.25rem;margin:0 0 .75rem}
                                        input{padding:.65rem .75rem;margin:.25rem 0;width:100%;box-sizing:border-box;border:1px solid #d1d5db;border-radius:6px}
                                        button{margin-top:.5rem;padding:.6rem .9rem;border:0;border-radius:6px;background:#111827;color:#fff;cursor:pointer}
                                        button:hover{background:#0b1220}
                                        .hint{color:#6b7280;font-size:.9rem}
                                    </style>
                                </head>
                                <body>
                                    <div class="card">
                                        <h1>Zwei-Faktor-Verifizierung</h1>
                                        <p class="hint">Bitte gib den 6-stelligen Code aus deiner Authenticator‑App ein.</p>
                                        %s
                                        <form method="post" action="/swagger-2fa">
                                            <input type="text" name="code" pattern="\\d{6}" inputmode="numeric" autocomplete="one-time-code" placeholder="123456" required>
                                            <input type="hidden" name="returnTo" value="%s">
                                            <button type="submit">Bestätigen</button>
                                        </form>
                                    </div>
                                </body>
                                </html>
                                """.formatted(errorHtml, escapeHtmlAttr(safeReturn));
        response.getWriter().write(html);
    }

    private boolean verifySecondFactorCode(String code) {
        if (totpSecret != null && !totpSecret.isBlank()) {
            try {
                String normalizedSecret = ensureBase32(totpSecret.trim().replace(" ", "").toUpperCase());
                Totp totp = new Totp(normalizedSecret);
                return totp.verify(code);
            } catch (Exception e) {
                log.warn("TOTP verification failed due to exception: {}", e.toString());
                return false;
            }
        }
        return twoFactorCode != null && !twoFactorCode.isBlank() && twoFactorCode.equals(code);
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    private static String escapeHtmlAttr(String s) {
        return escapeHtml(s).replace("\"", "&quot;");
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
