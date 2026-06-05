package com.familyleague.prediction.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.common.exception.BadRequestException;
import com.familyleague.common.exception.ForbiddenException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.entity.Season;
import com.familyleague.league.repository.SeasonRepository;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchStatus;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.player.entity.Player;
import com.familyleague.player.repository.PlayerRepository;
import com.familyleague.prediction.dto.HeadToHeadResponse;
import com.familyleague.prediction.dto.LeaguePredictionResponse;
import com.familyleague.prediction.dto.MatchPredictionResponse;
import com.familyleague.prediction.dto.SubmitLeaguePredictionRequest;
import com.familyleague.prediction.dto.SubmitMatchPredictionRequest;
import com.familyleague.prediction.entity.LeaguePrediction;
import com.familyleague.prediction.entity.MatchPrediction;
import com.familyleague.prediction.repository.LeaguePredictionRepository;
import com.familyleague.prediction.repository.MatchPredictionRepository;
import com.familyleague.team.entity.SeasonTeam;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.SeasonTeamRepository;
import com.familyleague.team.repository.TeamRepository;
import com.familyleague.user.entity.User;
import com.familyleague.user.repository.UserRepository;

@Service
@Transactional
public class PredictionServiceImpl implements PredictionService {

    private final MatchPredictionRepository matchPredictionRepository;
    private final LeaguePredictionRepository leaguePredictionRepository;
    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final SeasonTeamRepository seasonTeamRepository;

    public PredictionServiceImpl(MatchPredictionRepository matchPredictionRepository,
                                  LeaguePredictionRepository leaguePredictionRepository,
                                  MatchRepository matchRepository, SeasonRepository seasonRepository,
                                  TeamRepository teamRepository, PlayerRepository playerRepository,
                                  UserRepository userRepository, SeasonTeamRepository seasonTeamRepository) {
        this.matchPredictionRepository = matchPredictionRepository;
        this.leaguePredictionRepository = leaguePredictionRepository;
        this.matchRepository = matchRepository;
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.seasonTeamRepository = seasonTeamRepository;
    }

    @Override
    public MatchPredictionResponse submitMatchPrediction(UUID matchId, SubmitMatchPredictionRequest request) {
        UUID userId = SecurityUser.currentUserId();
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> ResourceNotFoundException.of("Match", matchId));

