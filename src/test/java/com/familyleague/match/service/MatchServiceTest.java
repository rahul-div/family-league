package com.familyleague.match.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private SeasonRepository seasonRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private SeasonTeamRepository seasonTeamRepository;

    @InjectMocks private MatchServiceImpl matchService;

    private Season season;
    private Team homeTeam, awayTeam;
    private UUID seasonId, homeId, awayId;

    @BeforeEach
    void setUp() {
        seasonId = UUID.randomUUID();
        homeId = UUID.randomUUID();
        awayId = UUID.randomUUID();

        season = Season.builder().name("S1").matchPredictionLockHours(1).build();
        season.setId(seasonId);
        homeTeam = Team.builder().name("Home").build();
        homeTeam.setId(homeId);
        awayTeam = Team.builder().name("Away").build();
        awayTeam.setId(awayId);
    }

    @Test
    void create_success_calculatesLockTime() {
        Instant scheduledAt = Instant.now().plus(7, ChronoUnit.DAYS);
        CreateMatchRequest request = new CreateMatchRequest(seasonId, homeId, awayId, 1, scheduledAt, "Stadium");

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(teamRepository.findById(homeId)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(awayId)).thenReturn(Optional.of(awayTeam));
        when(seasonTeamRepository.existsBySeasonIdAndTeamIdAndIsDeletedFalse(seasonId, homeId)).thenReturn(true);
        when(seasonTeamRepository.existsBySeasonIdAndTeamIdAndIsDeletedFalse(seasonId, awayId)).thenReturn(true);
        when(matchRepository.save(any())).thenAnswer(i -> {
            Match m = i.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setCreatedAt(Instant.now());
            return m;
        });
        when(seasonRepository.save(any())).thenReturn(season);

        MatchResponse response = matchService.create(request);

        ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        Match saved = captor.getValue();

        // Lock time = scheduledAt - 1 hour
        assertThat(saved.getPredictionLockTime()).isEqualTo(scheduledAt.minus(1, ChronoUnit.HOURS));
        assertThat(saved.getVenue()).isEqualTo("Stadium");
    }

    @Test
    void create_sameTeam_throws() {
        CreateMatchRequest request = new CreateMatchRequest(seasonId, homeId, homeId, 1, Instant.now(), null);
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));

        assertThatThrownBy(() -> matchService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("same");
    }

    @Test
    void create_teamNotInSeason_throws() {
        CreateMatchRequest request = new CreateMatchRequest(seasonId, homeId, awayId, 1, Instant.now(), null);
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(teamRepository.findById(homeId)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(awayId)).thenReturn(Optional.of(awayTeam));
        when(seasonTeamRepository.existsBySeasonIdAndTeamIdAndIsDeletedFalse(seasonId, homeId)).thenReturn(false);

        assertThatThrownBy(() -> matchService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not enrolled");
    }

    @Test
    void create_seasonNotFound_throws() {
        CreateMatchRequest request = new CreateMatchRequest(seasonId, homeId, awayId, 1, Instant.now(), null);
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_onlyScheduledMatches() {
        Match match = Match.builder().season(season).homeTeam(homeTeam).awayTeam(awayTeam)
                .status(MatchStatus.COMPLETED).build();
        match.setId(UUID.randomUUID());
        when(matchRepository.findById(any())).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.update(match.getId(),
                new UpdateMatchRequest(Instant.now(), null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SCHEDULED");
    }

    @Test
    void delete_onlyScheduledMatches() {
        Match match = Match.builder().season(season).homeTeam(homeTeam).awayTeam(awayTeam)
                .status(MatchStatus.IN_PROGRESS).build();
        match.setId(UUID.randomUUID());
        when(matchRepository.findById(any())).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.delete(match.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SCHEDULED");
    }

    @Test
    void create_updatesSeasonFirstMatchAt() {
        Instant earlyTime = Instant.now().plus(2, ChronoUnit.DAYS);
        season.setFirstMatchAt(Instant.now().plus(10, ChronoUnit.DAYS));
        CreateMatchRequest request = new CreateMatchRequest(seasonId, homeId, awayId, 1, earlyTime, null);

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(teamRepository.findById(homeId)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(awayId)).thenReturn(Optional.of(awayTeam));
        when(seasonTeamRepository.existsBySeasonIdAndTeamIdAndIsDeletedFalse(any(), any())).thenReturn(true);
        when(matchRepository.save(any())).thenAnswer(i -> {
            Match m = i.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setCreatedAt(Instant.now());
            return m;
        });
        when(seasonRepository.save(any())).thenReturn(season);

        matchService.create(request);

        // firstMatchAt should be updated to the earlier time
        assertThat(season.getFirstMatchAt()).isEqualTo(earlyTime);
        verify(seasonRepository).save(season);
    }
}
