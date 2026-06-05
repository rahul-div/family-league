package com.familyleague.league.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.league.entity.League;

public record LeagueResponse(
        UUID id,
        String name,
        String description,
        String sportType,
        boolean active,
        int seasonCount,
        Instant createdAt
) {
    public static LeagueResponse from(League league) {
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getDescription(),
                league.getSportType(),
                league.isActive(),
                league.getSeasons() != null ? league.getSeasons().size() : 0,
                league.getCreatedAt()
        );
    }

    public static LeagueResponse fromWithoutSeasons(League league) {
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getDescription(),
                league.getSportType(),
                league.isActive(),
                0,
                league.getCreatedAt()
        );
    }
}
