package com.familyleague.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String username,
        String email,
        String role
) {
    public AuthResponse(String accessToken, UUID userId, String username, String email, String role) {
        this(accessToken, "Bearer", userId, username, email, role);
    }
}
