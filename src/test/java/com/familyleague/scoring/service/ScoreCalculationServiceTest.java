package com.familyleague.scoring.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.familyleague.league.entity.Season;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchResult;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.match.repository.MatchResultRepository;
import com.familyleague.notification.service.EmailNotificationService;
import com.familyleague.player.entity.Player;
import com.familyleague.prediction.entity.MatchPrediction;
import com.familyleague.prediction.repository.LeaguePredictionRepository;
import com.familyleague.prediction.repository.MatchPredictionRepository;
import com.familyleague.scoring.entity.MatchScoreDetail;
import com.familyleague.scoring.repository.MatchScoreDetailRepository;
import com.familyleague.scoring.repository.SeasonScoreDetailRepository;
import com.familyleague.standing.repository.LeagueStandingRepository;
import com.familyleague.team.entity.Team;
import com.familyleague.user.entity.User;

@ExtendWith(MockitoExtension.class)
class ScoreCalculationServiceTest {

    @Mock private MatchPredictionRepository matchPredictionRepository;
    @Mock private MatchResultRepository matchResultRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private MatchScoreDetailRepository matchScoreDetailRepository;
    @Mock private LeaguePredictionRepository leaguePredictionRepository;
    @Mock private SeasonScoreDetailRepository seasonScoreDetailRepository;
    @Mock private LeagueStandingRepository leagueStandingRepository;
    @Mock private EmailNotificationService emailNotificationService;

    @InjectMocks private ScoreCalculationServiceImpl scoreCalculationService;

    private UUID matchId;
    private Team homeTeam, awayTeam;
    private Player potmPlayer;
    private Match match;
    private Season season;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        homeTeam = Team.builder().name("Home").build();
        homeTeam.setId(UUID.randomUUID());
        awayTeam = Team.builder().name("Away").build();
        awayTeam.setId(UUID.randomUUID());
        potmPlayer = Player.builder().name("Player1").team(homeTeam).build();
        potmPlayer.setId(UUID.randomUUID());

        season = Season.builder().name("Season").build();
        season.setId(UUID.randomUUID());

        match = Match.builder().season(season).homeTeam(homeTeam).awayTeam(awayTeam).matchNumber(1).build();
        match.setId(matchId);
    }

    @Test
    void calculateMatchScores_allCorrect_awards3Points() {
        MatchResult result = MatchResult.builder()
                .match(match).winnerTeam(homeTeam).tossWinnerTeam(awayTeam)
                .playerOfMatch(potmPlayer).isTie(false).build();

        User user = User.builder().username("user1").email("u@e.com").build();
        user.setId(UUID.randomUUID());

        MatchPrediction prediction = MatchPrediction.builder()
                .match(match).user(user)
                .predictedWinnerTeam(homeTeam)
                .predictedTossWinnerTeam(awayTeam)
                .predictedPotmPlayer(potmPlayer)
                .build();

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(matchResultRepository.findByMatchId(matchId)).thenReturn(Optional.of(result));
        when(matchPredictionRepository.findAllByMatchId(matchId)).thenReturn(List.of(prediction));

        scoreCalculationService.calculateMatchScores(matchId);

        ArgumentCaptor<MatchScoreDetail> captor = ArgumentCaptor.forClass(MatchScoreDetail.class);
        verify(matchScoreDetailRepository).save(captor.capture());

        MatchScoreDetail saved = captor.getValue();
        assertThat(saved.isWinnerCorrect()).isTrue();
        assertThat(saved.isTossWinnerCorrect()).isTrue();
        assertThat(saved.isPotmCorrect()).isTrue();
        assertThat(saved.getTotalMatchPoints()).isEqualTo(3);
    }

    @Test
    void calculateMatchScores_partialCorrect_awards1Point() {
        MatchResult result = MatchResult.builder()
                .match(match).winnerTeam(homeTeam).tossWinnerTeam(homeTeam)
                .playerOfMatch(potmPlayer).isTie(false).build();

        User user = User.builder().username("user1").email("u@e.com").build();
        user.setId(UUID.randomUUID());

        MatchPrediction prediction = MatchPrediction.builder()
                .match(match).user(user)
                .predictedWinnerTeam(awayTeam) // wrong
                .predictedTossWinnerTeam(homeTeam) // correct
                .predictedPotmPlayer(null) // no prediction
                .build();

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(matchResultRepository.findByMatchId(matchId)).thenReturn(Optional.of(result));
        when(matchPredictionRepository.findAllByMatchId(matchId)).thenReturn(List.of(prediction));

        scoreCalculationService.calculateMatchScores(matchId);

        ArgumentCaptor<MatchScoreDetail> captor = ArgumentCaptor.forClass(MatchScoreDetail.class);
        verify(matchScoreDetailRepository).save(captor.capture());

        MatchScoreDetail saved = captor.getValue();
        assertThat(saved.isWinnerCorrect()).isFalse();
        assertThat(saved.isTossWinnerCorrect()).isTrue();
        assertThat(saved.isPotmCorrect()).isFalse();
        assertThat(saved.getTotalMatchPoints()).isEqualTo(1);
    }

    @Test
    void calculateMatchScores_tie_winnerCountsAsCorrect() {
        MatchResult result = MatchResult.builder()
                .match(match).winnerTeam(null).tossWinnerTeam(homeTeam)
                .playerOfMatch(null).isTie(true).build();

        User user = User.builder().username("user1").email("u@e.com").build();
        user.setId(UUID.randomUUID());

        MatchPrediction prediction = MatchPrediction.builder()
                .match(match).user(user)
                .predictedWinnerTeam(homeTeam) // tie = any team counts
                .predictedTossWinnerTeam(homeTeam)
                .build();

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(matchResultRepository.findByMatchId(matchId)).thenReturn(Optional.of(result));
        when(matchPredictionRepository.findAllByMatchId(matchId)).thenReturn(List.of(prediction));

        scoreCalculationService.calculateMatchScores(matchId);

        ArgumentCaptor<MatchScoreDetail> captor = ArgumentCaptor.forClass(MatchScoreDetail.class);
        verify(matchScoreDetailRepository).save(captor.capture());

        MatchScoreDetail saved = captor.getValue();
        assertThat(saved.isWinnerCorrect()).isTrue(); // tie = any prediction counts
        assertThat(saved.getTotalMatchPoints()).isEqualTo(2); // winner + toss
    }
}
