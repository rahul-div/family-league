package com.familyleague.prediction.dto;

import java.util.UUID;

public record SubmitMatchPredictionRequest(
        UUID predictedWinnerTeamId,
        UUID predictedTossWinnerTeamId,
        UUID predictedPlayerOfMatchId
) {}
