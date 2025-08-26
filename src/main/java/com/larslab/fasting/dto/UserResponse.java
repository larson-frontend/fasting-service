package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.larslab.fasting.model.User;
import com.larslab.fasting.model.UserPreferences;
import com.larslab.fasting.config.FeatureFlags;
import java.time.Instant;

@Schema(description = "User information response")
public class UserResponse {
    
    @Schema(description = "User ID", example = "1")
    private String id;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "Email address", example = "john@example.com")
    private String email;
    
    @Schema(description = "Account creation timestamp")
    private Instant createdAt;
    
    @Schema(description = "Last login timestamp")
    private Instant lastLoginAt;
    
    @Schema(description = "User preferences")
    private UserPreferencesResponse preferences;
    
    public UserResponse() {}
    
    public UserResponse(User user) {
        this.id = user.getId().toString();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
        this.preferences = new UserPreferencesResponse(user.getPreferences());
    }
    
    /**
     * Feature-aware constructor that respects feature flags
     */
    public UserResponse(User user, FeatureFlags featureFlags) {
        this.id = user.getId().toString();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
        this.preferences = new UserPreferencesResponse(user.getPreferences(), featureFlags);
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public UserPreferencesResponse getPreferences() { return preferences; }
    public void setPreferences(UserPreferencesResponse preferences) { this.preferences = preferences; }
}
