package com.familyleague.standing.dto;

import java.util.UUID;

import com.familyleague.standing.entity.LeagueStanding;

public record LeagueStandingResponse(
        UUID id,
        UUID seasonId,
        UUID teamId,
        String teamName,
        String teamShortName,
        Integer currentPosition,
        int matchesPlayed,
        int wins,
        int draws,
        int losses,
        int pointsInLeague
) {
    public static LeagueStandingResponse from(LeagueStanding ls) {
        return new LeagueStandingResponse(
                ls.getId(),
                ls.getSeason().getId(),
                ls.getTeam().getId(),
                ls.getTeam().getName(),
                ls.getTeam().getShortName(),
                ls.getCurrentPosition(),
                ls.getMatchesPlayed(),
                ls.getWins(),
                ls.getDraws(),
                ls.getLosses(),
                ls.getPointsInLeague()
        );
    }
}
