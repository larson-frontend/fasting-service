package com.larslab.fasting.controller;

import com.larslab.fasting.model.User;
import com.larslab.fasting.service.UserService;
import com.larslab.fasting.security.JwtService;
import com.larslab.fasting.config.FeatureFlags;
import com.larslab.fasting.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8000", "http://localhost:8080", "http://localhost:4200"})
@Tag(name = "User Management", description = "APIs for user authentication and preference management")
@Validated
public class UserController {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final FeatureFlags featureFlags;
    
    public UserController(UserService userService, JwtService jwtService, FeatureFlags featureFlags) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.featureFlags = featureFlags;
    }
    
    @PostMapping("/login-or-create")
    @Operation(summary = "Login or create user", 
               description = "Creates a new user if they don't exist, or logs in existing user. The 'username' field can contain either a username or email address. Returns a JWT token for authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully"),
            @ApiResponse(responseCode = "201", description = "New user created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<?> loginOrCreate(@Valid @RequestBody LoginOrCreateRequest request) {
        try {
            User user = userService.loginOrCreateUser(request);
            
            // Generate JWT token for the user
            String jwtToken = jwtService.generateToken(user.getUsername());
            
            LoginOrCreateResponse response = new LoginOrCreateResponse(user, jwtToken);
            
            // Return 201 for new users, 200 for existing users
            HttpStatus status = user.getCreatedAt().equals(user.getLastLoginAt()) ? 
                    HttpStatus.CREATED : HttpStatus.OK;
            
            return ResponseEntity.status(status).body(response);
        } catch (IllegalArgumentException e) {
            // Return specific error message for better frontend handling
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }
    
    @GetMapping("/find/{identifier}")
    @Operation(summary = "Find user by identifier", 
               description = "Find user by username or email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> findUserByIdentifier(@PathVariable String identifier) {
        Optional<User> user = userService.getUserByIdentifier(identifier);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(new UserResponse(user.get(), featureFlags));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/check-availability")
    @Operation(summary = "Check username/email availability", 
               description = "Check if username or email is already taken")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability checked")
    })
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (username != null && !username.trim().isEmpty()) {
            response.put("usernameAvailable", !userService.getUserByUsername(username.trim()).isPresent());
        }
        
        if (email != null && !email.trim().isEmpty()) {
            response.put("emailAvailable", !userService.getUserByEmail(email.trim().toLowerCase()).isPresent());
        }
        
        return ResponseEntity.ok(response);
    }
    
        @GetMapping("/current")
    @Operation(summary = "Get current user", 
               description = "Retrieves current user information and preferences")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getCurrentUser(@RequestParam(required = false, defaultValue = "1") String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(new UserResponse(user.get(), featureFlags));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/preferences")
    @Operation(summary = "Update user preferences", 
               description = "Updates user preferences like language, theme, and notification settings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updatePreferences(
            @RequestParam(required = false, defaultValue = "1") String userId,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        try {
            Long id = Long.parseLong(userId);
            User updatedUser = userService.updatePreferences(id, request);
            return ResponseEntity.ok(new UserResponse(updatedUser, featureFlags));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/language")
    @Operation(summary = "Update user language", 
               description = "Quick endpoint to update just the user's language preference")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Language updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid language code"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateLanguage(
            @RequestParam(required = false, defaultValue = "1") String userId,
            @Valid @RequestBody UpdateLanguageRequest request) {
        try {
            Long id = Long.parseLong(userId);
            User updatedUser = userService.updateLanguage(id, request);
            return ResponseEntity.ok(new UserResponse(updatedUser, featureFlags));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", e.getMessage());
        error.put("status", "400");
        return error;
    }
}
