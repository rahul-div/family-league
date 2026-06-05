package com.familyleague.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.user.entity.User;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        String avatarUrl,
        String role,
        boolean active,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
