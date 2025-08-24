package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Schema(description = "Request for user login or creation")
public class LoginOrCreateRequest {
    
    @NotBlank(message = "Username/identifier is required")
    @Schema(description = "Username or email identifier (can be either username or email address)", 
            example = "john_doe or john@example.com", required = true)
    private String username;
    
    @Email(message = "Email must be valid")
    @Schema(description = "Email address (optional - only needed if username field is not an email)", 
            example = "john@example.com", required = false)
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
