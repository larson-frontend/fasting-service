package com.larslab.fasting.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Embeddable
@Schema(description = "User preferences and settings")
public class UserPreferences {
    
    @Column(name = "language")
    @Schema(description = "User interface language", example = "en", allowableValues = {"en", "de"})
    private Language language = Language.EN;
    
    @Column(name = "theme")
    @Schema(description = "UI theme preference", example = "light", allowableValues = {"light", "dark", "system"})
    private Theme theme = Theme.SYSTEM;
    
    @Embedded
    @Schema(description = "Notification preferences")
    private NotificationPreferences notifications;
    
    @Embedded
    @Schema(description = "Default fasting preferences")
    private FastingDefaults fastingDefaults;
    
    @Column(name = "timezone")
    @Schema(description = "User's timezone", example = "Europe/Berlin")
    private String timezone;
    
    public UserPreferences() {
        this.notifications = new NotificationPreferences();
        this.fastingDefaults = new FastingDefaults();
    }
    
    // Getters and Setters
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    
    public Theme getTheme() { return theme; }
    public void setTheme(Theme theme) { this.theme = theme; }
    
    public NotificationPreferences getNotifications() { return notifications; }
    public void setNotifications(NotificationPreferences notifications) { this.notifications = notifications; }
    
    public FastingDefaults getFastingDefaults() { return fastingDefaults; }
    public void setFastingDefaults(FastingDefaults fastingDefaults) { this.fastingDefaults = fastingDefaults; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    // Enums
    public enum Language {
        EN("en"), DE("de");
        
        private final String code;
        Language(String code) { this.code = code; }
        public String getCode() { return code; }
        
        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equals(code)) {
                    return lang;
                }
            }
            return EN; // Default fallback
        }
    }
    
    public enum Theme {
        LIGHT("light"), DARK("dark"), SYSTEM("system");
        
        private final String value;
        Theme(String value) { this.value = value; }
        public String getValue() { return value; }
        
        public static Theme fromValue(String value) {
            for (Theme theme : values()) {
                if (theme.value.equals(value)) {
                    return theme;
                }
            }
            return SYSTEM; // Default fallback
        }
    }
    
    @Embeddable
    @Schema(description = "Notification settings")
    public static class NotificationPreferences {
        
        @Column(name = "notifications_enabled")
        @Schema(description = "Master notification enable switch", example = "true")
        private Boolean enabled = true;
        
        @Column(name = "fasting_reminders")
        @Schema(description = "Enable fasting reminders", example = "true")
        private Boolean fastingReminders = true;
        
        @Column(name = "meal_reminders")
        @Schema(description = "Enable meal reminders", example = "true")
        private Boolean mealReminders = true;
        
        @Column(name = "progress_updates")
        @Schema(description = "Enable progress update notifications", example = "false")
        private Boolean progressUpdates = false;
        
        @Column(name = "goal_achievements")
        @Schema(description = "Enable goal achievement notifications", example = "true")
        private Boolean goalAchievements = true;
        
        @Column(name = "weekly_reports")
        @Schema(description = "Enable weekly progress reports", example = "false")
        private Boolean weeklyReports = false;
        
        public NotificationPreferences() {}
        
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
    
    @Embeddable
    @Schema(description = "Default fasting preferences")
    public static class FastingDefaults {
        
        @Column(name = "default_goal_hours")
        @Schema(description = "Default goal hours for new fasting sessions", example = "16")
        private Integer defaultGoalHours = 16;
        
        @Column(name = "preferred_fasting_type")
        @Schema(description = "Preferred fasting type", example = "16:8")
        private String preferredFastingType = "16:8";
        
        @Column(name = "auto_start_next_fast")
        @Schema(description = "Automatically start next fasting session", example = "false")
        private Boolean autoStartNextFast = false;
        
        public FastingDefaults() {}
        
        // Getters and Setters
        public Integer getDefaultGoalHours() { return defaultGoalHours; }
        public void setDefaultGoalHours(Integer defaultGoalHours) { this.defaultGoalHours = defaultGoalHours; }
        
        public String getPreferredFastingType() { return preferredFastingType; }
        public void setPreferredFastingType(String preferredFastingType) { this.preferredFastingType = preferredFastingType; }
        
        public Boolean getAutoStartNextFast() { return autoStartNextFast; }
        public void setAutoStartNextFast(Boolean autoStartNextFast) { this.autoStartNextFast = autoStartNextFast; }
    }
}
