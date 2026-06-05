package com.familyleague.league.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.common.exception.BadRequestException;
import com.familyleague.common.exception.ConflictException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.dto.CreateSeasonRequest;
import com.familyleague.league.dto.PublishFinalStandingsRequest;
import com.familyleague.league.dto.SeasonResponse;
import com.familyleague.league.entity.League;
import com.familyleague.league.entity.Season;
import com.familyleague.league.entity.SeasonStatus;
import com.familyleague.league.repository.LeagueRepository;
import com.familyleague.league.repository.SeasonRepository;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.scoring.service.LeaderboardService;
import com.familyleague.scoring.service.ScoreCalculationService;
import com.familyleague.standing.entity.LeagueStanding;
import com.familyleague.standing.repository.LeagueStandingRepository;
import com.familyleague.standing.service.StandingService;
import com.familyleague.team.entity.SeasonTeam;
import com.familyleague.team.repository.SeasonTeamRepository;

@Service
@Transactional
public class SeasonServiceImpl implements SeasonService {

    private static final Logger log = LoggerFactory.getLogger(SeasonServiceImpl.class);

    private final SeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;
    private final SeasonTeamRepository seasonTeamRepository;
    private final MatchRepository matchRepository;
    private final StandingService standingService;
    private final ScoreCalculationService scoreCalculationService;
    private final LeaderboardService leaderboardService;
    private final LeagueStandingRepository leagueStandingRepository;

    public SeasonServiceImpl(SeasonRepository seasonRepository, LeagueRepository leagueRepository,
                              SeasonTeamRepository seasonTeamRepository, MatchRepository matchRepository,
                              StandingService standingService, ScoreCalculationService scoreCalculationService,
                              LeaderboardService leaderboardService,
                              LeagueStandingRepository leagueStandingRepository) {
        this.seasonRepository = seasonRepository;
        this.leagueRepository = leagueRepository;
        this.seasonTeamRepository = seasonTeamRepository;
        this.matchRepository = matchRepository;
        this.standingService = standingService;
        this.scoreCalculationService = scoreCalculationService;
        this.leaderboardService = leaderboardService;
        this.leagueStandingRepository = leagueStandingRepository;
    }

    @Override
    public SeasonResponse create(CreateSeasonRequest request) {
        League league = leagueRepository.findById(request.leagueId())
                .orElseThrow(() -> ResourceNotFoundException.of("League", request.leagueId()));

        Season season = Season.builder()
                .league(league)
                .name(request.name())
                .seasonNumber(request.seasonNumber())
                .description(request.description())
                .leaguePredictionLockHours(request.leaguePredictionLockHours() != null
                        ? request.leaguePredictionLockHours() : 4)
                .matchPredictionLockHours(request.matchPredictionLockHours() != null
                        ? request.matchPredictionLockHours() : 1)
                .build();

        season = seasonRepository.save(season);
        long teamCount = seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(season.getId());
        return SeasonResponse.from(season, teamCount);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SeasonResponse> getByLeagueId(UUID leagueId, Pageable pageable) {
        Page<Season> page = seasonRepository.findByLeagueId(leagueId, pageable);
        List<SeasonResponse> content = page.getContent().stream()
                .map(s -> SeasonResponse.from(s, seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(s.getId())))
                .toList();
        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonResponse getById(UUID id) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", id));
        long teamCount = seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(id);
        return SeasonResponse.from(season, teamCount);
    }

    @Override
    public SeasonResponse openSeason(UUID id) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", id));

        if (season.getStatus() != SeasonStatus.UPCOMING) {
            throw new BadRequestException("Season can only be opened from UPCOMING status. Current: " + season.getStatus());
        }

        long teamCount = seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(id);
        if (teamCount < 2) {
            throw new BadRequestException("At least 2 teams must be enrolled to open a season");
        }

        long matchCount = matchRepository.countBySeasonIdAndIsDeletedFalse(id);
        if (matchCount == 0) {
            throw new BadRequestException("At least 1 match must be scheduled to open a season");
        }

        if (season.getFirstMatchAt() == null) {
            // Calculate from matches
            season.setFirstMatchAt(matchRepository.findFirstMatchTime(id).orElse(null));
        }

        if (season.getFirstMatchAt() == null) {
            throw new BadRequestException("First match start time must be set to open a season");
        }

        int lockHours = season.getLeaguePredictionLockHours() != null ? season.getLeaguePredictionLockHours() : 4;
        season.setPredictionLockedAt(season.getFirstMatchAt().minus(lockHours, ChronoUnit.HOURS));
        season.setStatus(SeasonStatus.PREDICTION_OPEN);
        season.setStartedAt(Instant.now());

        standingService.initializeStandings(id);

        season = seasonRepository.save(season);
        log.info("Season {} opened. Predictions close at {}", season.getName(), season.getPredictionLockedAt());
        return SeasonResponse.from(season, teamCount);
    }

    @Override
    public SeasonResponse closeSeason(UUID id) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", id));

        if (season.getStatus() != SeasonStatus.COMPLETED) {
            throw new BadRequestException("Season can only be closed from COMPLETED status. Current: " + season.getStatus());
        }

        season.setStatus(SeasonStatus.CLOSED);
        season.setClosedAt(Instant.now());
        season = seasonRepository.save(season);

        long teamCount = seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(id);
        log.info("Season {} closed", season.getName());
        return SeasonResponse.from(season, teamCount);
    }

    @Override
    public void deleteSeason(UUID id) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", id));
        season.softDelete(SecurityUser.currentUserId());
        seasonRepository.save(season);
    }

    @Override
    public SeasonResponse publishFinalStandings(UUID seasonId, PublishFinalStandingsRequest request) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", seasonId));

        if (season.isClosed()) {
            throw new ConflictException("Cannot publish results for a closed season");
        }

        // Update standings with final positions
        for (PublishFinalStandingsRequest.Entry entry : request.standings()) {
            LeagueStanding standing = leagueStandingRepository
                    .findBySeasonIdAndTeamId(seasonId, entry.teamId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Standing for team", entry.teamId()));
            standing.setCurrentPosition(entry.finalPosition());
            leagueStandingRepository.save(standing);

            // Also update SeasonTeam position
            seasonTeamRepository.findBySeasonIdAndTeamIdAndIsDeletedFalse(seasonId, entry.teamId())
                    .ifPresent(st -> {
                        st.setCurrentPosition(entry.finalPosition());
                        seasonTeamRepository.save(st);
                    });
        }

        // Calculate season prediction scores
        scoreCalculationService.calculateSeasonScores(seasonId);

        // Transition to COMPLETED
        season.setStatus(SeasonStatus.COMPLETED);
        season.setCompletedAt(Instant.now());
        season = seasonRepository.save(season);

        // Trigger leaderboard recalculation
        leaderboardService.recalculateLeaderboard(seasonId);

        long teamCount = seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(seasonId);
        log.info("Season {} final standings published and marked COMPLETED", season.getName());
        return SeasonResponse.from(season, teamCount);
    }
}
