package com.larslab.fasting.service;

import com.larslab.fasting.model.RefreshToken;
import com.larslab.fasting.model.User;
import com.larslab.fasting.repo.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${refresh.jwt.expiration:1209600000}") // 14 days default
    private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public String createToken(User user, String userAgent, String ip) {
        String rawToken = UUID.randomUUID().toString() + ":" + UUID.randomUUID();
        String hash = hash(rawToken);
        String uaHash = userAgent != null ? hash(userAgent) : null;
        String ipHash = ip != null ? hash(ipPrefix(ip)) : null;
        RefreshToken entity = new RefreshToken(user, hash, Instant.now().plusMillis(refreshExpirationMs), uaHash, ipHash);
        repository.save(entity);
        return rawToken; // return raw (client stores), server only keeps hash
    }

    public Optional<RefreshToken> validate(String rawToken, String userAgent, String ip) {
        String hash = hash(rawToken);
        Optional<RefreshToken> tokenOpt = repository.findByTokenHash(hash);
        if (tokenOpt.isEmpty()) return Optional.empty();
        RefreshToken token = tokenOpt.get();
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) return Optional.empty();
        if (token.getUserAgentHash() != null && userAgent != null && !token.getUserAgentHash().equals(hash(userAgent))) return Optional.empty();
        if (token.getIpHash() != null && ip != null && !token.getIpHash().equals(hash(ipPrefix(ip)))) return Optional.empty();
        return Optional.of(token);
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        repository.save(token);
    }

    public void rotate(RefreshToken oldToken, String userAgent, String ip) {
        revoke(oldToken);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String ipPrefix(String ip) {
        int idx = ip.lastIndexOf('.');
        return idx > 0 ? ip.substring(0, idx) : ip;
    }
}
