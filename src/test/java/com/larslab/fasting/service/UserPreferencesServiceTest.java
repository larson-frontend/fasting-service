package com.larslab.fasting.service;

import com.larslab.fasting.dto.UpdatePreferencesRequest;
import com.larslab.fasting.model.User;
import com.larslab.fasting.model.UserPreferences;
import com.larslab.fasting.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        UserPreferences preferences = new UserPreferences();
        preferences.setLanguage(UserPreferences.Language.EN);
        preferences.setTheme(UserPreferences.Theme.SYSTEM);
        preferences.setTimezone("UTC");
        
        UserPreferences.NotificationPreferences notifications = new UserPreferences.NotificationPreferences();
        notifications.setEnabled(true);
        notifications.setFastingReminders(true);
        notifications.setMealReminders(true);
        notifications.setProgressUpdates(false);
        notifications.setGoalAchievements(true);
        notifications.setWeeklyReports(false);
        preferences.setNotifications(notifications);
        
        UserPreferences.FastingDefaults fastingDefaults = new UserPreferences.FastingDefaults();
        fastingDefaults.setDefaultGoalHours(16);
        fastingDefaults.setPreferredFastingType("16:8");
        fastingDefaults.setAutoStartNextFast(false);
        preferences.setFastingDefaults(fastingDefaults);
        
        testUser.setPreferences(preferences);
    }

    @Test
    void updatePreferences_FullUpdate_UpdatesAllFields() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setLanguage("de");
        request.setTheme("dark");
        request.setTimezone("Europe/Berlin");
        
        UpdatePreferencesRequest.NotificationPreferencesRequest notifRequest = 
            new UpdatePreferencesRequest.NotificationPreferencesRequest();
        notifRequest.setEnabled(false);
        notifRequest.setFastingReminders(false);
        notifRequest.setMealReminders(false);
        notifRequest.setProgressUpdates(true);
        notifRequest.setGoalAchievements(false);
        notifRequest.setWeeklyReports(true);
        request.setNotifications(notifRequest);
        
        UpdatePreferencesRequest.FastingDefaultsRequest fastingRequest = 
            new UpdatePreferencesRequest.FastingDefaultsRequest();
        fastingRequest.setDefaultGoalHours(24);
        fastingRequest.setPreferredFastingType("24h");
        fastingRequest.setAutoStartNextFast(true);
        request.setFastingDefaults(fastingRequest);

        // Act
        User result = userService.updatePreferences(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(UserPreferences.Language.DE, result.getPreferences().getLanguage());
        assertEquals(UserPreferences.Theme.DARK, result.getPreferences().getTheme());
        assertEquals("Europe/Berlin", result.getPreferences().getTimezone());
        
        UserPreferences.NotificationPreferences notifications = result.getPreferences().getNotifications();
        assertFalse(notifications.getEnabled());
        assertFalse(notifications.getFastingReminders());
        assertFalse(notifications.getMealReminders());
        assertTrue(notifications.getProgressUpdates());
        assertFalse(notifications.getGoalAchievements());
        assertTrue(notifications.getWeeklyReports());
        
        UserPreferences.FastingDefaults fastingDefaults = result.getPreferences().getFastingDefaults();
        assertEquals(24, fastingDefaults.getDefaultGoalHours());
        assertEquals("24h", fastingDefaults.getPreferredFastingType());
        assertTrue(fastingDefaults.getAutoStartNextFast());

        verify(userRepository).save(testUser);
    }

    @Test
    void updatePreferences_PartialUpdate_OnlyLanguage() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setLanguage("de");

        // Act
        User result = userService.updatePreferences(1L, request);

        // Assert
        assertEquals(UserPreferences.Language.DE, result.getPreferences().getLanguage());
        // Other fields should remain unchanged
        assertEquals(UserPreferences.Theme.SYSTEM, result.getPreferences().getTheme());
        assertEquals("UTC", result.getPreferences().getTimezone());
        
        verify(userRepository).save(testUser);
    }

    @Test
    void updatePreferences_PartialUpdate_OnlyNotifications() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        UpdatePreferencesRequest.NotificationPreferencesRequest notifRequest = 
            new UpdatePreferencesRequest.NotificationPreferencesRequest();
        notifRequest.setFastingReminders(false);
        notifRequest.setWeeklyReports(true);
        request.setNotifications(notifRequest);

        // Act
        User result = userService.updatePreferences(1L, request);

        // Assert
        UserPreferences.NotificationPreferences notifications = result.getPreferences().getNotifications();
        assertFalse(notifications.getFastingReminders());
        assertTrue(notifications.getWeeklyReports());
        // Other notification fields should remain unchanged
        assertTrue(notifications.getEnabled());
        assertTrue(notifications.getMealReminders());
        assertFalse(notifications.getProgressUpdates());
        assertTrue(notifications.getGoalAchievements());
        
        verify(userRepository).save(testUser);
    }

    @Test
    void updatePreferences_PartialUpdate_OnlyFastingDefaults() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        UpdatePreferencesRequest.FastingDefaultsRequest fastingRequest = 
            new UpdatePreferencesRequest.FastingDefaultsRequest();
        fastingRequest.setDefaultGoalHours(20);
        fastingRequest.setPreferredFastingType("18:6");
        request.setFastingDefaults(fastingRequest);

        // Act
        User result = userService.updatePreferences(1L, request);

        // Assert
        UserPreferences.FastingDefaults fastingDefaults = result.getPreferences().getFastingDefaults();
        assertEquals(20, fastingDefaults.getDefaultGoalHours());
        assertEquals("18:6", fastingDefaults.getPreferredFastingType());
        // autoStartNextFast should remain unchanged
        assertFalse(fastingDefaults.getAutoStartNextFast());
        
        verify(userRepository).save(testUser);
    }

    @Test
    void updatePreferences_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setLanguage("de");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePreferences(999L, request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePreferences_InvalidLanguageCode_UsesDefault() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setLanguage("invalid");

        // Act
        User result = userService.updatePreferences(1L, request);

        // Assert - should fallback to EN as per Language.fromCode()
        assertEquals(UserPreferences.Language.EN, result.getPreferences().getLanguage());
    }

    @Test
    void updatePreferences_InvalidThemeValue_UsesDefault() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setTheme("invalid");

        // Act
        User result = userService.updatePreferences(1L, request);

        // Assert - should fallback to SYSTEM as per Theme.fromValue()
        assertEquals(UserPreferences.Theme.SYSTEM, result.getPreferences().getTheme());
    }

    @Test
    void updatePreferences_NullRequest_DoesNotThrow() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        // All fields are null

        // Act & Assert
        assertDoesNotThrow(() -> {
            userService.updatePreferences(1L, request);
        });

        verify(userRepository).save(testUser);
    }
}
