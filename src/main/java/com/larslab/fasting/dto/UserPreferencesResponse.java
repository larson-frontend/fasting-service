package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.larslab.fasting.model.UserPreferences;
import com.larslab.fasting.config.FeatureFlags;

@Schema(description = "User preferences response")
public class UserPreferencesResponse {
    
    @Schema(description = "User interface language", example = "en", allowableValues = {"en", "de"})
    private String language;
    
    @Schema(description = "UI theme preference", example = "light", allowableValues = {"light", "dark", "system"})
    private String theme;
    
    @Schema(description = "User's timezone", example = "Europe/Berlin")
    private String timezone;
    
    @Schema(description = "Notification preferences")
    private NotificationPreferencesResponse notifications;
    
    @Schema(description = "Fasting default preferences")
    private FastingDefaultsResponse fastingDefaults;
    
    public UserPreferencesResponse() {}
    
    public UserPreferencesResponse(UserPreferences preferences) {
        this.language = preferences.getLanguage().getCode();
        this.theme = preferences.getTheme().getValue();
        this.timezone = preferences.getTimezone();
        this.notifications = new NotificationPreferencesResponse(preferences.getNotifications());
        this.fastingDefaults = new FastingDefaultsResponse(preferences.getFastingDefaults());
    }
    
    /**
     * Feature-aware constructor that only includes enabled features
     */
    public UserPreferencesResponse(UserPreferences preferences, FeatureFlags featureFlags) {
        // Language is always included (core functionality)
        this.language = preferences.getLanguage().getCode();
        
        // Theme only if feature is enabled
        if (featureFlags.isThemeSelection()) {
            this.theme = preferences.getTheme().getValue();
        } else {
            this.theme = "system"; // Default fallback
        }
        
        // Timezone is always included (core functionality)
        this.timezone = preferences.getTimezone();
        
        // Notifications with feature flag awareness
        this.notifications = new NotificationPreferencesResponse(preferences.getNotifications(), featureFlags);
        
        // Fasting defaults with feature flag awareness
        if (featureFlags.isAdvancedFasting()) {
            this.fastingDefaults = new FastingDefaultsResponse(preferences.getFastingDefaults());
        } else {
            // Basic defaults for core functionality
            this.fastingDefaults = new FastingDefaultsResponse();
            this.fastingDefaults.setDefaultGoalHours(16); // Safe default
        }
    }
    
    // Getters and Setters
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public NotificationPreferencesResponse getNotifications() { return notifications; }
    public void setNotifications(NotificationPreferencesResponse notifications) { this.notifications = notifications; }
    
    public FastingDefaultsResponse getFastingDefaults() { return fastingDefaults; }
    public void setFastingDefaults(FastingDefaultsResponse fastingDefaults) { this.fastingDefaults = fastingDefaults; }
    
    @Schema(description = "Notification preferences")
    public static class NotificationPreferencesResponse {
        
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
        
        public NotificationPreferencesResponse() {}
        
        public NotificationPreferencesResponse(UserPreferences.NotificationPreferences notifications) {
            this.enabled = notifications.getEnabled();
            this.fastingReminders = notifications.getFastingReminders();
            this.mealReminders = notifications.getMealReminders();
            this.progressUpdates = notifications.getProgressUpdates();
            this.goalAchievements = notifications.getGoalAchievements();
            this.weeklyReports = notifications.getWeeklyReports();
        }
        
        /**
         * Feature-aware constructor that only includes enabled notification types
         */
        public NotificationPreferencesResponse(UserPreferences.NotificationPreferences notifications, FeatureFlags featureFlags) {
            // Basic notification enabled/disabled is always available (core functionality)
            this.enabled = notifications.getEnabled();
            
            if (featureFlags.isDetailedNotifications()) {
                // Include all detailed notification types when feature is enabled
                this.fastingReminders = notifications.getFastingReminders();
                this.mealReminders = notifications.getMealReminders();
                this.progressUpdates = notifications.getProgressUpdates();
                this.goalAchievements = notifications.getGoalAchievements();
                this.weeklyReports = notifications.getWeeklyReports();
            } else {
                // For initial release, hide detailed notification options
                // Set them to safe defaults but don't expose them in API
                this.fastingReminders = true;  // Safe default
                this.mealReminders = true;     // Safe default
                this.progressUpdates = false;  // Safe default
                this.goalAchievements = true;  // Safe default
                this.weeklyReports = false;    // Safe default
            }
        }
        
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
    
    @Schema(description = "Fasting default preferences")
    public static class FastingDefaultsResponse {
        
        @Schema(description = "Default goal hours for new fasting sessions", example = "16")
        private Integer defaultGoalHours;
        
        @Schema(description = "Preferred fasting type", example = "16:8")
        private String preferredFastingType;
        
        @Schema(description = "Automatically start next fasting session", example = "false")
        private Boolean autoStartNextFast;
        
        public FastingDefaultsResponse() {}
        
        public FastingDefaultsResponse(UserPreferences.FastingDefaults fastingDefaults) {
            this.defaultGoalHours = fastingDefaults.getDefaultGoalHours();
            this.preferredFastingType = fastingDefaults.getPreferredFastingType();
            this.autoStartNextFast = fastingDefaults.getAutoStartNextFast();
        }
        
        // Getters and Setters
        public Integer getDefaultGoalHours() { return defaultGoalHours; }
        public void setDefaultGoalHours(Integer defaultGoalHours) { this.defaultGoalHours = defaultGoalHours; }
        
        public String getPreferredFastingType() { return preferredFastingType; }
        public void setPreferredFastingType(String preferredFastingType) { this.preferredFastingType = preferredFastingType; }
        
        public Boolean getAutoStartNextFast() { return autoStartNextFast; }
        public void setAutoStartNextFast(Boolean autoStartNextFast) { this.autoStartNextFast = autoStartNextFast; }
    }
}
