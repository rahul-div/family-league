package com.familyleague.match.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PublishResultRequest(
        @NotNull UUID matchId,
        UUID winnerTeamId,
        UUID tossWinnerTeamId,
        UUID playerOfMatchId,
        boolean tie
) {}
