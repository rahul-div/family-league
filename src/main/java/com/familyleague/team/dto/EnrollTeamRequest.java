package com.familyleague.team.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record EnrollTeamRequest(
        @NotNull UUID teamId
) {}
