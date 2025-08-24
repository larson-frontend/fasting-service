package com.larslab.fasting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.larslab.fasting.dto.UpdatePreferencesRequest;
import com.larslab.fasting.model.User;
import com.larslab.fasting.model.UserPreferences;
import com.larslab.fasting.service.UserService;
import com.larslab.fasting.security.JwtService;
import com.larslab.fasting.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserPreferencesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UpdatePreferencesRequest validRequest;

    @BeforeEach
    void setUp() {
        // Create test user with preferences
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        UserPreferences preferences = new UserPreferences();
        preferences.setLanguage(UserPreferences.Language.EN);
        preferences.setTheme(UserPreferences.Theme.DARK);
        preferences.setTimezone("Europe/Berlin");
        
        UserPreferences.NotificationPreferences notifications = new UserPreferences.NotificationPreferences();
        notifications.setEnabled(true);
        notifications.setFastingReminders(true);
        notifications.setMealReminders(false);
        notifications.setProgressUpdates(true);
        notifications.setGoalAchievements(false);
        notifications.setWeeklyReports(true);
        preferences.setNotifications(notifications);
        
        UserPreferences.FastingDefaults fastingDefaults = new UserPreferences.FastingDefaults();
        fastingDefaults.setDefaultGoalHours(18);
        fastingDefaults.setPreferredFastingType("18:6");
        fastingDefaults.setAutoStartNextFast(true);
        preferences.setFastingDefaults(fastingDefaults);
        
        testUser.setPreferences(preferences);

        // Create valid request
        validRequest = new UpdatePreferencesRequest();
        validRequest.setLanguage("de");
        validRequest.setTheme("light");
        validRequest.setTimezone("America/New_York");
        
        UpdatePreferencesRequest.NotificationPreferencesRequest notifRequest = 
            new UpdatePreferencesRequest.NotificationPreferencesRequest();
        notifRequest.setEnabled(false);
        notifRequest.setFastingReminders(false);
        notifRequest.setMealReminders(true);
        notifRequest.setProgressUpdates(false);
        notifRequest.setGoalAchievements(true);
        notifRequest.setWeeklyReports(false);
        validRequest.setNotifications(notifRequest);
        
        UpdatePreferencesRequest.FastingDefaultsRequest fastingRequest = 
            new UpdatePreferencesRequest.FastingDefaultsRequest();
        fastingRequest.setDefaultGoalHours(24);
        fastingRequest.setPreferredFastingType("24h");
        fastingRequest.setAutoStartNextFast(false);
        validRequest.setFastingDefaults(fastingRequest);
    }

    @Test
    void updatePreferences_ValidRequest_ReturnsUpdatedUser() throws Exception {
        // Arrange
        when(userService.updatePreferences(eq(1L), any(UpdatePreferencesRequest.class)))
            .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void updatePreferences_PartialUpdate_OnlyLanguage() throws Exception {
        // Arrange
        UpdatePreferencesRequest partialRequest = new UpdatePreferencesRequest();
        partialRequest.setLanguage("de");
        
        when(userService.updatePreferences(eq(1L), any(UpdatePreferencesRequest.class)))
            .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePreferences_PartialUpdate_OnlyNotifications() throws Exception {
        // Arrange
        UpdatePreferencesRequest partialRequest = new UpdatePreferencesRequest();
        UpdatePreferencesRequest.NotificationPreferencesRequest notifRequest = 
            new UpdatePreferencesRequest.NotificationPreferencesRequest();
        notifRequest.setFastingReminders(false);
        notifRequest.setMealReminders(true);
        partialRequest.setNotifications(notifRequest);
        
        when(userService.updatePreferences(eq(1L), any(UpdatePreferencesRequest.class)))
            .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePreferences_PartialUpdate_OnlyFastingDefaults() throws Exception {
        // Arrange
        UpdatePreferencesRequest partialRequest = new UpdatePreferencesRequest();
        UpdatePreferencesRequest.FastingDefaultsRequest fastingRequest = 
            new UpdatePreferencesRequest.FastingDefaultsRequest();
        fastingRequest.setDefaultGoalHours(20);
        fastingRequest.setPreferredFastingType("20:4");
        partialRequest.setFastingDefaults(fastingRequest);
        
        when(userService.updatePreferences(eq(1L), any(UpdatePreferencesRequest.class)))
            .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePreferences_InvalidLanguage_ReturnsBadRequest() throws Exception {
        // Arrange
        UpdatePreferencesRequest invalidRequest = new UpdatePreferencesRequest();
        invalidRequest.setLanguage("invalid");

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePreferences_InvalidTheme_ReturnsBadRequest() throws Exception {
        // Arrange
        UpdatePreferencesRequest invalidRequest = new UpdatePreferencesRequest();
        invalidRequest.setTheme("invalid");

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePreferences_UserNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(userService.updatePreferences(eq(999L), any(UpdatePreferencesRequest.class)))
            .thenThrow(new IllegalArgumentException("User not found"));

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePreferences_InvalidUserId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePreferences_EmptyBody_AllowsPartialUpdate() throws Exception {
        // Arrange
        when(userService.updatePreferences(eq(1L), any(UpdatePreferencesRequest.class)))
            .thenReturn(testUser);

        // Act & Assert - Empty body should be allowed for partial updates
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void updatePreferences_NullNotificationFields_ShouldNotUpdateNullFields() throws Exception {
        // Arrange
        UpdatePreferencesRequest partialRequest = new UpdatePreferencesRequest();
        UpdatePreferencesRequest.NotificationPreferencesRequest notifRequest = 
            new UpdatePreferencesRequest.NotificationPreferencesRequest();
        // Only set some fields, leave others null
        notifRequest.setFastingReminders(true);
        // mealReminders, progressUpdates, etc. remain null
        partialRequest.setNotifications(notifRequest);
        
        when(userService.updatePreferences(eq(1L), any(UpdatePreferencesRequest.class)))
            .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePreferences_ValidTimezone_AcceptsStandardTimezones() throws Exception {
        // Arrange
        UpdatePreferencesRequest timezoneRequest = new UpdatePreferencesRequest();
        timezoneRequest.setTimezone("Asia/Tokyo");
        
        when(userService.updatePreferences(eq(1L), any(UpdatePreferencesRequest.class)))
            .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timezoneRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePreferences_ValidFastingGoalHours_AcceptsValidRange() throws Exception {
        // Arrange
        UpdatePreferencesRequest fastingRequest = new UpdatePreferencesRequest();
        UpdatePreferencesRequest.FastingDefaultsRequest fastingDefaults = 
            new UpdatePreferencesRequest.FastingDefaultsRequest();
        fastingDefaults.setDefaultGoalHours(16); // Valid range 1-48
        fastingRequest.setFastingDefaults(fastingDefaults);
        
        when(userService.updatePreferences(eq(1L), any(UpdatePreferencesRequest.class)))
            .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/preferences")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fastingRequest)))
                .andExpect(status().isOk());
    }
}