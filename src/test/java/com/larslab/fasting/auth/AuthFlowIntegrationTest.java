package com.larslab.fasting.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import com.larslab.fasting.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void login_refresh_logout_flow() throws Exception {
        // Login (create user if not exists)
        String loginPayload = objectMapper.writeValueAsString(Map.of("username", "testuser1", "email", "testuser1@example.com"));
        String loginResponse = mockMvc.perform(post("/api/users/login-or-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Map<?,?> loginJson = objectMapper.readValue(loginResponse, Map.class);
        String accessToken = (String) loginJson.get("accessToken");
        String refreshToken = (String) loginJson.get("refreshToken");
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        // Refresh
        String refreshPayload = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
        String refreshResponse = mockMvc.perform(post("/api/users/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshPayload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        Map<?,?> refreshJson = objectMapper.readValue(refreshResponse, Map.class);
        String newAccess = (String) refreshJson.get("accessToken");
        String newRefresh = (String) refreshJson.get("refreshToken");
        assertThat(newAccess).isNotBlank();
        assertThat(newRefresh).isNotBlank();
        assertThat(newRefresh).isNotEqualTo(refreshToken);

        // Logout (revoke)
        String logoutPayload = objectMapper.writeValueAsString(Map.of("refreshToken", newRefresh));
        mockMvc.perform(post("/api/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutPayload))
            .andExpect(status().isOk());

        // Attempt to reuse revoked token should fail
        mockMvc.perform(post("/api/users/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshPayload))
            .andExpect(status().isUnauthorized());
    }
}
