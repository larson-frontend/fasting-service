package com.larslab.fasting.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
@Schema(description = "User entity with authentication and preferences")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique user ID", example = "1")
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Schema(description = "Unique username", example = "john_doe")
    private String username;
    
    @Column(unique = true)
    @Schema(description = "User email address", example = "john@example.com")
    private String email;
    
    @Schema(description = "User creation timestamp")
    private Instant createdAt;
    
    @Schema(description = "Last login timestamp")
    private Instant lastLoginAt;
    
    @Embedded
    @Schema(description = "User preferences")
    private UserPreferences preferences;
    
    public User() {
        this.createdAt = Instant.now();
        this.lastLoginAt = Instant.now();
        this.preferences = new UserPreferences();
    }
    
    public User(String username, String email) {
        this();
        this.username = username;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public UserPreferences getPreferences() { return preferences; }
    public void setPreferences(UserPreferences preferences) { this.preferences = preferences; }
    
    public void updateLastLogin() {
        this.lastLoginAt = Instant.now();
    }
}
