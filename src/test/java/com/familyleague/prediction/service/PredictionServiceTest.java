package com.familyleague.prediction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.exception.BadRequestException;
import com.familyleague.common.exception.ForbiddenException;
import com.familyleague.league.entity.Season;
import com.familyleague.league.entity.SeasonStatus;
import com.familyleague.league.repository.SeasonRepository;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchStatus;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.player.repository.PlayerRepository;
import com.familyleague.prediction.dto.SubmitLeaguePredictionRequest;
import com.familyleague.prediction.dto.SubmitMatchPredictionRequest;
import com.familyleague.prediction.entity.MatchPrediction;
import com.familyleague.prediction.repository.LeaguePredictionRepository;
import com.familyleague.prediction.repository.MatchPredictionRepository;
import com.familyleague.team.entity.SeasonTeam;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.SeasonTeamRepository;
import com.familyleague.team.repository.TeamRepository;
import com.familyleague.user.entity.User;
import com.familyleague.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock private MatchPredictionRepository matchPredictionRepository;
    @Mock private LeaguePredictionRepository leaguePredictionRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private SeasonRepository seasonRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private UserRepository userRepository;
    @Mock private SeasonTeamRepository seasonTeamRepository;

    @InjectMocks private PredictionServiceImpl predictionService;

    private UUID userId;
    private Match testMatch;
    private Team homeTeam;
    private Team awayTeam;
    private Season testSeason;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        homeTeam = Team.builder().name("Home").build();
        homeTeam.setId(UUID.randomUUID());
        awayTeam = Team.builder().name("Away").build();
        awayTeam.setId(UUID.randomUUID());

        testSeason = Season.builder()
                .name("Test Season")
                .status(SeasonStatus.PREDICTION_OPEN)
                .predictionLockedAt(Instant.now().plus(2, ChronoUnit.DAYS))
                .build();
        testSeason.setId(UUID.randomUUID());

        testMatch = Match.builder()
                .season(testSeason)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchNumber(1)
                .scheduledAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .predictionLockTime(Instant.now().plus(23, ChronoUnit.HOURS))
                .status(MatchStatus.SCHEDULED)
                .build();
        testMatch.setId(UUID.randomUUID());
    }

    @Test
    void submitMatchPrediction_locked_throws() {
        testMatch.setPredictionLockTime(Instant.now().minus(1, ChronoUnit.HOURS));

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(userId);
            when(matchRepository.findById(any())).thenReturn(Optional.of(testMatch));

            SubmitMatchPredictionRequest request = new SubmitMatchPredictionRequest(
                    homeTeam.getId(), homeTeam.getId(), null);

            assertThatThrownBy(() -> predictionService.submitMatchPrediction(testMatch.getId(), request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Test
    void submitMatchPrediction_invalidTeam_throws() {
        UUID randomTeamId = UUID.randomUUID();

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(userId);
            when(matchRepository.findById(any())).thenReturn(Optional.of(testMatch));

            SubmitMatchPredictionRequest request = new SubmitMatchPredictionRequest(
                    randomTeamId, null, null);

            assertThatThrownBy(() -> predictionService.submitMatchPrediction(testMatch.getId(), request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("match teams");
        }
    }

    @Test
    void submitMatchPrediction_success() {
        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(userId);
            when(matchRepository.findById(any())).thenReturn(Optional.of(testMatch));
            when(teamRepository.getReferenceById(homeTeam.getId())).thenReturn(homeTeam);
            when(userRepository.getReferenceById(userId)).thenReturn(User.builder().username("test").build());
            when(matchPredictionRepository.findByMatchIdAndUserId(any(), any())).thenReturn(Optional.empty());

            MatchPrediction saved = MatchPrediction.builder()
                    .match(testMatch).user(User.builder().username("test").build())
                    .predictedWinnerTeam(homeTeam).build();
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(Instant.now());
            when(matchPredictionRepository.save(any())).thenReturn(saved);

            SubmitMatchPredictionRequest request = new SubmitMatchPredictionRequest(
                    homeTeam.getId(), null, null);

            var response = predictionService.submitMatchPrediction(testMatch.getId(), request);
            assertThat(response).isNotNull();
            verify(matchPredictionRepository).save(any());
        }
    }

    @Test
    void submitLeaguePredictions_locked_throws() {
        testSeason.setStatus(SeasonStatus.PREDICTION_LOCKED);

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(userId);
            when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));

            SubmitLeaguePredictionRequest request = new SubmitLeaguePredictionRequest(List.of());

            assertThatThrownBy(() -> predictionService.submitLeaguePredictions(testSeason.getId(), request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Test
    void submitLeaguePredictions_wrongTeamCount_throws() {
        Team team1 = Team.builder().name("T1").build();
        team1.setId(UUID.randomUUID());
        Team team2 = Team.builder().name("T2").build();
        team2.setId(UUID.randomUUID());

        SeasonTeam st1 = SeasonTeam.builder().season(testSeason).team(team1).build();
        SeasonTeam st2 = SeasonTeam.builder().season(testSeason).team(team2).build();

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(userId);
            when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));
            when(seasonTeamRepository.findBySeasonIdAndIsDeletedFalse(any())).thenReturn(List.of(st1, st2));

            // Only submitting 1 prediction for 2 teams
            SubmitLeaguePredictionRequest request = new SubmitLeaguePredictionRequest(
                    List.of(new SubmitLeaguePredictionRequest.Entry(team1.getId(), 1)));

            assertThatThrownBy(() -> predictionService.submitLeaguePredictions(testSeason.getId(), request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("all");
        }
    }

    @Test
    void submitLeaguePredictions_duplicatePosition_throws() {
        Team team1 = Team.builder().name("T1").build();
        team1.setId(UUID.randomUUID());
        Team team2 = Team.builder().name("T2").build();
        team2.setId(UUID.randomUUID());

        SeasonTeam st1 = SeasonTeam.builder().season(testSeason).team(team1).build();
        SeasonTeam st2 = SeasonTeam.builder().season(testSeason).team(team2).build();

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(userId);
            when(seasonRepository.findById(any())).thenReturn(Optional.of(testSeason));
            when(seasonTeamRepository.findBySeasonIdAndIsDeletedFalse(any())).thenReturn(List.of(st1, st2));

            // Both at position 1
            SubmitLeaguePredictionRequest request = new SubmitLeaguePredictionRequest(List.of(
                    new SubmitLeaguePredictionRequest.Entry(team1.getId(), 1),
                    new SubmitLeaguePredictionRequest.Entry(team2.getId(), 1)));

            assertThatThrownBy(() -> predictionService.submitLeaguePredictions(testSeason.getId(), request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Duplicate position");
        }
    }

    @Test
    void getAllMatchPredictions_beforeLock_forbidden() {
        // Match not yet locked
        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(userId);
            mocked.when(SecurityUser::isAdmin).thenReturn(false);
            when(matchRepository.findById(any())).thenReturn(Optional.of(testMatch));

            assertThatThrownBy(() -> predictionService.getAllMatchPredictions(testMatch.getId(), null))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("visible after");
        }
    }
}
