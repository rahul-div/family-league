package com.familyleague.match.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMatchRequest(
        @NotNull Instant scheduledAt,
        @Size(max = 200) String venue
) {}
