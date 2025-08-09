package com.larslab.fasting.dto;

import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class StartFastRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void constructor_WithoutParameter_UsesDefault16() {
        // When
        StartFastRequest request = new StartFastRequest();

        // Then
        assertThat(request.getGoalHours()).isEqualTo(16);
    }

    @Test
    void constructor_WithValidGoalHours_SetsValue() {
        // When
        StartFastRequest request = new StartFastRequest(12);

        // Then
        assertThat(request.getGoalHours()).isEqualTo(12);
    }

    @Test
    void constructor_WithNullGoalHours_UsesDefault() {
        // When
        StartFastRequest request = new StartFastRequest(null);

        // Then
        assertThat(request.getGoalHours()).isEqualTo(16);
    }

    @Test
    void setGoalHours_WithValidValue_SetsValue() {
        // Given
        StartFastRequest request = new StartFastRequest();

        // When
        request.setGoalHours(24);

        // Then
        assertThat(request.getGoalHours()).isEqualTo(24);
    }

    @Test
    void setGoalHours_WithNullValue_UsesDefault() {
        // Given
        StartFastRequest request = new StartFastRequest();

        // When
        request.setGoalHours(null);

        // Then
        assertThat(request.getGoalHours()).isEqualTo(16);
    }

    @Test
    void validation_WithValidGoalHours_PassesValidation() {
        // Given
        StartFastRequest request = new StartFastRequest(12);

        // When
        Set<ConstraintViolation<StartFastRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void validation_WithGoalHoursTooLow_FailsValidation() {
        // Given
        StartFastRequest request = new StartFastRequest(0);

        // When
        Set<ConstraintViolation<StartFastRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Ziel-Stunden müssen mindestens 1 sein");
    }

    @Test
    void validation_WithGoalHoursTooHigh_FailsValidation() {
        // Given
        StartFastRequest request = new StartFastRequest(50);

        // When
        Set<ConstraintViolation<StartFastRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Ziel-Stunden dürfen maximal 48 sein");
    }

    @Test
    void validation_WithBoundaryValues_PassesValidation() {
        // Test minimum boundary
        StartFastRequest request1 = new StartFastRequest(1);
        Set<ConstraintViolation<StartFastRequest>> violations1 = validator.validate(request1);
        assertThat(violations1).isEmpty();

        // Test maximum boundary
        StartFastRequest request2 = new StartFastRequest(48);
        Set<ConstraintViolation<StartFastRequest>> violations2 = validator.validate(request2);
        assertThat(violations2).isEmpty();
    }
}
