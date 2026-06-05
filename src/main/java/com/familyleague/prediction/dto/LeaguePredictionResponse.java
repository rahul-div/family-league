package com.familyleague.prediction.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.prediction.entity.LeaguePrediction;

public record LeaguePredictionResponse(
        UUID id,
        UUID seasonId,
        UUID userId,
        String username,
        UUID teamId,
        String teamName,
        int predictedPosition,
        boolean locked,
        Instant createdAt
) {
    public static LeaguePredictionResponse from(LeaguePrediction lp) {
        return new LeaguePredictionResponse(
                lp.getId(),
                lp.getSeason().getId(),
                lp.getUser().getId(),
                lp.getUser().getUsername(),
                lp.getTeam().getId(),
                lp.getTeam().getName(),
                lp.getPredictedPosition(),
                lp.isLocked(),
                lp.getCreatedAt()
        );
    }
}
