package com.familyleague.league.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLeagueRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        @Size(max = 50) String sportType
) {}
