package com.familyleague.notification.dto;

import java.util.List;
import java.util.UUID;

import com.familyleague.notification.entity.NotificationEventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BulkNotificationRequest(
        @NotEmpty List<UUID> userIds,
        @NotNull NotificationEventType eventType,
        @NotBlank String subject,
        @NotBlank String message
) {}
