package com.familyleague.notification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.notification.entity.EmailNotification;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.entity.NotificationStatus;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, UUID> {

    @Query("SELECT en FROM EmailNotification en WHERE en.status = 'PENDING' AND en.retryCount < :maxRetry")
    List<EmailNotification> findPendingWithRetryBelow(@Param("maxRetry") int maxRetry);

    Page<EmailNotification> findByUserId(UUID userId, Pageable pageable);

    Optional<EmailNotification> findByEventTypeAndReferenceIdAndUserId(
            NotificationEventType eventType, UUID referenceId, UUID userId);

    Page<EmailNotification> findByStatus(NotificationStatus status, Pageable pageable);
}
