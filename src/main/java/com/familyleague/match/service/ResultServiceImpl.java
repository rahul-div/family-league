package com.familyleague.match.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.exception.BadRequestException;
import com.familyleague.common.exception.ConflictException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.entity.Season;
import com.familyleague.league.entity.SeasonStatus;
import com.familyleague.match.dto.MatchResultResponse;
import com.familyleague.match.dto.PublishResultRequest;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchResult;
import com.familyleague.match.entity.MatchStatus;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.match.repository.MatchResultRepository;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.service.EmailNotificationService;
import com.familyleague.player.entity.Player;
import com.familyleague.player.repository.PlayerRepository;
import com.familyleague.scoring.service.LeaderboardService;
import com.familyleague.scoring.service.ScoreCalculationService;
import com.familyleague.standing.service.StandingService;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.TeamRepository;

@Service
@Transactional
public class ResultServiceImpl implements ResultService {

    private static final Logger log = LoggerFactory.getLogger(ResultServiceImpl.class);

    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final StandingService standingService;
    private final ScoreCalculationService scoreCalculationService;
    private final LeaderboardService leaderboardService;
    private final EmailNotificationService emailNotificationService;

    public ResultServiceImpl(MatchRepository matchRepository, MatchResultRepository matchResultRepository,
                              TeamRepository teamRepository, PlayerRepository playerRepository,
                              StandingService standingService, ScoreCalculationService scoreCalculationService,
                              LeaderboardService leaderboardService,
                              EmailNotificationService emailNotificationService) {
        this.matchRepository = matchRepository;
        this.matchResultRepository = matchResultRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.standingService = standingService;
        this.scoreCalculationService = scoreCalculationService;
        this.leaderboardService = leaderboardService;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public MatchResultResponse publishMatchResult(PublishResultRequest request) {
        Match match = matchRepository.findById(request.matchId())
                .orElseThrow(() -> ResourceNotFoundException.of("Match", request.matchId()));

        Season season = match.getSeason();
        if (season.getStatus() == SeasonStatus.UPCOMING || season.getStatus() == SeasonStatus.CLOSED) {
            throw new BadRequestException("Cannot publish results for season with status: " + season.getStatus());
        }

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new ConflictException("Match result already published");
        }

        if (matchResultRepository.existsByMatchId(request.matchId())) {
            throw new ConflictException("Match result already published");
        }

        UUID homeTeamId = match.getHomeTeam().getId();
        UUID awayTeamId = match.getAwayTeam().getId();

        // Validate winner team
        Team winnerTeam = null;
        if (!request.tie() && request.winnerTeamId() != null) {
            if (!request.winnerTeamId().equals(homeTeamId) && !request.winnerTeamId().equals(awayTeamId)) {
                throw new BadRequestException("Winner team must be one of the match teams");
            }
            winnerTeam = teamRepository.findById(request.winnerTeamId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Team", request.winnerTeamId()));
        }

        // Validate toss winner
        Team tossWinnerTeam = null;
        if (request.tossWinnerTeamId() != null) {
            if (!request.tossWinnerTeamId().equals(homeTeamId) && !request.tossWinnerTeamId().equals(awayTeamId)) {
                throw new BadRequestException("Toss winner must be one of the match teams");
            }
            tossWinnerTeam = teamRepository.findById(request.tossWinnerTeamId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Team", request.tossWinnerTeamId()));
        }

        // Validate POTM
        Player potm = null;
        if (request.playerOfMatchId() != null) {
            potm = playerRepository.findById(request.playerOfMatchId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Player", request.playerOfMatchId()));
            UUID potmTeamId = potm.getTeam().getId();
            if (!potmTeamId.equals(homeTeamId) && !potmTeamId.equals(awayTeamId)) {
                throw new BadRequestException("Player of the match must belong to one of the match teams");
            }
        }

        // Save result
        MatchResult result = MatchResult.builder()
                .match(match)
                .winnerTeam(winnerTeam)
                .tossWinnerTeam(tossWinnerTeam)
                .playerOfMatch(potm)
                .isTie(request.tie())
                .publishedAt(Instant.now())
                .publishedBy(SecurityUser.currentUserId())
                .build();
        result = matchResultRepository.save(result);

        // Update match status
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        // Update league standings
        standingService.updateStandingsAfterResult(result);

        // Trigger async score calculation + leaderboard recalc after commit
        final UUID matchId = match.getId();
        final UUID seasonId = season.getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    scoreCalculationService.calculateMatchScores(matchId);
                    leaderboardService.recalculateLeaderboard(seasonId);
                }
            });
        } else {
            // Fallback for non-transactional context (e.g., unit tests)
            scoreCalculationService.calculateMatchScores(matchId);
            leaderboardService.recalculateLeaderboard(seasonId);
        }

        // Notify admins
        emailNotificationService.notifyAdmins(
                "Match Result Published: " + match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName(),
                "Result published for match #" + match.getMatchNumber(),
                NotificationEventType.RESULT_PUBLISHED
        );

        log.info("Match result published for match {}", matchId);
        return MatchResultResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResultResponse getMatchResult(UUID matchId) {
        MatchResult result = matchResultRepository.findByMatchId(matchId)
                .orElseThrow(() -> ResourceNotFoundException.of("MatchResult for match", matchId));
        return MatchResultResponse.from(result);
    }
}
