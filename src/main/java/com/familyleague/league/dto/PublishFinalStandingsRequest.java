package com.familyleague.league.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PublishFinalStandingsRequest(
        @NotEmpty @Valid List<Entry> standings
) {
    public record Entry(
            @NotNull UUID teamId,
            @Positive int finalPosition
    ) {}
}
