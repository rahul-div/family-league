package com.familyleague.team.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.team.entity.Team;

public record TeamResponse(
        UUID id,
        String name,
        String shortName,
        String logoUrl,
        String description,
        Instant createdAt
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(), team.getName(), team.getShortName(),
                team.getLogoUrl(), team.getDescription(), team.getCreatedAt()
        );
    }
}
