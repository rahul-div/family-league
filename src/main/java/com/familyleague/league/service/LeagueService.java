package com.familyleague.league.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.league.dto.CreateLeagueRequest;
import com.familyleague.league.dto.LeagueResponse;
import com.familyleague.league.dto.UpdateLeagueRequest;

public interface LeagueService {

    LeagueResponse create(CreateLeagueRequest request);

    PagedResponse<LeagueResponse> getAll(String query, Pageable pageable);

    LeagueResponse getById(UUID id);

    LeagueResponse update(UUID id, UpdateLeagueRequest request);

    void delete(UUID id);
}
