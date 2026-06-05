package com.familyleague.league.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.familyleague.common.exception.BadRequestException;
import com.familyleague.league.dto.SeasonResponse;
import com.familyleague.league.entity.League;
import com.familyleague.league.entity.Season;
import com.familyleague.league.entity.SeasonStatus;
import com.familyleague.league.repository.LeagueRepository;
import com.familyleague.league.repository.SeasonRepository;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.scoring.service.LeaderboardService;
import com.familyleague.scoring.service.ScoreCalculationService;
import com.familyleague.standing.repository.LeagueStandingRepository;
import com.familyleague.standing.service.StandingService;
import com.familyleague.team.repository.SeasonTeamRepository;

@ExtendWith(MockitoExtension.class)
class SeasonServiceTest {

    @Mock private SeasonRepository seasonRepository;
    @Mock private LeagueRepository leagueRepository;
    @Mock private SeasonTeamRepository seasonTeamRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private StandingService standingService;
    @Mock private ScoreCalculationService scoreCalculationService;
    @Mock private LeaderboardService leaderboardService;
    @Mock private LeagueStandingRepository leagueStandingRepository;

    @InjectMocks private SeasonServiceImpl seasonService;

    private Season testSeason;
    private League testLeague;

    @BeforeEach
    void setUp() {
        testLeague = League.builder().name("Test League").build();
        testLeague.setId(UUID.randomUUID());

        testSeason = Season.builder()
                .league(testLeague)
                .name("Season 1")
                .status(SeasonStatus.UPCOMING)
                .leaguePredictionLockHours(4)
                .matchPredictionLockHours(1)
                .firstMatchAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
        testSeason.setId(UUID.randomUUID());
    }

    @Test
    void openSeason_success() {
        when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));
        when(seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(any())).thenReturn(3L);
        when(matchRepository.countBySeasonIdAndIsDeletedFalse(any())).thenReturn(2L);
        when(seasonRepository.save(any())).thenReturn(testSeason);

        SeasonResponse response = seasonService.openSeason(testSeason.getId());

        assertThat(response.status()).isEqualTo("PREDICTION_OPEN");
        verify(standingService).initializeStandings(any());
    }

    @Test
    void openSeason_notUpcoming_throws() {
        testSeason.setStatus(SeasonStatus.PREDICTION_OPEN);
        when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));

        assertThatThrownBy(() -> seasonService.openSeason(testSeason.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("UPCOMING");
    }

    @Test
    void openSeason_lessThan2Teams_throws() {
        when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));
        when(seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(any())).thenReturn(1L);

        assertThatThrownBy(() -> seasonService.openSeason(testSeason.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("2 teams");
    }

    @Test
    void openSeason_noMatches_throws() {
        when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));
        when(seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(any())).thenReturn(3L);
        when(matchRepository.countBySeasonIdAndIsDeletedFalse(any())).thenReturn(0L);

        assertThatThrownBy(() -> seasonService.openSeason(testSeason.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("match");
    }

    @Test
    void openSeason_noFirstMatchAt_calculatesFromMatches() {
        testSeason.setFirstMatchAt(null);
        Instant firstMatch = Instant.now().plus(5, ChronoUnit.DAYS);
        when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));
        when(seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(any())).thenReturn(3L);
        when(matchRepository.countBySeasonIdAndIsDeletedFalse(any())).thenReturn(2L);
        when(matchRepository.findFirstMatchTime(any())).thenReturn(Optional.of(firstMatch));
        when(seasonRepository.save(any())).thenReturn(testSeason);

        seasonService.openSeason(testSeason.getId());

        assertThat(testSeason.getFirstMatchAt()).isEqualTo(firstMatch);
        assertThat(testSeason.getPredictionLockedAt()).isEqualTo(firstMatch.minus(4, ChronoUnit.HOURS));
    }

    @Test
    void closeSeason_success() {
        testSeason.setStatus(SeasonStatus.COMPLETED);
        when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));
        when(seasonRepository.save(any())).thenReturn(testSeason);
        when(seasonTeamRepository.countBySeasonIdAndIsDeletedFalse(any())).thenReturn(3L);

        SeasonResponse response = seasonService.closeSeason(testSeason.getId());

        assertThat(response.status()).isEqualTo("CLOSED");
        assertThat(testSeason.getClosedAt()).isNotNull();
    }

    @Test
    void closeSeason_notCompleted_throws() {
        testSeason.setStatus(SeasonStatus.PREDICTION_OPEN);
        when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));

        assertThatThrownBy(() -> seasonService.closeSeason(testSeason.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("COMPLETED");
    }
}
