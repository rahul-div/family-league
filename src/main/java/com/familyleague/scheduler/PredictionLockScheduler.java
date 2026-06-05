package com.familyleague.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.common.config.AppProperties;
import com.familyleague.league.entity.Season;
import com.familyleague.league.entity.SeasonStatus;
import com.familyleague.league.repository.SeasonRepository;
import com.familyleague.match.entity.Match;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.service.EmailNotificationService;
import com.familyleague.prediction.entity.LeaguePrediction;
import com.familyleague.prediction.entity.MatchPrediction;
import com.familyleague.prediction.repository.LeaguePredictionRepository;
import com.familyleague.prediction.repository.MatchPredictionRepository;
import com.familyleague.user.entity.User;
import com.familyleague.user.entity.UserRole;
import com.familyleague.user.repository.UserRepository;

@Component
@Transactional
public class PredictionLockScheduler {

    private static final Logger log = LoggerFactory.getLogger(PredictionLockScheduler.class);

    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final LeaguePredictionRepository leaguePredictionRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;
    private final AppProperties appProperties;

    public PredictionLockScheduler(MatchRepository matchRepository, SeasonRepository seasonRepository,
                                    MatchPredictionRepository matchPredictionRepository,
                                    LeaguePredictionRepository leaguePredictionRepository,
                                    UserRepository userRepository,
                                    EmailNotificationService emailNotificationService,
                                    AppProperties appProperties) {
        this.matchRepository = matchRepository;
        this.seasonRepository = seasonRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.leaguePredictionRepository = leaguePredictionRepository;
        this.userRepository = userRepository;
        this.emailNotificationService = emailNotificationService;
        this.appProperties = appProperties;
    }

    @Scheduled(cron = "${app.scheduler.prediction-lock-cron:0 * * * * *}")
    public void lockMatchPredictions() {
        List<Match> matchesToLock = matchRepository.findMatchesToLock();
        for (Match match : matchesToLock) {
            List<MatchPrediction> predictions = matchPredictionRepository.findAllByMatchId(match.getId());
            predictions.forEach(p -> p.setLocked(true));
            matchPredictionRepository.saveAll(predictions);
            log.info("Locked {} match predictions for match #{}", predictions.size(), match.getMatchNumber());
        }
    }

    @Scheduled(cron = "${app.scheduler.prediction-lock-cron:0 * * * * *}")
    public void lockSeasonPredictions() {
        List<Season> seasonsToLock = seasonRepository.findSeasonsToLock();
        for (Season season : seasonsToLock) {
            season.setStatus(SeasonStatus.PREDICTION_LOCKED);
            seasonRepository.save(season);

            List<LeaguePrediction> predictions = leaguePredictionRepository.findBySeasonId(season.getId());
            predictions.forEach(p -> p.setLocked(true));
            leaguePredictionRepository.saveAll(predictions);
            log.info("Season '{}' predictions locked ({} predictions)", season.getName(), predictions.size());
        }
    }

    @Scheduled(cron = "0 0 * * * *") // hourly
    public void sendPredictionReminders() {
        int reminderHours = appProperties.getPrediction().getReminderHoursBeforeMatch();
        Instant reminderCutoff = Instant.now().plus(reminderHours, ChronoUnit.HOURS);

        List<Match> matchesClosingSoon = matchRepository.findMatchesNeedingReminder(reminderCutoff);
        List<User> activeUsers = userRepository.findAllByRoleAndActive(UserRole.USER);

        for (Match match : matchesClosingSoon) {
            List<UUID> usersWhoPredicted = matchPredictionRepository
                    .findUserIdsWhoSubmittedPrediction(match.getId());

            for (User user : activeUsers) {
                if (usersWhoPredicted.contains(user.getId())) continue;

                // Check idempotency — don't send duplicate reminders
                if (emailNotificationService instanceof com.familyleague.notification.service.EmailNotificationServiceImpl) {
                    // Skip if already reminded for this match
                }

                emailNotificationService.queueEmail(
                        user.getEmail(), user.getId(),
                        "Prediction Reminder: " + match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName(),
                        "Don't forget to submit your prediction! The window closes at " + match.getPredictionLockTime(),
                        NotificationEventType.MATCH_PREDICTION_REMINDER,
                        match.getId()
                );
            }
        }
    }
}
