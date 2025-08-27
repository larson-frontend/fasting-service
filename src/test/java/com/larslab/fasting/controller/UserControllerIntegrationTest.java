package com.larslab.fasting.controller;

import com.larslab.fasting.repo.UserRepository;
import com.larslab.fasting.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
// @WithMockUser is not effective for TestRestTemplate real HTTP calls; we pass real Bearer tokens instead
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.larslab.fasting.support.AbstractIntegrationTest;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void featureFlags(DynamicPropertyRegistry registry) {
        registry.add("features.theme-selection", () -> "true");
        registry.add("features.detailed-notifications", () -> "true");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private RestTemplate patchEnabledRestTemplate;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        userRepository.deleteAll();
        
        // Create RestTemplate with PATCH support
        patchEnabledRestTemplate = new RestTemplate();
        patchEnabledRestTemplate.setRequestFactory(new org.springframework.http.client.HttpComponentsClientHttpRequestFactory());
    }

    @Test
    public void testLoginOrCreateUser_NewUser() {
        LoginOrCreateRequest request = new LoginOrCreateRequest("test_user", "test@example.com");
        
        ResponseEntity<LoginOrCreateResponse> response = restTemplate.postForEntity(
            "/api/users/login-or-create", request, LoginOrCreateResponse.class);
        
        assertThat(response.getStatusCode().value()).isEqualTo(200); // Should be 200 for both create and login
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("test_user");
        assertThat(response.getBody().getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getBody().getUser().getPreferences().getLanguage()).isEqualTo("en");
    }

    @Test
    public void testLoginOrCreateUser_ExistingUser() {
        // First create a user directly via the service to avoid concurrency issues
        LoginOrCreateRequest firstRequest = new LoginOrCreateRequest("existing_user", "existing@example.com");
        restTemplate.postForEntity("/api/users/login-or-create", firstRequest, LoginOrCreateResponse.class);
        
        // Try to login with the same user
        LoginOrCreateRequest request = new LoginOrCreateRequest("existing_user", "existing@example.com");
        
        ResponseEntity<LoginOrCreateResponse> response = restTemplate.postForEntity(
            "/api/users/login-or-create", request, LoginOrCreateResponse.class);
        
        assertThat(response.getStatusCode().value()).isEqualTo(200); // OK
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("existing_user");
    }

    @Test
    public void testUpdateLanguage() {
        // First create a user
        LoginOrCreateRequest createRequest = new LoginOrCreateRequest("lang_user", "lang@example.com");
        ResponseEntity<LoginOrCreateResponse> createResponse = restTemplate.postForEntity(
            "/api/users/login-or-create", createRequest, LoginOrCreateResponse.class);
        
        assertThat(createResponse.getBody()).isNotNull();
        String userId = createResponse.getBody().getUser().getId();
        String token = createResponse.getBody().getToken();
        assertThat(token).as("access token should be present in login response").isNotBlank();
        
        UpdateLanguageRequest request = new UpdateLanguageRequest("de");
        
        // Use the PATCH-enabled RestTemplate
        String url = "http://localhost:" + port + "/api/users/language?userId=" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<UserResponse> response = patchEnabledRestTemplate.exchange(
            url,
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            UserResponse.class);
        
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPreferences().getLanguage()).isEqualTo("de");
    }

    @Test
    public void testUpdatePreferences() {
        // First create a user
        LoginOrCreateRequest createRequest = new LoginOrCreateRequest("pref_user", "pref@example.com");
        ResponseEntity<LoginOrCreateResponse> createResponse = restTemplate.postForEntity(
            "/api/users/login-or-create", createRequest, LoginOrCreateResponse.class);
        
        assertThat(createResponse.getBody()).isNotNull();
        String userId = createResponse.getBody().getUser().getId();
        String token = createResponse.getBody().getToken();
        assertThat(token).as("access token should be present in login response").isNotBlank();
        
        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setLanguage("de");
        request.setTheme("dark");
        
        UpdatePreferencesRequest.NotificationPreferencesRequest notifications = 
            new UpdatePreferencesRequest.NotificationPreferencesRequest();
        notifications.setFastingReminders(false);
        notifications.setMealReminders(true);
        notifications.setProgressUpdates(false);
        request.setNotifications(notifications);
        
        // Use the PATCH-enabled RestTemplate
        String url = "http://localhost:" + port + "/api/users/preferences?userId=" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<UserResponse> response = patchEnabledRestTemplate.exchange(
            url,
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            UserResponse.class);
        
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPreferences().getLanguage()).isEqualTo("de");
        assertThat(response.getBody().getPreferences().getTheme()).isEqualTo("dark");
        assertThat(response.getBody().getPreferences().getNotifications().getFastingReminders()).isFalse();
        assertThat(response.getBody().getPreferences().getNotifications().getMealReminders()).isTrue();
        assertThat(response.getBody().getPreferences().getNotifications().getProgressUpdates()).isFalse();
    }

    @Test
    public void testGetCurrentUser() {
        // First create a user
        LoginOrCreateRequest createRequest = new LoginOrCreateRequest("current_user", "current@example.com");
        ResponseEntity<LoginOrCreateResponse> createResponse = restTemplate.postForEntity(
            "/api/users/login-or-create", createRequest, LoginOrCreateResponse.class);
        
        assertThat(createResponse.getBody()).isNotNull();
        String userId = createResponse.getBody().getUser().getId();
        String token = createResponse.getBody().getToken();
        assertThat(token).as("access token should be present in login response").isNotBlank();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
            "/api/users/current?userId=" + userId,
            HttpMethod.GET,
            entity,
            UserResponse.class);
        
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("current_user");
        assertThat(response.getBody().getEmail()).isEqualTo("current@example.com");
    }
}
