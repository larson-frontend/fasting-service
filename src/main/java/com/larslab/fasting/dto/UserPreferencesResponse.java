package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.larslab.fasting.model.UserPreferences;

@Schema(description = "User preferences response")
public class UserPreferencesResponse {
    
    @Schema(description = "User interface language", example = "en", allowableValues = {"en", "de"})
    private String language;
    
    @Schema(description = "UI theme preference", example = "light", allowableValues = {"light", "dark", "system"})
    private String theme;
    
    @Schema(description = "Notification preferences")
    private NotificationPreferencesResponse notifications;
    
    public UserPreferencesResponse() {}
    
    public UserPreferencesResponse(UserPreferences preferences) {
        this.language = preferences.getLanguage().getCode();
        this.theme = preferences.getTheme().getValue();
        this.notifications = new NotificationPreferencesResponse(preferences.getNotifications());
    }
    
    // Getters and Setters
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public NotificationPreferencesResponse getNotifications() { return notifications; }
    public void setNotifications(NotificationPreferencesResponse notifications) { this.notifications = notifications; }
    
    @Schema(description = "Notification preferences")
    public static class NotificationPreferencesResponse {
        
        @Schema(description = "Enable fasting reminders", example = "true")
        private Boolean fastingReminders;
        
        @Schema(description = "Enable meal reminders", example = "true")
        private Boolean mealReminders;
        
        @Schema(description = "Enable progress update notifications", example = "true")
        private Boolean progressUpdates;
        
        public NotificationPreferencesResponse() {}
        
        public NotificationPreferencesResponse(UserPreferences.NotificationPreferences notifications) {
            this.fastingReminders = notifications.getFastingReminders();
            this.mealReminders = notifications.getMealReminders();
            this.progressUpdates = notifications.getProgressUpdates();
        }
        
        // Getters and Setters
        public Boolean getFastingReminders() { return fastingReminders; }
        public void setFastingReminders(Boolean fastingReminders) { this.fastingReminders = fastingReminders; }
        
        public Boolean getMealReminders() { return mealReminders; }
        public void setMealReminders(Boolean mealReminders) { this.mealReminders = mealReminders; }
        
        public Boolean getProgressUpdates() { return progressUpdates; }
        public void setProgressUpdates(Boolean progressUpdates) { this.progressUpdates = progressUpdates; }
    }
}
