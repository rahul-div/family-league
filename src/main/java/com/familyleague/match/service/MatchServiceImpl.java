package com.familyleague.match.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.common.exception.BadRequestException;
import com.familyleague.common.exception.ConflictException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.entity.Season;
import com.familyleague.league.repository.SeasonRepository;
import com.familyleague.match.dto.CreateMatchRequest;
import com.familyleague.match.dto.MatchResponse;
import com.familyleague.match.dto.UpdateMatchRequest;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchStatus;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.SeasonTeamRepository;
import com.familyleague.team.repository.TeamRepository;

@Service
@Transactional(readOnly = true)
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;
    private final SeasonTeamRepository seasonTeamRepository;

    public MatchServiceImpl(MatchRepository matchRepository, SeasonRepository seasonRepository,
                            TeamRepository teamRepository, SeasonTeamRepository seasonTeamRepository) {
        this.matchRepository = matchRepository;
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
        this.seasonTeamRepository = seasonTeamRepository;
    }

    @Override
    @Transactional
    public MatchResponse create(CreateMatchRequest request) {
        Season season = seasonRepository.findById(request.seasonId())
                .orElseThrow(() -> ResourceNotFoundException.of("Season", request.seasonId()));

        if (request.homeTeamId().equals(request.awayTeamId())) {
            throw new BadRequestException("Home team and away team cannot be the same");
        }

        Team homeTeam = teamRepository.findById(request.homeTeamId())
                .orElseThrow(() -> ResourceNotFoundException.of("Team", request.homeTeamId()));
        Team awayTeam = teamRepository.findById(request.awayTeamId())
                .orElseThrow(() -> ResourceNotFoundException.of("Team", request.awayTeamId()));

        if (!seasonTeamRepository.existsBySeasonIdAndTeamIdAndIsDeletedFalse(request.seasonId(), request.homeTeamId())) {
            throw new BadRequestException("Home team is not enrolled in this season");
        }
        if (!seasonTeamRepository.existsBySeasonIdAndTeamIdAndIsDeletedFalse(request.seasonId(), request.awayTeamId())) {
            throw new BadRequestException("Away team is not enrolled in this season");
        }

        int lockHours = season.getMatchPredictionLockHours() != null ? season.getMatchPredictionLockHours() : 1;
        Instant lockTime = request.scheduledAt().minus(lockHours, ChronoUnit.HOURS);

        Match match = Match.builder()
                .season(season)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchNumber(request.matchNumber())
                .scheduledAt(request.scheduledAt())
                .venue(request.venue())
                .predictionLockTime(lockTime)
                .build();

        match = matchRepository.save(match);

        // Update season's firstMatchAt if this match is earlier
        if (season.getFirstMatchAt() == null || request.scheduledAt().isBefore(season.getFirstMatchAt())) {
            season.setFirstMatchAt(request.scheduledAt());
            seasonRepository.save(season);
        }

        return MatchResponse.from(match);
    }

    @Override
    public PagedResponse<MatchResponse> getBySeason(UUID seasonId, MatchStatus status, Pageable pageable) {
        Page<Match> page;
        if (status != null) {
            page = matchRepository.findBySeasonIdAndStatus(seasonId, status, pageable);
        } else {
            page = matchRepository.findBySeasonId(seasonId, pageable);
        }
        List<MatchResponse> content = page.getContent().stream()
                .map(MatchResponse::from).toList();
        return PagedResponse.from(page, content);
    }

    @Override
    public MatchResponse getById(UUID id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Match", id));
        return MatchResponse.from(match);
    }

    @Override
    @Transactional
    public MatchResponse update(UUID id, UpdateMatchRequest request) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Match", id));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new ConflictException("Only SCHEDULED matches can be updated");
        }

        match.setScheduledAt(request.scheduledAt());
        if (request.venue() != null) {
            match.setVenue(request.venue());
        }

        int lockHours = match.getSeason().getMatchPredictionLockHours() != null
                ? match.getSeason().getMatchPredictionLockHours() : 1;
        match.setPredictionLockTime(request.scheduledAt().minus(lockHours, ChronoUnit.HOURS));

        return MatchResponse.from(matchRepository.save(match));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Match", id));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new ConflictException("Only SCHEDULED matches can be deleted");
        }

        match.softDelete(SecurityUser.currentUserId());
        matchRepository.save(match);
    }
}
