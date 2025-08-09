package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Request zum Starten einer Fasten-Session mit optionalem Ziel")
public class StartFastRequest {
    
    @Schema(description = "Ziel-Stunden für die Fasten-Session", 
            example = "16", 
            minimum = "1", 
            maximum = "48", 
            defaultValue = "16")
    @Min(value = 1, message = "Ziel-Stunden müssen mindestens 1 sein")
    @Max(value = 48, message = "Ziel-Stunden dürfen maximal 48 sein")
    private Integer goalHours = 16;
    
    public StartFastRequest() {}
    
    public StartFastRequest(Integer goalHours) {
        this.goalHours = goalHours != null ? goalHours : 16;
    }
    
    public Integer getGoalHours() {
        return goalHours; // FIXED: Return actual value, default handling is in constructor
    }
    
    public void setGoalHours(Integer goalHours) {
        this.goalHours = goalHours != null ? goalHours : 16; // Keep default handling in setter
    }
}
