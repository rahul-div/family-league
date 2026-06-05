package com.familyleague.match.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMatchRequest(
        @NotNull UUID seasonId,
        @NotNull UUID homeTeamId,
        @NotNull UUID awayTeamId,
        @Min(1) Integer matchNumber,
        @NotNull Instant scheduledAt,
        @Size(max = 200) String venue
) {}
