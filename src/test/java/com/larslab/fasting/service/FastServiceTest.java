package com.larslab.fasting.service;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.repo.FastRepository;
import com.larslab.fasting.dto.StartFastRequest;
import com.larslab.fasting.dto.FastStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FastServiceTest {

    @Mock
    private FastRepository repository;

    @InjectMocks
    private FastService fastService;

    private FastSession activeFastSession;
    private FastSession completedFastSession;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        
        activeFastSession = new FastSession();
        activeFastSession.setStartAt(now.minus(2, ChronoUnit.HOURS));
        activeFastSession.setGoalHours(16);
        activeFastSession.setEndAt(null);
        
        completedFastSession = new FastSession();
        completedFastSession.setStartAt(now.minus(18, ChronoUnit.HOURS));
        completedFastSession.setEndAt(now.minus(2, ChronoUnit.HOURS));
        completedFastSession.setGoalHours(16);
    }

    @Test
    void getActive_WhenActiveSessionExists_ReturnsActiveSession() {
        // Given
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.of(activeFastSession));

        // When
        Optional<FastSession> result = fastService.getActive();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(activeFastSession);
        verify(repository).findFirstByEndAtIsNullOrderByStartAtDesc();
    }

    @Test
    void getActive_WhenNoActiveSession_ReturnsEmpty() {
        // Given
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.empty());

        // When
        Optional<FastSession> result = fastService.getActive();

        // Then
        assertThat(result).isEmpty();
        verify(repository).findFirstByEndAtIsNullOrderByStartAtDesc();
    }

    @Test
    void start_WhenNoActiveSession_CreatesNewSession() {
        // Given
        StartFastRequest request = new StartFastRequest(12);
        FastSession newSession = new FastSession(now, 12);
        
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.empty());
        when(repository.save(any(FastSession.class))).thenReturn(newSession);

        // When
        FastSession result = fastService.start(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGoalHours()).isEqualTo(12);
        verify(repository).findFirstByEndAtIsNullOrderByStartAtDesc();
        verify(repository).save(any(FastSession.class));
    }

    @Test
    void start_WhenActiveSessionExists_ReturnsExistingSession() {
        // Given
        StartFastRequest request = new StartFastRequest(12);
        
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.of(activeFastSession));

        // When
        FastSession result = fastService.start(request);

        // Then
        assertThat(result).isEqualTo(activeFastSession);
        verify(repository).findFirstByEndAtIsNullOrderByStartAtDesc();
        verify(repository, never()).save(any());
    }

    @Test
    void start_WithDefaultGoalHours_UsesDefault16Hours() {
        // Given
        StartFastRequest request = new StartFastRequest(); // Default constructor
        FastSession newSession = new FastSession(now, 16);
        
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.empty());
        when(repository.save(any(FastSession.class))).thenReturn(newSession);

        // When
        FastSession result = fastService.start(request);

        // Then
        assertThat(result.getGoalHours()).isEqualTo(16);
        verify(repository).save(any(FastSession.class));
    }

    @Test
    void stop_WhenActiveSessionExists_StopsSession() {
        // Given
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.of(activeFastSession));
        when(repository.save(any(FastSession.class))).thenReturn(activeFastSession);

        // When
        FastSession result = fastService.stop();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEndAt()).isNotNull();
        verify(repository).findFirstByEndAtIsNullOrderByStartAtDesc();
        verify(repository).save(activeFastSession);
    }

    @Test
    void stop_WhenNoActiveSession_ThrowsException() {
        // Given
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> fastService.stop())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kein aktives Fasten");
        
        verify(repository).findFirstByEndAtIsNullOrderByStartAtDesc();
        verify(repository, never()).save(any());
    }

    @Test
    void getStatus_WhenActiveSession_ReturnsActiveStatus() {
        // Given
        activeFastSession.setStartAt(now.minus(2, ChronoUnit.HOURS).minus(30, ChronoUnit.MINUTES));
        activeFastSession.setGoalHours(12);
        
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.of(activeFastSession));

        // When
        FastStatusResponse result = fastService.getStatus();

        // Then
        assertThat(result.isActive()).isTrue();
        assertThat(result.getHours()).isEqualTo(2);
        assertThat(result.getMinutes()).isEqualTo(30);
        assertThat(result.getGoalHours()).isEqualTo(12);
        assertThat(result.getProgressPercent()).isGreaterThan(0);
        assertThat(result.getSince()).isNotNull();
    }

    @Test
    void getStatus_WhenNoActiveSession_ReturnsInactiveStatus() {
        // Given
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.empty());

        // When
        FastStatusResponse result = fastService.getStatus();

        // Then
        assertThat(result.isActive()).isFalse();
        assertThat(result.getHours()).isNull();
        assertThat(result.getMinutes()).isNull();
        assertThat(result.getGoalHours()).isNull();
        assertThat(result.getProgressPercent()).isNull();
        assertThat(result.getSince()).isNull();
    }

    @Test
    void getStatus_CalculatesProgressPercentCorrectly() {
        // Given - 4 hours into a 12-hour fast = 33.333%
        activeFastSession.setStartAt(now.minus(4, ChronoUnit.HOURS));
        activeFastSession.setGoalHours(12);
        
        when(repository.findFirstByEndAtIsNullOrderByStartAtDesc())
                .thenReturn(Optional.of(activeFastSession));

        // When
        FastStatusResponse result = fastService.getStatus();

        // Then
        assertThat(result.getProgressPercent()).isCloseTo(33.333, within(0.1));
    }

    @Test
    void history_ReturnsAllSessions() {
        // Given
        List<FastSession> sessions = List.of(activeFastSession, completedFastSession);
        when(repository.findAll()).thenReturn(sessions);

        // When
        List<FastSession> result = fastService.history();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(activeFastSession, completedFastSession);
        verify(repository).findAll();
    }

    @Test
    void history_WhenNoSessions_ReturnsEmptyList() {
        // Given
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<FastSession> result = fastService.history();

        // Then
        assertThat(result).isEmpty();
        verify(repository).findAll();
    }
}
