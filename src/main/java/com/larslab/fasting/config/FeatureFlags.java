package com.larslab.fasting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Feature flags configuration for controlling feature availability in the backend.
 * This allows us to gradually roll out features and maintain a stable core while 
 * developing additional functionality.
 */
@Component
@ConfigurationProperties(prefix = "features")
public class FeatureFlags {
    
    /**
     * Controls whether detailed notification features are available.
     * When false, only basic notification enabled/disabled functionality is available.
     * When true, all notification types (reminders, meals, progress, etc.) are available.
     */
    private boolean detailedNotifications = false;
    
    /**
     * Controls whether theme selection features are available.
     * When false, theme preferences are not processed or returned.
     * When true, full theme selection (light, dark, system) is available.
     */
    private boolean themeSelection = false;
    
    /**
     * Controls whether advanced fasting features are available.
     * When false, only basic start/stop fasting is available.
     * When true, custom goals, schedules, and tracking are available.
     */
    private boolean advancedFasting = false;
    
    // Getters and setters
    public boolean isDetailedNotifications() {
        return detailedNotifications;
    }
    
    public void setDetailedNotifications(boolean detailedNotifications) {
        this.detailedNotifications = detailedNotifications;
    }
    
    public boolean isThemeSelection() {
        return themeSelection;
    }
    
    public void setThemeSelection(boolean themeSelection) {
        this.themeSelection = themeSelection;
    }
    
    public boolean isAdvancedFasting() {
        return advancedFasting;
    }
    
    public void setAdvancedFasting(boolean advancedFasting) {
        this.advancedFasting = advancedFasting;
    }
    
    /**
     * Helper method to check if a specific feature is enabled
     */
    public boolean isFeatureEnabled(String featureName) {
        switch (featureName.toLowerCase()) {
            case "detailednotifications":
            case "detailed-notifications":
                return detailedNotifications;
            case "themeselection":
            case "theme-selection":
                return themeSelection;
            case "advancedfasting":
            case "advanced-fasting":
                return advancedFasting;
            default:
                return false;
        }
    }
}
