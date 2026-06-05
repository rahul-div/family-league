package com.familyleague.team.service;

import java.util.List;
import java.util.UUID;

import com.familyleague.team.dto.EnrollTeamRequest;
import com.familyleague.team.dto.TeamResponse;

public interface SeasonTeamService {

    void enrollTeam(UUID seasonId, EnrollTeamRequest request);

    void removeTeam(UUID seasonId, UUID teamId);

    List<TeamResponse> getSeasonTeams(UUID seasonId);
}
