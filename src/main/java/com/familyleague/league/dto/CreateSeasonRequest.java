package com.familyleague.league.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSeasonRequest(
        @NotNull UUID leagueId,
        @NotBlank @Size(max = 255) String name,
        Integer seasonNumber,
        String description,
        @Min(1) Integer leaguePredictionLockHours,
        @Min(1) Integer matchPredictionLockHours
) {}
