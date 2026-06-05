package com.familyleague.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

class AuthE2ETest extends BaseE2ETest {

    @Test
    void fullAuthWorkflow() throws Exception {
        // Register
        String token = registerAndGetToken("e2eauth", "e2eauth@test.com", "Password@1");
        assertThat(token).isNotBlank();

        // Get profile
        ResponseEntity<String> profileResponse = get("/auth/me", token);
        assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode profile = objectMapper.readTree(profileResponse.getBody());
        assertThat(profile.path("data").path("email").asText()).isEqualTo("e2eauth@test.com");

        // Login by email
        String loginToken = loginAndGetToken("e2eauth@test.com", "Password@1");
        assertThat(loginToken).isNotBlank();

        // Login by username
        String loginByUsername = loginAndGetToken("e2eauth", "Password@1");
        assertThat(loginByUsername).isNotBlank();
    }

    @Test
    void unauthenticatedAccess_returns401() {
        int status = getStatus("GET", "/leagues", "", null);
        assertThat(status).isEqualTo(401);
    }

    @Test
    void register_invalidPassword_returns400() {
        int status = getStatus("POST", "/auth/register", "",
                java.util.Map.of("username", "badpwd", "email", "bad@test.com", "password", "weak"));
        assertThat(status).isEqualTo(400);
    }

    @Test
    void updateProfile() throws Exception {
        String token = registerAndGetToken("e2eprofile", "e2eprofile@test.com", "Password@1");

        // Get user id
        ResponseEntity<String> meResponse = get("/auth/me", token);
        JsonNode me = objectMapper.readTree(meResponse.getBody());
        String userId = me.path("data").path("id").asText();

        // Update profile
        ResponseEntity<String> updateResponse = put("/users/" + userId + "/profile",
                java.util.Map.of("displayName", "Updated Name", "avatarUrl", "https://example.com/avatar.png"),
                token);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode updated = objectMapper.readTree(updateResponse.getBody());
        assertThat(updated.path("data").path("displayName").asText()).isEqualTo("Updated Name");
    }
}
