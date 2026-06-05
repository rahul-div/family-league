package com.familyleague.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.common.config.AppProperties;
import com.familyleague.match.entity.Match;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.service.EmailNotificationService;

@Component
@Transactional(readOnly = true)
public class SeasonLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeasonLifecycleScheduler.class);

    private final MatchRepository matchRepository;
    private final EmailNotificationService emailNotificationService;
    private final AppProperties appProperties;

    public SeasonLifecycleScheduler(MatchRepository matchRepository,
                                     EmailNotificationService emailNotificationService,
                                     AppProperties appProperties) {
        this.matchRepository = matchRepository;
        this.emailNotificationService = emailNotificationService;
        this.appProperties = appProperties;
    }

    @Scheduled(cron = "${app.scheduler.result-alert-cron:0 */5 * * * *}")
    public void alertPendingResults() {
        int alertHours = appProperties.getResult().getPendingAlertHours();
        Instant alertCutoff = Instant.now().minus(alertHours, ChronoUnit.HOURS);

        List<Match> pendingMatches = matchRepository.findCompletedMatchesWithoutResult(alertCutoff);
        if (!pendingMatches.isEmpty()) {
            StringBuilder body = new StringBuilder("The following matches are awaiting results:\n\n");
            pendingMatches.forEach(m ->
                    body.append(String.format("- Match #%d: %s vs %s (scheduled %s)\n",
                            m.getMatchNumber(), m.getHomeTeam().getName(),
                            m.getAwayTeam().getName(), m.getScheduledAt())));

            emailNotificationService.notifyAdmins(
                    "Pending Match Results Alert",
                    body.toString(),
                    NotificationEventType.PENDING_RESULT_ALERT
            );
            log.warn("Alert: {} matches pending results", pendingMatches.size());
        }
    }
}
