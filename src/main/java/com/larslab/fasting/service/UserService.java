package com.larslab.fasting.service;

import com.larslab.fasting.model.User;
import com.larslab.fasting.model.UserPreferences;
import com.larslab.fasting.repo.UserRepository;
import com.larslab.fasting.dto.LoginOrCreateRequest;
import com.larslab.fasting.dto.UpdatePreferencesRequest;
import com.larslab.fasting.dto.UpdateLanguageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Login or create user - handles single field that can be username or email
     */
    public User loginOrCreateUser(LoginOrCreateRequest request) {
        // Validate that identifier is provided
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username/email identifier is required");
        }
        
        String identifier = request.getUsername().trim();
        boolean isEmail = isValidEmail(identifier);
        
        String actualUsername;
        String actualEmail;
        
        if (isEmail) {
            // Identifier is an email
            actualEmail = identifier.toLowerCase();
            actualUsername = extractUsernameFromEmail(actualEmail);
        } else {
            // Identifier is a username
            actualUsername = identifier;
            
            // Use provided email or auto-generate
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                actualEmail = request.getEmail().trim().toLowerCase();
            } else {
                actualEmail = actualUsername + "@fasting.app";
            }
        }
        
        // Try to find existing user by username OR email
        Optional<User> existingUser = getUserByIdentifier(identifier);
        
        if (existingUser.isPresent()) {
            // User exists - login
            User user = existingUser.get();
            user.updateLastLogin();
            return userRepository.save(user);
        }
        
        // Check if generated/provided username already exists
        if (!isEmail && userRepository.existsByUsername(actualUsername)) {
            throw new IllegalArgumentException("Username '" + actualUsername + "' is already taken");
        }
        
        // Check if generated/provided email already exists
        if (userRepository.existsByEmail(actualEmail)) {
            throw new IllegalArgumentException("Email '" + actualEmail + "' is already registered");
        }
        
        // Create new user
        User newUser = new User(actualUsername, actualEmail);
        return userRepository.save(newUser);
    }
    
    /**
     * Check if string is a valid email format
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && 
               email.indexOf("@") < email.lastIndexOf(".");
    }
    
    /**
     * Extract username from email (part before @)
     */
    private String extractUsernameFromEmail(String email) {
        return email.substring(0, email.indexOf("@"));
    }
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get user by identifier (username or email)
     */
    public Optional<User> getUserByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String cleanIdentifier = identifier.trim();
        
        // Try to find by username first
        Optional<User> userByUsername = userRepository.findByUsername(cleanIdentifier);
        if (userByUsername.isPresent()) {
            return userByUsername;
        }
        
        // If not found by username, try by email
        return userRepository.findByEmail(cleanIdentifier.toLowerCase());
    }
    
    /**
     * Check if username or email already exists
     */
    public boolean isUsernameOrEmailTaken(String username, String email) {
        return userRepository.existsByUsername(username) || userRepository.existsByEmail(email);
    }
    
    /**
     * Update user preferences
     */
    public User updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserPreferences preferences = user.getPreferences();
        
        // Update language if provided
        if (request.getLanguage() != null) {
            preferences.setLanguage(UserPreferences.Language.fromCode(request.getLanguage()));
        }
        
        // Update theme if provided
        if (request.getTheme() != null) {
            preferences.setTheme(UserPreferences.Theme.fromValue(request.getTheme()));
        }
        
        // Update timezone if provided
        if (request.getTimezone() != null) {
            preferences.setTimezone(request.getTimezone());
        }
        
        // Update notifications if provided
        if (request.getNotifications() != null) {
            UpdatePreferencesRequest.NotificationPreferencesRequest notifRequest = request.getNotifications();
            UserPreferences.NotificationPreferences notifications = preferences.getNotifications();
            
            if (notifRequest.getEnabled() != null) {
                notifications.setEnabled(notifRequest.getEnabled());
            }
            if (notifRequest.getFastingReminders() != null) {
                notifications.setFastingReminders(notifRequest.getFastingReminders());
            }
            if (notifRequest.getMealReminders() != null) {
                notifications.setMealReminders(notifRequest.getMealReminders());
            }
            if (notifRequest.getProgressUpdates() != null) {
                notifications.setProgressUpdates(notifRequest.getProgressUpdates());
            }
            if (notifRequest.getGoalAchievements() != null) {
                notifications.setGoalAchievements(notifRequest.getGoalAchievements());
            }
            if (notifRequest.getWeeklyReports() != null) {
                notifications.setWeeklyReports(notifRequest.getWeeklyReports());
            }
        }
        
        // Update fasting defaults if provided
        if (request.getFastingDefaults() != null) {
            UpdatePreferencesRequest.FastingDefaultsRequest fastingRequest = request.getFastingDefaults();
            UserPreferences.FastingDefaults fastingDefaults = preferences.getFastingDefaults();
            
            if (fastingRequest.getDefaultGoalHours() != null) {
                fastingDefaults.setDefaultGoalHours(fastingRequest.getDefaultGoalHours());
            }
            if (fastingRequest.getPreferredFastingType() != null) {
                fastingDefaults.setPreferredFastingType(fastingRequest.getPreferredFastingType());
            }
            if (fastingRequest.getAutoStartNextFast() != null) {
                fastingDefaults.setAutoStartNextFast(fastingRequest.getAutoStartNextFast());
            }
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Update user language (quick update)
     */
    public User updateLanguage(Long userId, UpdateLanguageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.getPreferences().setLanguage(UserPreferences.Language.fromCode(request.getLanguage()));
        
        return userRepository.save(user);
    }
}
