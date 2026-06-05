package com.familyleague.scoring.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.service.EmailNotificationService;
import com.familyleague.scoring.dto.LeaderboardEntryResponse;
import com.familyleague.scoring.dto.MatchScoreDetailResponse;
import com.familyleague.scoring.dto.MyRankResponse;
import com.familyleague.scoring.dto.SeasonScoreDetailResponse;
import com.familyleague.scoring.entity.UserSeasonScore;
import com.familyleague.scoring.repository.MatchScoreDetailRepository;
import com.familyleague.scoring.repository.SeasonScoreDetailRepository;
import com.familyleague.scoring.repository.UserSeasonScoreRepository;
import com.familyleague.user.entity.User;
import com.familyleague.user.entity.UserRole;
import com.familyleague.user.repository.UserRepository;

@Service
@Transactional
public class LeaderboardServiceImpl implements LeaderboardService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardServiceImpl.class);

    private final UserSeasonScoreRepository userSeasonScoreRepository;
    private final MatchScoreDetailRepository matchScoreDetailRepository;
    private final SeasonScoreDetailRepository seasonScoreDetailRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    public LeaderboardServiceImpl(UserSeasonScoreRepository userSeasonScoreRepository,
                                   MatchScoreDetailRepository matchScoreDetailRepository,
                                   SeasonScoreDetailRepository seasonScoreDetailRepository,
                                   UserRepository userRepository,
                                   EmailNotificationService emailNotificationService) {
        this.userSeasonScoreRepository = userSeasonScoreRepository;
        this.matchScoreDetailRepository = matchScoreDetailRepository;
        this.seasonScoreDetailRepository = seasonScoreDetailRepository;
        this.userRepository = userRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LeaderboardEntryResponse> getLeaderboard(UUID seasonId, Pageable pageable) {
        Page<UserSeasonScore> page = userSeasonScoreRepository.findLeaderboard(seasonId, pageable);
        List<LeaderboardEntryResponse> content = page.getContent().stream()
                .map(LeaderboardEntryResponse::from).toList();
        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public MyRankResponse getMyRank(UUID seasonId) {
        UUID userId = SecurityUser.currentUserId();

        UserSeasonScore uss = userSeasonScoreRepository.findByUserIdAndSeasonId(userId, seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("No score found for current user in this season"));

        LeaderboardEntryResponse entry = LeaderboardEntryResponse.from(uss);

        List<MatchScoreDetailResponse> matchScores = matchScoreDetailRepository
                .findBySeasonIdAndUserId(seasonId, userId).stream()
                .map(MatchScoreDetailResponse::from).toList();

        List<SeasonScoreDetailResponse> seasonScores = seasonScoreDetailRepository
                .findBySeasonIdAndUserId(seasonId, userId).stream()
                .map(SeasonScoreDetailResponse::from).toList();

        return new MyRankResponse(entry, matchScores, seasonScores);
    }

    @Override
    @Async("scoreExecutor")
    public void recalculateLeaderboard(UUID seasonId) {
        log.info("Starting leaderboard recalculation for season {}", seasonId);

        List<User> activeUsers = userRepository.findAllByRoleAndActive(UserRole.USER);
        List<User> admins = userRepository.findAllByRoleAndActive(UserRole.ADMIN);
        // Include admins as they may also have predictions
        activeUsers.addAll(admins);

        List<UserSeasonScore> scores = activeUsers.stream().map(user -> {
            int matchPoints = matchScoreDetailRepository.sumMatchPointsForUserInSeason(user.getId(), seasonId);
            int seasonPoints = seasonScoreDetailRepository.sumSeasonPointsForUserInSeason(user.getId(), seasonId);
            int total = matchPoints + seasonPoints;

            UserSeasonScore uss = userSeasonScoreRepository.findByUserIdAndSeasonId(user.getId(), seasonId)
                    .orElse(UserSeasonScore.builder().user(user)
                            .season(null) // will be set below
                            .build());

            // Need to handle season reference
            if (uss.getSeason() == null) {
                uss.setSeason(new com.familyleague.league.entity.Season());
                uss.getSeason().setId(seasonId);
            }

            uss.setMatchPoints(matchPoints);
            uss.setSeasonPredictionPoints(seasonPoints);
            uss.setTotalPoints(total);
            uss.setLastCalculatedAt(Instant.now());
            return uss;
        }).filter(uss -> uss.getTotalPoints() > 0 || userSeasonScoreRepository
                .findByUserIdAndSeasonId(uss.getUser().getId(), seasonId).isPresent())
                .toList();

        // Sort and assign ranks
        List<UserSeasonScore> sorted = scores.stream()
                .sorted(Comparator.comparingInt(UserSeasonScore::getTotalPoints).reversed()
                        .thenComparing(UserSeasonScore::getLastCalculatedAt))
                .toList();

        AtomicInteger rank = new AtomicInteger(1);
        for (UserSeasonScore uss : sorted) {
            uss.setRank(rank.getAndIncrement());
            userSeasonScoreRepository.save(uss);
        }

        // Notify admins with top-10 summary
        StringBuilder summary = new StringBuilder("Leaderboard updated!\n\nTop 10:\n");
        sorted.stream().limit(10).forEach(uss ->
                summary.append(String.format("#%d %s - %d pts\n",
                        uss.getRank(), uss.getUser().getDisplayName(), uss.getTotalPoints())));

        emailNotificationService.notifyAdmins(
                "Leaderboard Updated - Season",
                summary.toString(),
                NotificationEventType.LEADERBOARD_UPDATED
        );

        log.info("Leaderboard recalculation complete. {} entries ranked", sorted.size());
    }
}
