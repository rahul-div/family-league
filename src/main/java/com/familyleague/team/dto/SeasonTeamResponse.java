package com.familyleague.team.dto;

import java.util.UUID;

import com.familyleague.team.entity.SeasonTeam;

public record SeasonTeamResponse(
        UUID id,
        UUID seasonId,
        UUID teamId,
        String teamName,
        Integer seedPosition,
        Integer currentPosition
) {
    public static SeasonTeamResponse from(SeasonTeam st) {
        return new SeasonTeamResponse(
                st.getId(), st.getSeason().getId(), st.getTeam().getId(),
                st.getTeam().getName(), st.getSeedPosition(), st.getCurrentPosition()
        );
    }
}
