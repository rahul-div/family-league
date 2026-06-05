package com.familyleague.match.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.exception.BadRequestException;
import com.familyleague.common.exception.ConflictException;
import com.familyleague.league.entity.Season;
import com.familyleague.league.entity.SeasonStatus;
import com.familyleague.match.dto.MatchResultResponse;
import com.familyleague.match.dto.PublishResultRequest;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchResult;
import com.familyleague.match.entity.MatchStatus;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.match.repository.MatchResultRepository;
import com.familyleague.notification.service.EmailNotificationService;
import com.familyleague.player.entity.Player;
import com.familyleague.player.repository.PlayerRepository;
import com.familyleague.scoring.service.LeaderboardService;
import com.familyleague.scoring.service.ScoreCalculationService;
import com.familyleague.standing.service.StandingService;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private MatchResultRepository matchResultRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private StandingService standingService;
    @Mock private ScoreCalculationService scoreCalculationService;
    @Mock private LeaderboardService leaderboardService;
    @Mock private EmailNotificationService emailNotificationService;

    @InjectMocks private ResultServiceImpl resultService;

    private UUID matchId, homeTeamId, awayTeamId, playerId;
    private Match match;
    private Team homeTeam, awayTeam;
    private Player player;
    private Season season;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        homeTeamId = UUID.randomUUID();
        awayTeamId = UUID.randomUUID();
        playerId = UUID.randomUUID();

        homeTeam = Team.builder().name("Home").build();
        homeTeam.setId(homeTeamId);
        awayTeam = Team.builder().name("Away").build();
        awayTeam.setId(awayTeamId);
        player = Player.builder().name("Star").team(homeTeam).build();
        player.setId(playerId);

        season = Season.builder().name("S1").status(SeasonStatus.IN_PROGRESS).build();
        season.setId(UUID.randomUUID());

        match = Match.builder().season(season).homeTeam(homeTeam).awayTeam(awayTeam)
                .matchNumber(1).status(MatchStatus.SCHEDULED).build();
        match.setId(matchId);
    }

    @Test
    void publishResult_success() {
        PublishResultRequest request = new PublishResultRequest(matchId, homeTeamId, awayTeamId, playerId, false);

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(UUID.randomUUID());
            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchResultRepository.existsByMatchId(matchId)).thenReturn(false);
            when(teamRepository.findById(homeTeamId)).thenReturn(Optional.of(homeTeam));
            when(teamRepository.findById(awayTeamId)).thenReturn(Optional.of(awayTeam));
            when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
            when(matchResultRepository.save(any())).thenAnswer(i -> {
                MatchResult r = i.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });
            when(matchRepository.save(any())).thenReturn(match);

            MatchResultResponse response = resultService.publishMatchResult(request);

            // Match status should be COMPLETED
            assertThat(match.getStatus()).isEqualTo(MatchStatus.COMPLETED);
            verify(standingService).updateStandingsAfterResult(any());
        }
    }

    @Test
    void publishResult_alreadyCompleted_throws() {
        match.setStatus(MatchStatus.COMPLETED);
        PublishResultRequest request = new PublishResultRequest(matchId, homeTeamId, null, null, false);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> resultService.publishMatchResult(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already published");
    }

    @Test
    void publishResult_seasonUpcoming_throws() {
        season.setStatus(SeasonStatus.UPCOMING);
        PublishResultRequest request = new PublishResultRequest(matchId, homeTeamId, null, null, false);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> resultService.publishMatchResult(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("status");
    }

    @Test
    void publishResult_seasonClosed_throws() {
        season.setStatus(SeasonStatus.CLOSED);
        PublishResultRequest request = new PublishResultRequest(matchId, homeTeamId, null, null, false);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> resultService.publishMatchResult(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("status");
    }

    @Test
    void publishResult_winnerNotInMatch_throws() {
        UUID randomTeam = UUID.randomUUID();
        PublishResultRequest request = new PublishResultRequest(matchId, randomTeam, null, null, false);

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(UUID.randomUUID());
            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchResultRepository.existsByMatchId(matchId)).thenReturn(false);

            assertThatThrownBy(() -> resultService.publishMatchResult(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("match teams");
        }
    }

    @Test
    void publishResult_potmNotInMatchTeams_throws() {
        Team otherTeam = Team.builder().name("Other").build();
        otherTeam.setId(UUID.randomUUID());
        Player otherPlayer = Player.builder().name("Other").team(otherTeam).build();
        otherPlayer.setId(UUID.randomUUID());

        PublishResultRequest request = new PublishResultRequest(matchId, homeTeamId, null, otherPlayer.getId(), false);

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(UUID.randomUUID());
            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchResultRepository.existsByMatchId(matchId)).thenReturn(false);
            when(teamRepository.findById(homeTeamId)).thenReturn(Optional.of(homeTeam));
            when(playerRepository.findById(otherPlayer.getId())).thenReturn(Optional.of(otherPlayer));

            assertThatThrownBy(() -> resultService.publishMatchResult(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("match teams");
        }
    }

    @Test
    void publishResult_tieResult() {
        PublishResultRequest request = new PublishResultRequest(matchId, null, homeTeamId, null, true);

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(UUID.randomUUID());
            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchResultRepository.existsByMatchId(matchId)).thenReturn(false);
            when(teamRepository.findById(homeTeamId)).thenReturn(Optional.of(homeTeam));
            when(matchResultRepository.save(any())).thenAnswer(i -> {
                MatchResult r = i.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });
            when(matchRepository.save(any())).thenReturn(match);

            MatchResultResponse response = resultService.publishMatchResult(request);

            ArgumentCaptor<MatchResult> captor = ArgumentCaptor.forClass(MatchResult.class);
            verify(matchResultRepository).save(captor.capture());
            assertThat(captor.getValue().isTie()).isTrue();
            assertThat(captor.getValue().getWinnerTeam()).isNull();
        }
    }
}
