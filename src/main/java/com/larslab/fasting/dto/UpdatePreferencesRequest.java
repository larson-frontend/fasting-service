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
    
    @Schema(description = "Fasting default preferences")
    private FastingDefaultsRequest fastingDefaults;
    
    @Schema(description = "User's timezone", example = "Europe/Berlin")
    private String timezone;
    
    public UpdatePreferencesRequest() {}
    
    // Getters and Setters
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public NotificationPreferencesRequest getNotifications() { return notifications; }
    public void setNotifications(NotificationPreferencesRequest notifications) { this.notifications = notifications; }
    
    public FastingDefaultsRequest getFastingDefaults() { return fastingDefaults; }
    public void setFastingDefaults(FastingDefaultsRequest fastingDefaults) { this.fastingDefaults = fastingDefaults; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    @Schema(description = "Notification preferences update request")
    public static class NotificationPreferencesRequest {
        
        @Schema(description = "Master notification enable switch", example = "true")
        private Boolean enabled;
        
        @Schema(description = "Enable fasting reminders", example = "true")
        private Boolean fastingReminders;
        
        @Schema(description = "Enable meal reminders", example = "true")
        private Boolean mealReminders;
        
        @Schema(description = "Enable progress update notifications", example = "false")
        private Boolean progressUpdates;
        
        @Schema(description = "Enable goal achievement notifications", example = "true")
        private Boolean goalAchievements;
        
        @Schema(description = "Enable weekly progress reports", example = "false")
        private Boolean weeklyReports;
        
        public NotificationPreferencesRequest() {}
        
        // Getters and Setters
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public Boolean getFastingReminders() { return fastingReminders; }
        public void setFastingReminders(Boolean fastingReminders) { this.fastingReminders = fastingReminders; }
        
        public Boolean getMealReminders() { return mealReminders; }
        public void setMealReminders(Boolean mealReminders) { this.mealReminders = mealReminders; }
        
        public Boolean getProgressUpdates() { return progressUpdates; }
        public void setProgressUpdates(Boolean progressUpdates) { this.progressUpdates = progressUpdates; }
        
        public Boolean getGoalAchievements() { return goalAchievements; }
        public void setGoalAchievements(Boolean goalAchievements) { this.goalAchievements = goalAchievements; }
        
        public Boolean getWeeklyReports() { return weeklyReports; }
        public void setWeeklyReports(Boolean weeklyReports) { this.weeklyReports = weeklyReports; }
    }
    
    @Schema(description = "Fasting defaults update request")
    public static class FastingDefaultsRequest {
        
        @Schema(description = "Default goal hours for new fasting sessions", example = "16")
        private Integer defaultGoalHours;
        
        @Schema(description = "Preferred fasting type", example = "16:8")
        private String preferredFastingType;
        
        @Schema(description = "Automatically start next fasting session", example = "false")
        private Boolean autoStartNextFast;
        
        public FastingDefaultsRequest() {}
        
        // Getters and Setters
        public Integer getDefaultGoalHours() { return defaultGoalHours; }
        public void setDefaultGoalHours(Integer defaultGoalHours) { this.defaultGoalHours = defaultGoalHours; }
        
        public String getPreferredFastingType() { return preferredFastingType; }
        public void setPreferredFastingType(String preferredFastingType) { this.preferredFastingType = preferredFastingType; }
        
        public Boolean getAutoStartNextFast() { return autoStartNextFast; }
        public void setAutoStartNextFast(Boolean autoStartNextFast) { this.autoStartNextFast = autoStartNextFast; }
    }
}
