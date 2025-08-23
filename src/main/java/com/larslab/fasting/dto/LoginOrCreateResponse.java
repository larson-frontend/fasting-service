package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.larslab.fasting.model.User;

@Schema(description = "Response for login or create user request")
public class LoginOrCreateResponse {
    
    @Schema(description = "User information")
    private UserResponse user;
    
    @Schema(description = "Authentication token (optional for future use)")
    private String token;
    
    public LoginOrCreateResponse() {}
    
    public LoginOrCreateResponse(User user) {
        this.user = new UserResponse(user);
        this.token = null; // For future JWT implementation
    }
    
    public LoginOrCreateResponse(User user, String token) {
        this.user = new UserResponse(user);
        this.token = token;
    }
    
    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
