package com.larslab.fasting.dto;

public record TokenRefreshResponse(String accessToken, String refreshToken, String tokenType, long expiresInMs) {
    public TokenRefreshResponse(String accessToken, String refreshToken, long expiresInMs) {
        this(accessToken, refreshToken, "Bearer", expiresInMs);
    }
}
