package com.familyleague.league.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.league.dto.CreateSeasonRequest;
import com.familyleague.league.dto.PublishFinalStandingsRequest;
import com.familyleague.league.dto.SeasonResponse;

public interface SeasonService {

    SeasonResponse create(CreateSeasonRequest request);

    PagedResponse<SeasonResponse> getByLeagueId(UUID leagueId, Pageable pageable);

    SeasonResponse getById(UUID id);

    SeasonResponse openSeason(UUID id);

    SeasonResponse closeSeason(UUID id);

    void deleteSeason(UUID id);

    SeasonResponse publishFinalStandings(UUID seasonId, PublishFinalStandingsRequest request);
}
