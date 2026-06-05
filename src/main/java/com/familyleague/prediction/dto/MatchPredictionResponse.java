package com.familyleague.prediction.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.prediction.entity.MatchPrediction;

public record MatchPredictionResponse(
        UUID id,
        UUID matchId,
        Integer matchNumber,
        UUID userId,
        String username,
        UUID predictedWinnerTeamId,
        String predictedWinnerTeamName,
        UUID predictedTossWinnerTeamId,
        String predictedTossWinnerTeamName,
        UUID predictedPlayerOfMatchId,
        String predictedPlayerOfMatchName,
        boolean locked,
        Instant createdAt,
        Instant updatedAt
) {
    public static MatchPredictionResponse from(MatchPrediction mp) {
        return new MatchPredictionResponse(
                mp.getId(),
                mp.getMatch().getId(),
                mp.getMatch().getMatchNumber(),
                mp.getUser().getId(),
                mp.getUser().getUsername(),
                mp.getPredictedWinnerTeam() != null ? mp.getPredictedWinnerTeam().getId() : null,
                mp.getPredictedWinnerTeam() != null ? mp.getPredictedWinnerTeam().getName() : null,
                mp.getPredictedTossWinnerTeam() != null ? mp.getPredictedTossWinnerTeam().getId() : null,
                mp.getPredictedTossWinnerTeam() != null ? mp.getPredictedTossWinnerTeam().getName() : null,
                mp.getPredictedPotmPlayer() != null ? mp.getPredictedPotmPlayer().getId() : null,
                mp.getPredictedPotmPlayer() != null ? mp.getPredictedPotmPlayer().getName() : null,
                mp.isLocked(),
                mp.getCreatedAt(),
                mp.getUpdatedAt()
        );
    }
}
