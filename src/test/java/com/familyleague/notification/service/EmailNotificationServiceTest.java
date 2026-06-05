package com.familyleague.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.familyleague.common.config.AppProperties;
import com.familyleague.notification.entity.EmailNotification;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.entity.NotificationStatus;
import com.familyleague.notification.repository.EmailNotificationRepository;
import com.familyleague.user.entity.User;
import com.familyleague.user.entity.UserRole;
import com.familyleague.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock private EmailNotificationRepository emailNotificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private JavaMailSender mailSender;

    private EmailNotificationServiceImpl service;
    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.getNotification().setMaxRetryCount(3);
        appProperties.getNotification().setFromEmail("noreply@test.com");

        // Empty mail username = simulated mode
        service = new EmailNotificationServiceImpl(
                emailNotificationRepository, userRepository, mailSender, appProperties, "");
    }

    @Test
    void queueEmail_createsPendingNotification() {
        when(emailNotificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.queueEmail("to@test.com", UUID.randomUUID(), "Subject", "Body",
                NotificationEventType.WELCOME, null);

        ArgumentCaptor<EmailNotification> captor = ArgumentCaptor.forClass(EmailNotification.class);
        verify(emailNotificationRepository).save(captor.capture());
        EmailNotification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getToEmail()).isEqualTo("to@test.com");
        assertThat(saved.getSubject()).isEqualTo("Subject");
    }

    @Test
    void processPendingEmails_simulatedMode_marksSent() {
        EmailNotification pending = EmailNotification.builder()
                .toEmail("user@test.com").subject("Test").body("Body")
                .status(NotificationStatus.PENDING).retryCount(0).build();
        pending.setId(UUID.randomUUID());

        when(emailNotificationRepository.findPendingWithRetryBelow(3)).thenReturn(List.of(pending));
        when(emailNotificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.processPendingEmails();

        assertThat(pending.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(pending.getSentAt()).isNotNull();
    }

    @Test
    void processPendingEmails_realMode_sendsEmail() {
        // Real mode = non-empty mail username
        service = new EmailNotificationServiceImpl(
                emailNotificationRepository, userRepository, mailSender, appProperties, "real@smtp.com");

        EmailNotification pending = EmailNotification.builder()
                .toEmail("user@test.com").subject("Test").body("Hello")
                .status(NotificationStatus.PENDING).retryCount(0).build();
        pending.setId(UUID.randomUUID());

        when(emailNotificationRepository.findPendingWithRetryBelow(3)).thenReturn(List.of(pending));
        when(emailNotificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.processPendingEmails();

        verify(mailSender).send(any(SimpleMailMessage.class));
        assertThat(pending.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void processPendingEmails_smtpFailure_incrementsRetry() {
        service = new EmailNotificationServiceImpl(
                emailNotificationRepository, userRepository, mailSender, appProperties, "real@smtp.com");

        EmailNotification pending = EmailNotification.builder()
                .toEmail("user@test.com").subject("Test").body("Hello")
                .status(NotificationStatus.PENDING).retryCount(0).build();
        pending.setId(UUID.randomUUID());

        when(emailNotificationRepository.findPendingWithRetryBelow(3)).thenReturn(List.of(pending));
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));
        when(emailNotificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.processPendingEmails();

        assertThat(pending.getRetryCount()).isEqualTo(1);
        assertThat(pending.getStatus()).isEqualTo(NotificationStatus.PENDING); // not failed yet
    }

    @Test
    void processPendingEmails_maxRetryReached_marksFailed() {
        service = new EmailNotificationServiceImpl(
                emailNotificationRepository, userRepository, mailSender, appProperties, "real@smtp.com");

        EmailNotification pending = EmailNotification.builder()
                .toEmail("user@test.com").subject("Test").body("Hello")
                .status(NotificationStatus.PENDING).retryCount(2).build(); // Already retried twice
        pending.setId(UUID.randomUUID());

        when(emailNotificationRepository.findPendingWithRetryBelow(3)).thenReturn(List.of(pending));
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));
        when(emailNotificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.processPendingEmails();

        assertThat(pending.getRetryCount()).isEqualTo(3);
        assertThat(pending.getStatus()).isEqualTo(NotificationStatus.FAILED); // Now permanently failed
    }

    @Test
    void notifyAdmins_queuesEmailForEachAdmin() {
        User admin1 = User.builder().username("admin1").email("a1@test.com").build();
        admin1.setId(UUID.randomUUID());
        User admin2 = User.builder().username("admin2").email("a2@test.com").build();
        admin2.setId(UUID.randomUUID());

        when(userRepository.findAllByRoleAndActive(UserRole.ADMIN)).thenReturn(List.of(admin1, admin2));
        when(emailNotificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.notifyAdmins("Alert", "Something happened", NotificationEventType.PENDING_RESULT_ALERT);

        verify(emailNotificationRepository, times(2)).save(any());
    }
}
