package com.larslab.fasting.model;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

class FastSessionTest {

    @Test
    void constructor_WithStartTime_SetsDefaults() {
        // Given
        Instant startTime = Instant.now();

        // When
        FastSession session = new FastSession(startTime);

        // Then
        assertThat(session.getStartAt()).isEqualTo(startTime);
        assertThat(session.getEndAt()).isNull();
        assertThat(session.getGoalHours()).isEqualTo(16);
    }

    @Test
    void constructor_WithStartTimeAndGoalHours_SetsValues() {
        // Given
        Instant startTime = Instant.now();
        Integer goalHours = 12;

        // When
        FastSession session = new FastSession(startTime, goalHours);

        // Then
        assertThat(session.getStartAt()).isEqualTo(startTime);
        assertThat(session.getEndAt()).isNull();
        assertThat(session.getGoalHours()).isEqualTo(12);
    }

    @Test
    void constructor_WithNullGoalHours_UsesDefault() {
        // Given
        Instant startTime = Instant.now();

        // When
        FastSession session = new FastSession(startTime, (Integer) null);

        // Then
        assertThat(session.getGoalHours()).isEqualTo(16);
    }

    @Test
    void setGoalHours_WithNull_UsesDefault() {
        // Given
        FastSession session = new FastSession();

        // When
        session.setGoalHours(null);

        // Then
        assertThat(session.getGoalHours()).isEqualTo(16);
    }

    @Test
    void setGoalHours_WithValidValue_SetsValue() {
        // Given
        FastSession session = new FastSession();

        // When
        session.setGoalHours(24);

        // Then
        assertThat(session.getGoalHours()).isEqualTo(24);
    }

    @Test
    void getDuration_WhenSessionActive_CalculatesFromNow() {
        // Given
        Instant twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        FastSession session = new FastSession(twoHoursAgo);

        // When
        Duration duration = session.getDuration();

        // Then
        assertThat(duration.toHours()).isGreaterThanOrEqualTo(1);
        assertThat(duration.toHours()).isLessThanOrEqualTo(3); // Allow some tolerance
    }

    @Test
    void getDuration_WhenSessionCompleted_CalculatesExactDuration() {
        // Given
        Instant start = Instant.now().minus(4, ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        FastSession session = new FastSession(start);
        session.setEndAt(end);

        // When
        Duration duration = session.getDuration();

        // Then
        assertThat(duration.toHours()).isEqualTo(2);
    }

    @Test
    void getDuration_WithPreciseTime_CalculatesCorrectly() {
        // Given
        Instant start = Instant.parse("2025-08-09T10:00:00Z");
        Instant end = Instant.parse("2025-08-09T18:30:00Z");
        FastSession session = new FastSession(start);
        session.setEndAt(end);

        // When
        Duration duration = session.getDuration();

        // Then
        assertThat(duration.toHours()).isEqualTo(8);
        assertThat(duration.toMinutesPart()).isEqualTo(30);
    }
}
