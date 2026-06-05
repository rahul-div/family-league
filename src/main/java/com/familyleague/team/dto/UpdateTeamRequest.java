package com.familyleague.team.dto;

import jakarta.validation.constraints.Size;

public record UpdateTeamRequest(
        @Size(max = 255) String name,
        @Size(max = 10) String shortName,
        String logoUrl,
        String description
) {}
