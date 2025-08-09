package com.larslab.fasting.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Schema(description = "Eine Fasten-Session mit Start- und Endzeit sowie Ziel")
public class FastSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Eindeutige ID der Fasten-Session", example = "1")
    private Long id;
    
    @Schema(description = "Startzeitpunkt der Fasten-Session", example = "2024-01-01T10:00:00Z")
    private Instant startAt;
    
    @Schema(description = "Endzeitpunkt der Fasten-Session (null wenn noch aktiv)", example = "2024-01-02T10:00:00Z")
    private Instant endAt;
    
    @Column(name = "goal_hours")
    @Schema(description = "Ziel-Stunden für die Fasten-Session", example = "16", minimum = "1", maximum = "48")
    private Integer goalHours = 16;

    public FastSession() {}
    public FastSession(Instant startAt) { 
        this.startAt = startAt; 
        this.goalHours = 16;
    }
    public FastSession(Instant startAt, Instant endAt) { 
        this.startAt = startAt; 
        this.endAt = endAt;
        this.goalHours = 16;
    }
    public FastSession(Instant startAt, Integer goalHours) { 
        this.startAt = startAt; 
        this.goalHours = goalHours != null ? goalHours : 16;
    }

    public Long getId() { return id; }
    public Instant getStartAt() { return startAt; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }
    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }
    public Integer getGoalHours() { return goalHours != null ? goalHours : 16; }
    public void setGoalHours(Integer goalHours) { this.goalHours = goalHours != null ? goalHours : 16; }

    @Transient
    @Schema(description = "Berechnete Dauer der Fasten-Session", example = "PT18H30M")
    public Duration getDuration() {
        Instant end = (endAt != null) ? endAt : Instant.now();
        return Duration.between(startAt, end);
    }
}
