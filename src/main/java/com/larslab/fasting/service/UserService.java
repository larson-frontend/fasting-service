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
     * Login or create user - idempotent operation
     */
    public User loginOrCreateUser(LoginOrCreateRequest request) {
        // First try to find by username
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update email if provided and different
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                user.setEmail(request.getEmail());
            }
            user.updateLastLogin();
            return userRepository.save(user);
        }
        
        // If not found by username, check if email already exists
        if (request.getEmail() != null) {
            Optional<User> userByEmail = userRepository.findByEmail(request.getEmail());
            if (userByEmail.isPresent()) {
                throw new IllegalArgumentException("Email already exists with different username");
            }
        }
        
        // Create new user
        User newUser = new User(request.getUsername(), request.getEmail());
        return userRepository.save(newUser);
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
        
        // Update notifications if provided
        if (request.getNotifications() != null) {
            UpdatePreferencesRequest.NotificationPreferencesRequest notifRequest = request.getNotifications();
            UserPreferences.NotificationPreferences notifications = preferences.getNotifications();
            
            if (notifRequest.getFastingReminders() != null) {
                notifications.setFastingReminders(notifRequest.getFastingReminders());
            }
            if (notifRequest.getMealReminders() != null) {
                notifications.setMealReminders(notifRequest.getMealReminders());
            }
            if (notifRequest.getProgressUpdates() != null) {
                notifications.setProgressUpdates(notifRequest.getProgressUpdates());
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
