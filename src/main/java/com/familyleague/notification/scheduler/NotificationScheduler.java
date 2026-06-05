package com.familyleague.notification.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.familyleague.notification.service.EmailNotificationService;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final EmailNotificationService emailNotificationService;

    public NotificationScheduler(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Scheduled(cron = "${app.scheduler.email-process-cron:0 */5 * * * *}")
    public void processEmails() {
        log.debug("Processing pending email notifications");
        emailNotificationService.processPendingEmails();
    }
}
