package com.familyleague.prediction.dto;

public record HeadToHeadResponse(
        MatchPredictionResponse myPrediction,
        MatchPredictionResponse theirPrediction
) {}
