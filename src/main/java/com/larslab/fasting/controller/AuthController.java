package com.larslab.fasting.controller;

import com.larslab.fasting.dto.TokenRefreshResponse;
import com.larslab.fasting.model.RefreshToken;
import com.larslab.fasting.model.User;
import com.larslab.fasting.security.JwtService;
import com.larslab.fasting.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> body, @RequestHeader(value = "User-Agent", required = false) String userAgent, @RequestHeader(value = "X-Forwarded-For", required = false) String xff, @RequestHeader(value = "X-Real-IP", required = false) String realIp) {
        String rawRefresh = body.get("refreshToken");
        if (rawRefresh == null || rawRefresh.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error("missing_refresh_token"));
        }
        String ip = firstNonNull(splitFirst(xff), realIp);
        Optional<RefreshToken> tokenOpt = refreshTokenService.validate(rawRefresh, userAgent, ip);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("invalid_refresh_token"));
        }
        RefreshToken token = tokenOpt.get();
        User user = token.getUser();
        // Rotate
        refreshTokenService.rotate(token, userAgent, ip);
        String newRefresh = refreshTokenService.createToken(user, userAgent, ip);
        String newAccess = jwtService.generateAccessToken(user.getUsername());
        return ResponseEntity.ok(new TokenRefreshResponse(newAccess, newRefresh, 900000));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String,String> body, @RequestHeader(value = "User-Agent", required = false) String userAgent, @RequestHeader(value = "X-Forwarded-For", required = false) String xff) {
        String rawRefresh = body.get("refreshToken");
        if (rawRefresh == null || rawRefresh.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error("missing_refresh_token"));
        }
        String ip = splitFirst(xff);
        return refreshTokenService.validate(rawRefresh, userAgent, ip)
            .map(rt -> {
                refreshTokenService.revoke(rt);
                return ResponseEntity.ok(Map.of("status","revoked"));
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("invalid_refresh_token")));
    }

    private Map<String,String> error(String code) {
        Map<String,String> m = new HashMap<>();
        m.put("error", code);
        return m;
    }

    private String splitFirst(String list) {
        if (list == null) return null;
        int idx = list.indexOf(',');
        return idx > 0 ? list.substring(0, idx).trim() : list.trim();
    }

    private String firstNonNull(String a, String b) { return a != null ? a : b; }
}
