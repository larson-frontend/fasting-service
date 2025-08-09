package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Status Response der aktuellen Fasten-Session")
public class FastStatusResponse {
    
    @Schema(description = "Ob eine Fasten-Session aktiv ist", example = "true")
    private boolean active;
    
    @Schema(description = "Stunden seit Start der aktiven Session", example = "8")
    private Integer hours;
    
    @Schema(description = "Minuten seit Start der aktiven Session", example = "30")
    private Integer minutes;
    
    @Schema(description = "Startzeitpunkt der aktiven Session", example = "2025-08-09T10:30:00Z")
    private String since;
    
    @Schema(description = "Ziel-Stunden für die aktive Session", example = "16")
    private Integer goalHours;
    
    @Schema(description = "Fortschritt in Prozent zum Ziel", example = "53.125")
    private Double progressPercent;
    
    // Constructors
    public FastStatusResponse() {}
    
    public FastStatusResponse(boolean active) {
        this.active = active;
    }
    
    public FastStatusResponse(boolean active, Integer hours, Integer minutes, String since, Integer goalHours) {
        this.active = active;
        this.hours = hours;
        this.minutes = minutes;
        this.since = since;
        this.goalHours = goalHours;
        
        // Calculate progress percentage
        if (hours != null && minutes != null && goalHours != null && goalHours > 0) {
            double totalMinutes = (hours * 60.0) + minutes;
            double goalMinutes = goalHours * 60.0;
            this.progressPercent = Math.round((totalMinutes / goalMinutes) * 100.0 * 1000.0) / 1000.0;
        }
    }
    
    // Getters and Setters
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Integer getHours() {
        return hours;
    }
    
    public void setHours(Integer hours) {
        this.hours = hours;
    }
    
    public Integer getMinutes() {
        return minutes;
    }
    
    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }
    
    public String getSince() {
        return since;
    }
    
    public void setSince(String since) {
        this.since = since;
    }
    
    public Integer getGoalHours() {
        return goalHours;
    }
    
    public void setGoalHours(Integer goalHours) {
        this.goalHours = goalHours;
    }
    
    public Double getProgressPercent() {
        return progressPercent;
    }
    
    public void setProgressPercent(Double progressPercent) {
        this.progressPercent = progressPercent;
    }
}
