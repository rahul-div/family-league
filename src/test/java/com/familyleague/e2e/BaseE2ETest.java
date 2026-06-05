package com.familyleague.e2e;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    protected final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    // Use plain RestTemplate instead of TestRestTemplate to avoid auth streaming issues
    protected RestTemplate restTemplate = new RestTemplate();

    protected String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    protected String registerAndGetToken(String username, String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("username", username);
            put("email", email);
            put("password", password);
            put("displayName", username);
        }});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/auth/register", new HttpEntity<>(body, headers), String.class);

        JsonNode json = objectMapper.readTree(response.getBody());
        return json.path("data").path("accessToken").asText();
    }

    protected String loginAndGetToken(String usernameOrEmail, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("usernameOrEmail", usernameOrEmail);
            put("password", password);
        }});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/auth/login", new HttpEntity<>(body, headers), String.class);

        JsonNode json = objectMapper.readTree(response.getBody());
        return json.path("data").path("accessToken").asText();
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected String getAdminToken() throws Exception {
        return loginAndGetToken("admin", "Admin@1234");
    }

    protected ResponseEntity<String> get(String path, String token) {
        return restTemplate.exchange(baseUrl() + path, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);
    }

    protected ResponseEntity<String> post(String path, Object body, String token) throws Exception {
        String json = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);
        return restTemplate.postForEntity(baseUrl() + path,
                new HttpEntity<>(json, authHeaders(token)), String.class);
    }

    protected ResponseEntity<String> put(String path, Object body, String token) throws Exception {
        String json = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);
        return restTemplate.exchange(baseUrl() + path, HttpMethod.PUT,
                new HttpEntity<>(json, authHeaders(token)), String.class);
    }

    /**
     * Get HTTP status code even for error responses (4xx, 5xx).
     */
    protected int getStatus(String method, String path, String token, Object body) {
        try {
            HttpHeaders headers = authHeaders(token);
            String json = null;
            if (body != null) {
                json = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);
            }
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    baseUrl() + path, HttpMethod.valueOf(method), entity, String.class);
            return resp.getStatusCode().value();
        } catch (HttpClientErrorException e) {
            return e.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }
}
