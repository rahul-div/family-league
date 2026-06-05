package com.familyleague.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 10) String shortName,
        String logoUrl,
        String description
) {}
