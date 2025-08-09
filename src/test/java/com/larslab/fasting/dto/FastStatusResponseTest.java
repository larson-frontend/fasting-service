package com.larslab.fasting.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FastStatusResponseTest {

    @Test
    void constructor_WithActiveStatusFalse_CreatesInactiveResponse() {
        // When
        FastStatusResponse response = new FastStatusResponse(false);

        // Then
        assertThat(response.isActive()).isFalse();
        assertThat(response.getHours()).isNull();
        assertThat(response.getMinutes()).isNull();
        assertThat(response.getSince()).isNull();
        assertThat(response.getGoalHours()).isNull();
        assertThat(response.getProgressPercent()).isNull();
    }

    @Test
    void constructor_WithActiveStatusTrue_CreatesActiveResponse() {
        // Given
        String since = "2025-08-09T10:30:00Z";

        // When
        FastStatusResponse response = new FastStatusResponse(true, 4, 30, since, 12);

        // Then
        assertThat(response.isActive()).isTrue();
        assertThat(response.getHours()).isEqualTo(4);
        assertThat(response.getMinutes()).isEqualTo(30);
        assertThat(response.getSince()).isEqualTo(since);
        assertThat(response.getGoalHours()).isEqualTo(12);
        assertThat(response.getProgressPercent()).isNotNull();
    }

    @Test
    void constructor_CalculatesProgressPercentCorrectly() {
        // Given - 4.5 hours into 12-hour fast = 37.5%
        String since = "2025-08-09T10:30:00Z";

        // When
        FastStatusResponse response = new FastStatusResponse(true, 4, 30, since, 12);

        // Then
        assertThat(response.getProgressPercent()).isCloseTo(37.5, within(0.001));
    }

    @Test
    void constructor_WithZeroGoalHours_HandlesGracefully() {
        // Given
        String since = "2025-08-09T10:30:00Z";

        // When
        FastStatusResponse response = new FastStatusResponse(true, 4, 30, since, 0);

        // Then
        assertThat(response.getProgressPercent()).isNull();
    }

    @Test
    void constructor_WithNullGoalHours_HandlesGracefully() {
        // Given
        String since = "2025-08-09T10:30:00Z";

        // When
        FastStatusResponse response = new FastStatusResponse(true, 4, 30, since, null);

        // Then
        assertThat(response.getProgressPercent()).isNull();
    }

    @Test
    void progressPercent_WithExactHours_CalculatesCorrectly() {
        // Given - exactly 8 hours into 16-hour fast = 50%
        String since = "2025-08-09T10:30:00Z";

        // When
        FastStatusResponse response = new FastStatusResponse(true, 8, 0, since, 16);

        // Then
        assertThat(response.getProgressPercent()).isEqualTo(50.0);
    }

    @Test
    void progressPercent_WithOverGoal_CalculatesOver100() {
        // Given - 20 hours into 16-hour fast = 125%
        String since = "2025-08-09T10:30:00Z";

        // When
        FastStatusResponse response = new FastStatusResponse(true, 20, 0, since, 16);

        // Then
        assertThat(response.getProgressPercent()).isEqualTo(125.0);
    }

    @Test
    void progressPercent_WithMinutes_CalculatesCorrectly() {
        // Given - 2 hours 15 minutes into 9-hour fast = 25%
        String since = "2025-08-09T10:30:00Z";

        // When
        FastStatusResponse response = new FastStatusResponse(true, 2, 15, since, 9);

        // Then
        assertThat(response.getProgressPercent()).isCloseTo(25.0, within(0.001));
    }

    @Test
    void progressPercent_RoundsToThreeDecimals() {
        // Given - 1 hour 1 minute into 6-hour fast = 16.944...%
        String since = "2025-08-09T10:30:00Z";

        // When
        FastStatusResponse response = new FastStatusResponse(true, 1, 1, since, 6);

        // Then (actual calculation: 61 minutes / 360 minutes = 16.944%)
        assertThat(response.getProgressPercent()).isCloseTo(16.944, within(0.001));
    }

    @Test
    void setters_WorkCorrectly() {
        // Given
        FastStatusResponse response = new FastStatusResponse();

        // When
        response.setActive(true);
        response.setHours(5);
        response.setMinutes(45);
        response.setSince("2025-08-09T12:00:00Z");
        response.setGoalHours(10);
        response.setProgressPercent(57.5);

        // Then
        assertThat(response.isActive()).isTrue();
        assertThat(response.getHours()).isEqualTo(5);
        assertThat(response.getMinutes()).isEqualTo(45);
        assertThat(response.getSince()).isEqualTo("2025-08-09T12:00:00Z");
        assertThat(response.getGoalHours()).isEqualTo(10);
        assertThat(response.getProgressPercent()).isEqualTo(57.5);
    }
}
