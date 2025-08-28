package com.larslab.fasting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.model.User;
import com.larslab.fasting.service.FastService;
import com.larslab.fasting.service.UserService;
import com.larslab.fasting.security.JwtService;
import com.larslab.fasting.security.UserAuthorizationService;
import com.larslab.fasting.dto.StartFastRequest;
import com.larslab.fasting.dto.FastStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FastController.class)
@WithMockUser(username = "testuser", roles = {"USER"})
class FastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FastService fastService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserAuthorizationService authorizationService;

    @MockBean
    private JwtService jwtService;

    private User testUser;
    private FastSession activeSession;
    private FastSession completedSession;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        testUser = new User("testuser", "test@example.com");
        testUser.setId(1L);

        activeSession = new FastSession(testUser, now.minus(2, ChronoUnit.HOURS), 16);
        completedSession = new FastSession(testUser, now.minus(18, ChronoUnit.HOURS), 16);
        completedSession.setEndAt(now.minus(2, ChronoUnit.HOURS));
    }

    // ===== BASIC ENDPOINT TESTS =====

    @Test
    void start_WithValidRequest_ReturnsCreatedSession() throws Exception {
        // Given
        StartFastRequest request = new StartFastRequest(16);
        FastSession newSession = new FastSession(now, 16);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(fastService.start(eq(testUser), any(StartFastRequest.class))).thenReturn(newSession);

        // When & Then
        mockMvc.perform(post("/api/fast/start")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.goalHours").value(16));
    }

    @Test
    void start_WithInvalidUserId_ReturnsBadRequest() throws Exception {
        // Given
        StartFastRequest request = new StartFastRequest(16);

        // When & Then
        mockMvc.perform(post("/api/fast/start")
                .param("userId", "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void start_WithNonExistentUser_ReturnsNotFound() throws Exception {
        // Given
        StartFastRequest request = new StartFastRequest(16);

        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/fast/start")
                .param("userId", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void status_WithActiveSession_ReturnsActiveStatus() throws Exception {
        // Given
        FastStatusResponse statusResponse = new FastStatusResponse(true, 2, 30, now.minus(2, ChronoUnit.HOURS).toString(), 16);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(fastService.getStatus(testUser)).thenReturn(statusResponse);

        // When & Then
        mockMvc.perform(get("/api/fast/status")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.hours").value(2))
                .andExpect(jsonPath("$.minutes").value(30))
                .andExpect(jsonPath("$.goalHours").value(16));
    }

    @Test
    void status_WithNoActiveSession_ReturnsInactiveStatus() throws Exception {
        // Given
        FastStatusResponse statusResponse = new FastStatusResponse(false);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(fastService.getStatus(testUser)).thenReturn(statusResponse);

        // When & Then
        mockMvc.perform(get("/api/fast/status")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.hours").doesNotExist())
                .andExpect(jsonPath("$.minutes").doesNotExist());
    }

    @Test
    void history_WithValidUser_ReturnsSessionList() throws Exception {
        // Given
        List<FastSession> sessions = List.of(activeSession, completedSession);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(fastService.history(testUser)).thenReturn(sessions);

        // When & Then
        mockMvc.perform(get("/api/fast/history")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ===== AUTHENTICATED USER ENDPOINT TESTS =====

    @Test
    @WithMockUser(username = "testuser")
    void statusByUser_WithValidAuthentication_ReturnsStatus() throws Exception {
        // Given
        FastStatusResponse statusResponse = new FastStatusResponse(true, 2, 30, now.minus(2, ChronoUnit.HOURS).toString(), 16);

        when(authorizationService.userMatches("testuser", "testuser")).thenReturn(true);
        when(userService.getUserByIdentifier("testuser")).thenReturn(Optional.of(testUser));
        when(fastService.getStatus(testUser)).thenReturn(statusResponse);

        // When & Then
        mockMvc.perform(get("/api/fast/user/testuser/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void historyByUser_WithValidAuthentication_ReturnsHistory() throws Exception {
        // Given
        List<FastSession> sessions = List.of(activeSession, completedSession);

        when(authorizationService.userMatches("testuser", "testuser")).thenReturn(true);
        when(userService.getUserByIdentifier("testuser")).thenReturn(Optional.of(testUser));
        when(fastService.history(testUser)).thenReturn(sessions);

        // When & Then
        mockMvc.perform(get("/api/fast/user/testuser/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ===== VALIDATION TESTS =====

    @Test
    void start_WithInvalidGoalHours_ReturnsBadRequest() throws Exception {
        // Given
        StartFastRequest request = new StartFastRequest(50); // Invalid: > 48

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/fast/start")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
