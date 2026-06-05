package com.familyleague.scoring.dto;

import java.util.UUID;

import com.familyleague.scoring.entity.SeasonScoreDetail;

public record SeasonScoreDetailResponse(
        UUID teamId,
        String teamName,
        int predictedPosition,
        Integer actualPosition,
        int pointsEarned
) {
    public static SeasonScoreDetailResponse from(SeasonScoreDetail ssd) {
        return new SeasonScoreDetailResponse(
                ssd.getTeam().getId(),
                ssd.getTeam().getName(),
                ssd.getPredictedPosition(),
                ssd.getActualPosition(),
                ssd.getPointsEarned()
        );
    }
}