        // Check prediction window
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new ForbiddenException("Predictions are only allowed for SCHEDULED matches");
        }
        if (match.isPredictionLocked()) {
            throw new ForbiddenException("Prediction window has closed for this match");
        }

        UUID homeTeamId = match.getHomeTeam().getId();
        UUID awayTeamId = match.getAwayTeam().getId();

        // Validate predicted winner team
        Team winnerTeam = null;
        if (request.predictedWinnerTeamId() != null) {
            if (!request.predictedWinnerTeamId().equals(homeTeamId) && !request.predictedWinnerTeamId().equals(awayTeamId)) {
                throw new BadRequestException("Predicted winner must be one of the match teams");
            }
            winnerTeam = teamRepository.getReferenceById(request.predictedWinnerTeamId());
        }

        // Validate predicted toss winner
        Team tossWinner = null;
        if (request.predictedTossWinnerTeamId() != null) {
            if (!request.predictedTossWinnerTeamId().equals(homeTeamId) && !request.predictedTossWinnerTeamId().equals(awayTeamId)) {
                throw new BadRequestException("Predicted toss winner must be one of the match teams");
            }
            tossWinner = teamRepository.getReferenceById(request.predictedTossWinnerTeamId());
        }

        // Validate POTM player
        Player potmPlayer = null;
        if (request.predictedPlayerOfMatchId() != null) {
            potmPlayer = playerRepository.findById(request.predictedPlayerOfMatchId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Player", request.predictedPlayerOfMatchId()));
            UUID playerTeamId = potmPlayer.getTeam().getId();
            if (!playerTeamId.equals(homeTeamId) && !playerTeamId.equals(awayTeamId)) {
                throw new BadRequestException("POTM player must belong to one of the match teams");
            }
        }

        User user = userRepository.getReferenceById(userId);

        // Upsert pattern
        MatchPrediction prediction = matchPredictionRepository.findByMatchIdAndUserId(matchId, userId)
                .orElse(MatchPrediction.builder()
                        .match(match)
                        .user(user)
                        .build());

        prediction.setPredictedWinnerTeam(winnerTeam);
        prediction.setPredictedTossWinnerTeam(tossWinner);
        prediction.setPredictedPotmPlayer(potmPlayer);
        prediction.setSubmittedAt(Instant.now());

        prediction = matchPredictionRepository.save(prediction);
        return MatchPredictionResponse.from(prediction);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchPredictionResponse getMyMatchPrediction(UUID matchId) {
        UUID userId = SecurityUser.currentUserId();
        MatchPrediction prediction = matchPredictionRepository.findByMatchIdAndUserId(matchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("No prediction found for this match"));
        return MatchPredictionResponse.from(prediction);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MatchPredictionResponse> getAllMatchPredictions(UUID matchId, Pageable pageable) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> ResourceNotFoundException.of("Match", matchId));

        // Only visible after lock time (or admin)
        if (!match.isPredictionLocked() && !SecurityUser.isAdmin()) {
            throw new ForbiddenException("Predictions are only visible after the prediction window closes");
        }

        Page<MatchPrediction> page = matchPredictionRepository.findByMatchId(matchId, pageable);
        List<MatchPredictionResponse> content = page.getContent().stream()
                .map(MatchPredictionResponse::from).toList();
        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public HeadToHeadResponse getHeadToHead(UUID matchId, UUID opponentId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> ResourceNotFoundException.of("Match", matchId));

        if (!match.isPredictionLocked() && !SecurityUser.isAdmin()) {
            throw new ForbiddenException("Head-to-head is only available after the prediction window closes");
        }

        UUID userId = SecurityUser.currentUserId();
        MatchPrediction myPrediction = matchPredictionRepository.findByMatchIdAndUserId(matchId, userId)
                .orElse(null);
        MatchPrediction theirPrediction = matchPredictionRepository.findByMatchIdAndUserId(matchId, opponentId)
                .orElse(null);

        return new HeadToHeadResponse(
                myPrediction != null ? MatchPredictionResponse.from(myPrediction) : null,
                theirPrediction != null ? MatchPredictionResponse.from(theirPrediction) : null
        );
    }

    @Override
    public List<LeaguePredictionResponse> submitLeaguePredictions(UUID seasonId,
                                                                    SubmitLeaguePredictionRequest request) {
        UUID userId = SecurityUser.currentUserId();
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", seasonId));

        // Check prediction window
        if (season.isPredictionLocked()) {
            throw new ForbiddenException("League prediction window has closed");
        }
        if (season.getPredictionLockedAt() != null && Instant.now().isAfter(season.getPredictionLockedAt())) {
            throw new ForbiddenException("League prediction window has closed");
        }

        // Validate: must include ALL enrolled teams
        List<SeasonTeam> enrolledTeams = seasonTeamRepository.findBySeasonIdAndIsDeletedFalse(seasonId);
        Set<UUID> enrolledTeamIds = enrolledTeams.stream()
                .map(st -> st.getTeam().getId())
                .collect(Collectors.toSet());

        if (request.predictions().size() != enrolledTeamIds.size()) {
            throw new BadRequestException("Must predict positions for all " + enrolledTeamIds.size()
                    + " enrolled teams. Received: " + request.predictions().size());
        }

        // Validate unique positions and correct range
        Set<Integer> positions = new HashSet<>();
        Set<UUID> predictedTeamIds = new HashSet<>();
        for (SubmitLeaguePredictionRequest.Entry entry : request.predictions()) {
            if (!enrolledTeamIds.contains(entry.teamId())) {
                throw new BadRequestException("Team " + entry.teamId() + " is not enrolled in this season");
            }
            if (!predictedTeamIds.add(entry.teamId())) {
                throw new BadRequestException("Duplicate team in predictions: " + entry.teamId());
            }
            if (entry.predictedPosition() < 1 || entry.predictedPosition() > enrolledTeamIds.size()) {
                throw new BadRequestException("Position must be between 1 and " + enrolledTeamIds.size());
            }
            if (!positions.add(entry.predictedPosition())) {
                throw new BadRequestException("Duplicate position: " + entry.predictedPosition());
            }
        }

        // Delete old predictions and save new ones
        leaguePredictionRepository.deleteBySeasonIdAndUserId(seasonId, userId);

        User user = userRepository.getReferenceById(userId);
        List<LeaguePrediction> savedPredictions = request.predictions().stream().map(entry -> {
            Team team = teamRepository.getReferenceById(entry.teamId());
            LeaguePrediction lp = LeaguePrediction.builder()
                    .season(season)
                    .user(user)
                    .team(team)
                    .predictedPosition(entry.predictedPosition())
                    .submittedAt(Instant.now())
                    .build();
            return leaguePredictionRepository.save(lp);
        }).toList();

        return savedPredictions.stream()
                .map(LeaguePredictionResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaguePredictionResponse> getMyLeaguePredictions(UUID seasonId) {
        UUID userId = SecurityUser.currentUserId();
        return leaguePredictionRepository.findBySeasonIdAndUserId(seasonId, userId).stream()
                .map(LeaguePredictionResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaguePredictionResponse> getAllLeaguePredictions(UUID seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", seasonId));

        if (!season.isPredictionLocked() && !SecurityUser.isAdmin()) {
            throw new ForbiddenException("League predictions are only visible after the prediction window closes");
        }

        return leaguePredictionRepository.findBySeasonId(seasonId).stream()
                .map(LeaguePredictionResponse::from).toList();
    }
}
