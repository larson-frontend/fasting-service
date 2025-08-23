package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to update user preferences")
public class UpdatePreferencesRequest {
    
    @Pattern(regexp = "en|de", message = "Language must be 'en' or 'de'")
    @Schema(description = "User interface language", example = "en", allowableValues = {"en", "de"})
    private String language;
    
    @Pattern(regexp = "light|dark|system", message = "Theme must be 'light', 'dark', or 'system'")
    @Schema(description = "UI theme preference", example = "light", allowableValues = {"light", "dark", "system"})
    private String theme;
    
    @Schema(description = "Notification preferences")
    private NotificationPreferencesRequest notifications;
    
    public UpdatePreferencesRequest() {}
    
    // Getters and Setters
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public NotificationPreferencesRequest getNotifications() { return notifications; }
    public void setNotifications(NotificationPreferencesRequest notifications) { this.notifications = notifications; }
    
    @Schema(description = "Notification preferences update request")
    public static class NotificationPreferencesRequest {
        
        @Schema(description = "Enable fasting reminders", example = "true")
        private Boolean fastingReminders;
        
        @Schema(description = "Enable meal reminders", example = "true")
        private Boolean mealReminders;
        
        @Schema(description = "Enable progress update notifications", example = "true")
        private Boolean progressUpdates;
        
        public NotificationPreferencesRequest() {}
        
        // Getters and Setters
        public Boolean getFastingReminders() { return fastingReminders; }
        public void setFastingReminders(Boolean fastingReminders) { this.fastingReminders = fastingReminders; }
        
        public Boolean getMealReminders() { return mealReminders; }
        public void setMealReminders(Boolean mealReminders) { this.mealReminders = mealReminders; }
        
        public Boolean getProgressUpdates() { return progressUpdates; }
        public void setProgressUpdates(Boolean progressUpdates) { this.progressUpdates = progressUpdates; }
    }
}
