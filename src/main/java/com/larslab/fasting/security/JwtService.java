package com.larslab.fasting.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    
    // Intentionally no secure default; must be overridden via environment.
    @Value("${jwt.secret:change-me-in-prod}")
    private String secretKey;
    
    @Value("${jwt.expiration:900000}") // default 15m for access tokens
    private Long accessExpiration;

    @Value("${refresh.jwt.expiration:1209600000}") // 14 days
    private Long refreshExpiration;
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public String generateAccessToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + accessExpiration))
            .signWith(getSignInKey())
            .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(getSignInKey())
            .compact();
    }
    
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }
    
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @PostConstruct
    void validateSecret() {
        int length = secretKey == null ? -1 : secretKey.length();
        if (secretKey == null) {
            log.error("JWT secret missing (null)");
            throw new IllegalStateException("JWT secret must be provided via JWT_SECRET env var and be at least 32 characters");
        }
        if (secretKey.equals("change-me-in-prod")) {
            log.error("JWT secret is still default placeholder (length={})", length);
            throw new IllegalStateException("JWT secret must be provided via JWT_SECRET env var and be at least 32 characters");
        }
        if (length < 32) {
            log.error("JWT secret too short (length={})", length);
            throw new IllegalStateException("JWT secret must be provided via JWT_SECRET env var and be at least 32 characters");
        }
        log.info("JWT secret accepted (length={})", length);
    }
}
