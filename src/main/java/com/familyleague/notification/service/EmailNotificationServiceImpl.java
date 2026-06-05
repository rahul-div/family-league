package com.familyleague.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.familyleague.common.config.AppProperties;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.notification.dto.BulkNotificationRequest;
import com.familyleague.notification.dto.EmailNotificationResponse;
import com.familyleague.notification.entity.EmailNotification;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.entity.NotificationStatus;
import com.familyleague.notification.repository.EmailNotificationRepository;
import com.familyleague.user.entity.User;
import com.familyleague.user.entity.UserRole;
import com.familyleague.user.repository.UserRepository;

@Service
@Transactional
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

    private final EmailNotificationRepository emailNotificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final String mailUsername;

    public EmailNotificationServiceImpl(EmailNotificationRepository emailNotificationRepository,
                                         UserRepository userRepository,
                                         JavaMailSender mailSender,
                                         AppProperties appProperties,
                                         @Value("${spring.mail.username:}") String mailUsername) {
        this.emailNotificationRepository = emailNotificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.appProperties = appProperties;
        this.mailUsername = mailUsername;
    }

    @Override
    @Async("emailExecutor")
    public void queueEmail(String toEmail, UUID userId, String subject, String body,
                           NotificationEventType eventType, UUID referenceId) {
        try {
            EmailNotification notification = EmailNotification.builder()
                    .toEmail(toEmail)
                    .userId(userId)
                    .subject(subject)
                    .body(body)
                    .eventType(eventType)
                    .status(NotificationStatus.PENDING)
                    .referenceId(referenceId)
                    .build();
            emailNotificationRepository.save(notification);
            log.debug("Queued email to {} for event {}", toEmail, eventType);
        } catch (Exception e) {
            // Retry once without userId FK (user may not be committed yet in async context)
            log.warn("Email queue failed with userId {}, retrying without FK: {}", userId, e.getMessage());
            try {
                EmailNotification notification = EmailNotification.builder()
                        .toEmail(toEmail)
                        .userId(null)
                        .subject(subject)
                        .body(body)
                        .eventType(eventType)
                        .status(NotificationStatus.PENDING)
                        .referenceId(referenceId)
                        .build();
                emailNotificationRepository.save(notification);
            } catch (Exception retryEx) {
                log.error("Email queue retry also failed for {}: {}", toEmail, retryEx.getMessage());
            }
        }
    }

    @Override
    public void processPendingEmails() {
        int maxRetry = appProperties.getNotification().getMaxRetryCount();
        List<EmailNotification> pending = emailNotificationRepository.findPendingWithRetryBelow(maxRetry);

        for (EmailNotification notification : pending) {
            trySend(notification);
        }
    }

    private void trySend(EmailNotification notification) {
        if (!StringUtils.hasText(mailUsername)) {
            // Simulated mode — log instead of sending
            log.info("[SIMULATED EMAIL] To: {}, Subject: {}, Body: {}",
                    notification.getToEmail(), notification.getSubject(),
                    truncate(notification.getBody(), 200));
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            emailNotificationRepository.save(notification);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appProperties.getNotification().getFromEmail());
            message.setTo(notification.getToEmail());
            message.setSubject(notification.getSubject());
            message.setText(notification.getBody());
            mailSender.send(message);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            log.info("Email sent to {}", notification.getToEmail());
        } catch (Exception e) {
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setErrorMessage(truncate(e.getMessage(), 500));
            if (notification.getRetryCount() >= appProperties.getNotification().getMaxRetryCount()) {
                notification.setStatus(NotificationStatus.FAILED);
                log.error("Email permanently failed for {}: {}", notification.getToEmail(), e.getMessage());
            } else {
                log.warn("Email retry {} for {}: {}", notification.getRetryCount(),
                        notification.getToEmail(), e.getMessage());
            }
        }
        emailNotificationRepository.save(notification);
    }

    @Override
    public void notifyAdmins(String subject, String body, NotificationEventType eventType) {
        List<User> admins = userRepository.findAllByRoleAndActive(UserRole.ADMIN);
        for (User admin : admins) {
            queueEmail(admin.getEmail(), admin.getId(), subject, body, eventType, null);
        }
    }

    @Override
    public void sendBulkNotification(BulkNotificationRequest request) {
        List<User> users = userRepository.findAllById(request.userIds()).stream()
                .filter(u -> !u.isDeleted() && u.isActive())
                .toList();

        for (User user : users) {
            queueEmail(user.getEmail(), user.getId(), request.subject(), request.message(),
                    request.eventType(), null);
        }
        log.info("Bulk notification queued for {} users", users.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmailNotificationResponse> getNotificationLogs(Pageable pageable) {
        Page<EmailNotification> page = emailNotificationRepository.findAll(pageable);
        List<EmailNotificationResponse> content = page.getContent().stream()
                .map(EmailNotificationResponse::from)
                .toList();
        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmailNotificationResponse> getMyNotifications(UUID userId, Pageable pageable) {
        Page<EmailNotification> page = emailNotificationRepository.findByUserId(userId, pageable);
        List<EmailNotificationResponse> content = page.getContent().stream()
                .map(EmailNotificationResponse::from)
                .toList();
        return PagedResponse.from(page, content);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
