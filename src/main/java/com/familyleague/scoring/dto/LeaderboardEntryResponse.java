package com.familyleague.scoring.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.scoring.entity.UserSeasonScore;

public record LeaderboardEntryResponse(
        Integer rank,
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        int matchPoints,
        int seasonPredictionPoints,
        int totalPoints,
        Instant lastCalculatedAt
) {
    public static LeaderboardEntryResponse from(UserSeasonScore uss) {
        return new LeaderboardEntryResponse(
                uss.getRank(),
                uss.getUser().getId(),
                uss.getUser().getUsername(),
                uss.getUser().getDisplayName(),
                uss.getUser().getAvatarUrl(),
                uss.getMatchPoints(),
                uss.getSeasonPredictionPoints(),
                uss.getTotalPoints(),
                uss.getLastCalculatedAt()
        );
    }
}
