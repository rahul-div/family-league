package com.familyleague.match.dto;

import java.time.Instant;
import java.util.UUID;

import com.familyleague.match.entity.MatchResult;

public record MatchResultResponse(
        UUID id,
        UUID matchId,
        Integer matchNumber,
        UUID winnerTeamId,
        String winnerTeamName,
        UUID tossWinnerTeamId,
        String tossWinnerTeamName,
        UUID playerOfMatchId,
        String playerOfMatchName,
        boolean tie,
        Instant publishedAt
) {
    public static MatchResultResponse from(MatchResult result) {
        return new MatchResultResponse(
                result.getId(),
                result.getMatch().getId(),
                result.getMatch().getMatchNumber(),
                result.getWinnerTeam() != null ? result.getWinnerTeam().getId() : null,
                result.getWinnerTeam() != null ? result.getWinnerTeam().getName() : null,
                result.getTossWinnerTeam() != null ? result.getTossWinnerTeam().getId() : null,
                result.getTossWinnerTeam() != null ? result.getTossWinnerTeam().getName() : null,
                result.getPlayerOfMatch() != null ? result.getPlayerOfMatch().getId() : null,
                result.getPlayerOfMatch() != null ? result.getPlayerOfMatch().getName() : null,
                result.isTie(),
                result.getPublishedAt()
        );
    }
}
