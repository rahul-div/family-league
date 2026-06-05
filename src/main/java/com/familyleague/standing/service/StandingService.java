package com.familyleague.standing.service;

import java.util.List;
import java.util.UUID;

import com.familyleague.match.entity.MatchResult;
import com.familyleague.standing.dto.LeagueStandingResponse;

public interface StandingService {

    List<LeagueStandingResponse> getStandings(UUID seasonId);

    void initializeStandings(UUID seasonId);

    void updateStandingsAfterResult(MatchResult result);
}
