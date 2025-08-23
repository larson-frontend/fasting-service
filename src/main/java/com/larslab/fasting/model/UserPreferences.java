package com.larslab.fasting.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Embeddable
@Schema(description = "User preferences and settings")
public class UserPreferences {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    @Schema(description = "User interface language", example = "en", allowableValues = {"en", "de"})
    private Language language = Language.EN;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "theme")
    @Schema(description = "UI theme preference", example = "light", allowableValues = {"light", "dark", "system"})
    private Theme theme = Theme.SYSTEM;
    
    @Embedded
    @Schema(description = "Notification preferences")
    private NotificationPreferences notifications;
    
    public UserPreferences() {
        this.notifications = new NotificationPreferences();
    }
    
    // Getters and Setters
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    
    public Theme getTheme() { return theme; }
    public void setTheme(Theme theme) { this.theme = theme; }
    
    public NotificationPreferences getNotifications() { return notifications; }
    public void setNotifications(NotificationPreferences notifications) { this.notifications = notifications; }
    
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
        
        @Column(name = "fasting_reminders")
        @Schema(description = "Enable fasting reminders", example = "true")
        private Boolean fastingReminders = true;
        
        @Column(name = "meal_reminders")
        @Schema(description = "Enable meal reminders", example = "true")
        private Boolean mealReminders = true;
        
        @Column(name = "progress_updates")
        @Schema(description = "Enable progress update notifications", example = "true")
        private Boolean progressUpdates = true;
        
        public NotificationPreferences() {}
        
        // Getters and Setters
        public Boolean getFastingReminders() { return fastingReminders; }
        public void setFastingReminders(Boolean fastingReminders) { this.fastingReminders = fastingReminders; }
        
        public Boolean getMealReminders() { return mealReminders; }
        public void setMealReminders(Boolean mealReminders) { this.mealReminders = mealReminders; }
        
        public Boolean getProgressUpdates() { return progressUpdates; }
        public void setProgressUpdates(Boolean progressUpdates) { this.progressUpdates = progressUpdates; }
    }
}
