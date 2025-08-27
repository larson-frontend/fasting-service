package com.larslab.fasting.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.larslab.fasting.support.AbstractIntegrationTest;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RateLimitingIntegrationTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void rateLimitProps(DynamicPropertyRegistry registry) {
        registry.add("rate.limit.capacity", () -> "5");
        registry.add("rate.limit.window.ms", () -> "60000");
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void exceeding_rate_limit_returns_429() throws Exception {
        String payload = "{\"username\":\"rluser\",\"email\":\"rluser@example.com\"}";
        // First 5 requests OK
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/users/login-or-create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isOk());
        }
        // 6th should be limited
        mockMvc.perform(post("/api/users/login-or-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isTooManyRequests());
    }
}
