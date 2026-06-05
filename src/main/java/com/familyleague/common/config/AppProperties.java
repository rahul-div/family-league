package com.familyleague.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Typed configuration properties bound from application.yml under the "app" prefix.
 */
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Prediction prediction = new Prediction();
    private Notification notification = new Notification();
    private Scheduler scheduler = new Scheduler();
    private Result result = new Result();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000; // 24 hours
    }

    @Getter
    @Setter
    public static class Prediction {
        private int leagueLockHours = 4;
        private int matchLockHours = 1;
        private int reminderHoursBeforeMatch = 2;
    }

    @Getter
    @Setter
    public static class Notification {
        private String adminEmail = "admin@familyleague.com";
        private String fromEmail = "noreply@familyleague.com";
        private int maxRetryCount = 3;
    }

    @Getter
    @Setter
    public static class Scheduler {
        private String predictionLockCron = "0 * * * * *";
        private String emailProcessCron = "0 */5 * * * *";
        private String resultAlertCron = "0 */5 * * * *";
    }

    @Getter
    @Setter
    public static class Result {
        private int pendingAlertHours = 2;
    }
}
