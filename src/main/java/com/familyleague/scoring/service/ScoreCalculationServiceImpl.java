package com.familyleague.scoring.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchResult;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.match.repository.MatchResultRepository;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.service.EmailNotificationService;
import com.familyleague.prediction.entity.LeaguePrediction;
import com.familyleague.prediction.entity.MatchPrediction;
import com.familyleague.prediction.repository.LeaguePredictionRepository;
import com.familyleague.prediction.repository.MatchPredictionRepository;
import com.familyleague.scoring.entity.MatchScoreDetail;
import com.familyleague.scoring.entity.SeasonScoreDetail;
import com.familyleague.scoring.repository.MatchScoreDetailRepository;
import com.familyleague.scoring.repository.SeasonScoreDetailRepository;
import com.familyleague.standing.entity.LeagueStanding;
import com.familyleague.standing.repository.LeagueStandingRepository;

@Service
@Transactional
public class ScoreCalculationServiceImpl implements ScoreCalculationService {

    private static final Logger log = LoggerFactory.getLogger(ScoreCalculationServiceImpl.class);

    private final MatchPredictionRepository matchPredictionRepository;
    private final MatchResultRepository matchResultRepository;
    private final MatchRepository matchRepository;
    private final MatchScoreDetailRepository matchScoreDetailRepository;
    private final LeaguePredictionRepository leaguePredictionRepository;
    private final SeasonScoreDetailRepository seasonScoreDetailRepository;
    private final LeagueStandingRepository leagueStandingRepository;
    private final EmailNotificationService emailNotificationService;

    public ScoreCalculationServiceImpl(MatchPredictionRepository matchPredictionRepository,
                                        MatchResultRepository matchResultRepository,
                                        MatchRepository matchRepository,
                                        MatchScoreDetailRepository matchScoreDetailRepository,
                                        LeaguePredictionRepository leaguePredictionRepository,
                                        SeasonScoreDetailRepository seasonScoreDetailRepository,
                                        LeagueStandingRepository leagueStandingRepository,
                                        EmailNotificationService emailNotificationService) {
        this.matchPredictionRepository = matchPredictionRepository;
        this.matchResultRepository = matchResultRepository;
        this.matchRepository = matchRepository;
        this.matchScoreDetailRepository = matchScoreDetailRepository;
        this.leaguePredictionRepository = leaguePredictionRepository;
        this.seasonScoreDetailRepository = seasonScoreDetailRepository;
        this.leagueStandingRepository = leagueStandingRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public void calculateMatchScores(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> ResourceNotFoundException.of("Match", matchId));
        MatchResult result = matchResultRepository.findByMatchId(matchId)
                .orElseThrow(() -> ResourceNotFoundException.of("MatchResult for match", matchId));

        UUID seasonId = match.getSeason().getId();
        List<MatchPrediction> predictions = matchPredictionRepository.findAllByMatchId(matchId);

        UUID winnerTeamId = result.getWinnerTeam() != null ? result.getWinnerTeam().getId() : null;
        UUID tossWinnerId = result.getTossWinnerTeam() != null ? result.getTossWinnerTeam().getId() : null;
        UUID potmId = result.getPlayerOfMatch() != null ? result.getPlayerOfMatch().getId() : null;

        for (MatchPrediction prediction : predictions) {
            int points = 0;

            // Winner check
            boolean winnerCorrect = false;
            if (result.isTie()) {
                // Tie: any prediction of either team counts as correct
                winnerCorrect = prediction.getPredictedWinnerTeam() != null;
            } else if (winnerTeamId != null && prediction.getPredictedWinnerTeam() != null) {
                winnerCorrect = winnerTeamId.equals(prediction.getPredictedWinnerTeam().getId());
            }
            if (winnerCorrect) points++;

            // Toss winner check
            boolean tossCorrect = false;
            if (tossWinnerId != null && prediction.getPredictedTossWinnerTeam() != null) {
                tossCorrect = tossWinnerId.equals(prediction.getPredictedTossWinnerTeam().getId());
            }
            if (tossCorrect) points++;

            // POTM check
            boolean potmCorrect = false;
            if (potmId != null && prediction.getPredictedPotmPlayer() != null) {
                potmCorrect = potmId.equals(prediction.getPredictedPotmPlayer().getId());
            }
            if (potmCorrect) points++;

            MatchScoreDetail scoreDetail = MatchScoreDetail.builder()
                    .match(match)
                    .user(prediction.getUser())
                    .season(match.getSeason())
                    .winnerCorrect(winnerCorrect)
                    .tossWinnerCorrect(tossCorrect)
                    .potmCorrect(potmCorrect)
                    .totalMatchPoints(points)
                    .calculatedAt(Instant.now())
                    .build();
            matchScoreDetailRepository.save(scoreDetail);

            // Notify user about their score
            emailNotificationService.queueEmail(
                    prediction.getUser().getEmail(),
                    prediction.getUser().getId(),
                    "Match Score Update - " + match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName(),
                    "You scored " + points + " points! Winner: " + (winnerCorrect ? "✓" : "✗")
                            + ", Toss: " + (tossCorrect ? "✓" : "✗")
                            + ", POTM: " + (potmCorrect ? "✓" : "✗"),
                    NotificationEventType.RESULT_PUBLISHED,
                    matchId
            );
        }

        log.info("Calculated match scores for match {} ({} predictions)", matchId, predictions.size());
    }

    @Override
    public void calculateSeasonScores(UUID seasonId) {
        // Clear previous season score details
        seasonScoreDetailRepository.deleteBySeasonId(seasonId);

        // Get final standings
        List<LeagueStanding> standings = leagueStandingRepository.findBySeasonIdOrderByCurrentPosition(seasonId);

        // Get all users who submitted league predictions
        List<UUID> userIds = leaguePredictionRepository.findUserIdsWhoSubmittedPrediction(seasonId);

        for (UUID userId : userIds) {
            List<LeaguePrediction> predictions = leaguePredictionRepository
                    .findBySeasonIdAndUserId(seasonId, userId);

            for (LeaguePrediction prediction : predictions) {
                Integer actualPosition = standings.stream()
                        .filter(s -> s.getTeam().getId().equals(prediction.getTeam().getId()))
                        .map(LeagueStanding::getCurrentPosition)
                        .findFirst().orElse(null);

                int pointsEarned = 0;
                if (actualPosition != null && actualPosition.equals(prediction.getPredictedPosition())) {
                    pointsEarned = 1;
                }

                SeasonScoreDetail detail = SeasonScoreDetail.builder()
                        .season(prediction.getSeason())
                        .user(prediction.getUser())
                        .team(prediction.getTeam())
                        .predictedPosition(prediction.getPredictedPosition())
                        .actualPosition(actualPosition)
                        .pointsEarned(pointsEarned)
                        .calculatedAt(Instant.now())
                        .build();
                seasonScoreDetailRepository.save(detail);
            }
        }

        log.info("Calculated season scores for season {} ({} users)", seasonId, userIds.size());
    }
}
