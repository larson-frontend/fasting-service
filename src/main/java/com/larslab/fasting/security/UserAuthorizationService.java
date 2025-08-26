package com.larslab.fasting.security;

import com.larslab.fasting.model.User;
import com.larslab.fasting.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAuthorizationService {

    private final UserService userService;

    public UserAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Check if the authenticated user matches the requested identifier
     * @param authenticatedUsername The username from the JWT token
     * @param requestedIdentifier The identifier from the URL path (username or email)
     * @return true if the user is authorized to access this identifier's data
     */
    public boolean userMatches(String authenticatedUsername, String requestedIdentifier) {
        // First, get the authenticated user
        Optional<User> authenticatedUser = userService.getUserByIdentifier(authenticatedUsername);
        if (authenticatedUser.isEmpty()) {
            return false;
        }
        
        User user = authenticatedUser.get();
        
        // Check if the requested identifier matches either username or email
        return user.getUsername().equals(requestedIdentifier) || 
               user.getEmail().equals(requestedIdentifier);
    }

    /**
     * Extract JWT token from Authorization header
     * @param authHeader The Authorization header value
     * @return JWT token or null if invalid format
     */
    public String extractJwtFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
