package com.larslab.fasting.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.larslab.fasting.support.AbstractIntegrationTest;

import java.util.Map;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvalidRefreshTokenIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void invalid_and_revoked_refresh_token_behaviour() throws Exception {
        // Login to get valid refresh token
        String loginPayload = objectMapper.writeValueAsString(Map.of("username","badrefuser","email","badrefuser@example.com"));
        String loginResponse = mockMvc.perform(post("/api/users/login-or-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        Map<?,?> loginJson = objectMapper.readValue(loginResponse, Map.class);
        String refreshToken = (String) loginJson.get("refreshToken");
        assertThat(refreshToken).isNotBlank();

        // Use clearly invalid token
        String invalidPayload = objectMapper.writeValueAsString(Map.of("refreshToken","invalid:token"));
        mockMvc.perform(post("/api/users/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isUnauthorized());

        // Revoke legitimate token via logout
        String logoutPayload = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
        mockMvc.perform(post("/api/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutPayload))
            .andExpect(status().isOk());

        // Attempt to use revoked token
        mockMvc.perform(post("/api/users/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
            .andExpect(status().isUnauthorized());
    }
}
