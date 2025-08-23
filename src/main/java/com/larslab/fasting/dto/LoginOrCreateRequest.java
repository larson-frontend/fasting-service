package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Schema(description = "Request for user login or creation")
public class LoginOrCreateRequest {
    
    @NotBlank(message = "Username is required")
    @Schema(description = "Username for the user", example = "john_doe", required = true)
    private String username;
    
    @Email(message = "Email must be valid")
    @Schema(description = "Email address (optional)", example = "john@example.com")
    private String email;
    
    public LoginOrCreateRequest() {}
    
    public LoginOrCreateRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
