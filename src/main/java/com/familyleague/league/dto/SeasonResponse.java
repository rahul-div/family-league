package com.familyleague.league.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.league.entity.Season;

public record SeasonResponse(
        UUID id,
        UUID leagueId,
        String leagueName,
        String name,
        Integer seasonNumber,
        String description,
        String status,
        Integer leaguePredictionLockHours,
        Integer matchPredictionLockHours,
        Instant firstMatchAt,
        Instant predictionLockedAt,
        Instant startedAt,
        Instant completedAt,
        Instant closedAt,
        long teamCount,
        Instant createdAt
) {
    public static SeasonResponse from(Season season, long teamCount) {
        return new SeasonResponse(
                season.getId(),
                season.getLeague().getId(),
                season.getLeague().getName(),
                season.getName(),
                season.getSeasonNumber(),
                season.getDescription(),
                season.getStatus().name(),
                season.getLeaguePredictionLockHours(),
                season.getMatchPredictionLockHours(),
                season.getFirstMatchAt(),
                season.getPredictionLockedAt(),
                season.getStartedAt(),
                season.getCompletedAt(),
                season.getClosedAt(),
                teamCount,
                season.getCreatedAt()
        );
    }
}
