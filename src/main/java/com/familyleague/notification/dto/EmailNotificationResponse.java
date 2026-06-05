package com.familyleague.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.notification.entity.EmailNotification;

public record EmailNotificationResponse(
        UUID id,
        String toEmail,
        UUID userId,
        String subject,
        String eventType,
        String status,
        Instant sentAt,
        int retryCount,
        String errorMessage,
        Instant createdAt
) {
    public static EmailNotificationResponse from(EmailNotification en) {
        return new EmailNotificationResponse(
                en.getId(),
                en.getToEmail(),
                en.getUserId(),
                en.getSubject(),
                en.getEventType() != null ? en.getEventType().name() : null,
                en.getStatus().name(),
                en.getSentAt(),
                en.getRetryCount(),
                en.getErrorMessage(),
                en.getCreatedAt()
        );
    }
}
