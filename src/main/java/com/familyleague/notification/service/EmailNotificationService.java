package com.familyleague.notification.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.notification.dto.BulkNotificationRequest;
import com.familyleague.notification.dto.EmailNotificationResponse;
import com.familyleague.notification.entity.NotificationEventType;

public interface EmailNotificationService {

    void queueEmail(String toEmail, UUID userId, String subject, String body,
                    NotificationEventType eventType, UUID referenceId);

    void processPendingEmails();

    void notifyAdmins(String subject, String body, NotificationEventType eventType);

    void sendBulkNotification(BulkNotificationRequest request);

    PagedResponse<EmailNotificationResponse> getNotificationLogs(Pageable pageable);

    PagedResponse<EmailNotificationResponse> getMyNotifications(UUID userId, Pageable pageable);
}
