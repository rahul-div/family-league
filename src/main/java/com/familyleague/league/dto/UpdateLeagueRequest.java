package com.familyleague.league.dto;

import jakarta.validation.constraints.Size;

public record UpdateLeagueRequest(
        @Size(max = 255) String name,
        String description,
        @Size(max = 50) String sportType
) {}
