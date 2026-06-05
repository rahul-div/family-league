package com.familyleague.scoring.dto;

import java.util.UUID;

import com.familyleague.scoring.entity.MatchScoreDetail;

public record MatchScoreDetailResponse(
        UUID matchId,
        Integer matchNumber,
        boolean winnerCorrect,
        boolean tossWinnerCorrect,
        boolean potmCorrect,
        int totalMatchPoints
) {
    public static MatchScoreDetailResponse from(MatchScoreDetail msd) {
        return new MatchScoreDetailResponse(
                msd.getMatch().getId(),
                msd.getMatch().getMatchNumber(),
                msd.isWinnerCorrect(),
                msd.isTossWinnerCorrect(),
                msd.isPotmCorrect(),
                msd.getTotalMatchPoints()
        );
    }
}
