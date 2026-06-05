package com.familyleague.match.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.match.entity.Match;

public record MatchResponse(
        UUID id,
        UUID seasonId,
        String seasonName,
        UUID homeTeamId,
        String homeTeamName,
        UUID awayTeamId,
        String awayTeamName,
        Integer matchNumber,
        Instant scheduledAt,
        String venue,
        String status,
        Instant predictionLockTime,
        boolean predictionLocked,
        Instant createdAt
) {
    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getSeason().getId(),
                match.getSeason().getName(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getMatchNumber(),
                match.getScheduledAt(),
                match.getVenue(),
                match.getStatus().name(),
                match.getPredictionLockTime(),
                match.isPredictionLocked(),
                match.getCreatedAt()
        );
    }
}
