package com.familyleague.standing.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.entity.Season;
import com.familyleague.league.repository.SeasonRepository;
import com.familyleague.match.entity.MatchResult;
import com.familyleague.standing.dto.LeagueStandingResponse;
import com.familyleague.standing.entity.LeagueStanding;
import com.familyleague.standing.repository.LeagueStandingRepository;
import com.familyleague.team.entity.SeasonTeam;
import com.familyleague.team.repository.SeasonTeamRepository;

@Service
@Transactional
public class StandingServiceImpl implements StandingService {

    private final LeagueStandingRepository leagueStandingRepository;
    private final SeasonTeamRepository seasonTeamRepository;
    private final SeasonRepository seasonRepository;

    public StandingServiceImpl(LeagueStandingRepository leagueStandingRepository,
                                SeasonTeamRepository seasonTeamRepository,
                                SeasonRepository seasonRepository) {
        this.leagueStandingRepository = leagueStandingRepository;
        this.seasonTeamRepository = seasonTeamRepository;
        this.seasonRepository = seasonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueStandingResponse> getStandings(UUID seasonId) {
        return leagueStandingRepository.findBySeasonIdOrderByCurrentPosition(seasonId).stream()
                .map(LeagueStandingResponse::from)
                .toList();
    }

    @Override
    public void initializeStandings(UUID seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", seasonId));

        List<SeasonTeam> seasonTeams = seasonTeamRepository.findBySeasonIdAndIsDeletedFalse(seasonId);
        for (SeasonTeam st : seasonTeams) {
            if (leagueStandingRepository.findBySeasonIdAndTeamId(seasonId, st.getTeam().getId()).isEmpty()) {
                LeagueStanding standing = LeagueStanding.builder()
                        .season(season)
                        .team(st.getTeam())
                        .build();
                leagueStandingRepository.save(standing);
            }
        }
    }

    @Override
    public void updateStandingsAfterResult(MatchResult result) {
        UUID seasonId = result.getMatch().getSeason().getId();
        UUID homeTeamId = result.getMatch().getHomeTeam().getId();
        UUID awayTeamId = result.getMatch().getAwayTeam().getId();

        LeagueStanding homeSt = getOrCreateStanding(seasonId, result.getMatch().getSeason(),
                result.getMatch().getHomeTeam(), homeTeamId);
        LeagueStanding awaySt = getOrCreateStanding(seasonId, result.getMatch().getSeason(),
                result.getMatch().getAwayTeam(), awayTeamId);

        homeSt.setMatchesPlayed(homeSt.getMatchesPlayed() + 1);
        awaySt.setMatchesPlayed(awaySt.getMatchesPlayed() + 1);

        if (result.isTie()) {
            homeSt.setDraws(homeSt.getDraws() + 1);
            awaySt.setDraws(awaySt.getDraws() + 1);
            homeSt.setPointsInLeague(homeSt.getPointsInLeague() + 1);
            awaySt.setPointsInLeague(awaySt.getPointsInLeague() + 1);
        } else if (result.getWinnerTeam() != null) {
            UUID winnerId = result.getWinnerTeam().getId();
            if (winnerId.equals(homeTeamId)) {
                homeSt.setWins(homeSt.getWins() + 1);
                homeSt.setPointsInLeague(homeSt.getPointsInLeague() + 2);
                awaySt.setLosses(awaySt.getLosses() + 1);
            } else {
                awaySt.setWins(awaySt.getWins() + 1);
                awaySt.setPointsInLeague(awaySt.getPointsInLeague() + 2);
                homeSt.setLosses(homeSt.getLosses() + 1);
            }
        }

        leagueStandingRepository.save(homeSt);
        leagueStandingRepository.save(awaySt);

        // Recalculate positions for all teams in the season
        recalculatePositions(seasonId);
    }

    private LeagueStanding getOrCreateStanding(UUID seasonId, Season season,
                                                com.familyleague.team.entity.Team team, UUID teamId) {
        return leagueStandingRepository.findBySeasonIdAndTeamId(seasonId, teamId)
                .orElseGet(() -> {
                    LeagueStanding standing = LeagueStanding.builder()
                            .season(season)
                            .team(team)
                            .build();
                    return leagueStandingRepository.save(standing);
                });
    }

    private void recalculatePositions(UUID seasonId) {
        List<LeagueStanding> standings = leagueStandingRepository.findBySeasonId(seasonId);
        standings.sort(Comparator.comparingInt(LeagueStanding::getPointsInLeague).reversed()
                .thenComparingInt(LeagueStanding::getWins).reversed());

        AtomicInteger position = new AtomicInteger(1);
        standings.forEach(s -> {
            s.setCurrentPosition(position.getAndIncrement());
            leagueStandingRepository.save(s);
        });
    }
}
