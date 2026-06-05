package com.familyleague.match.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.match.dto.CreateMatchRequest;
import com.familyleague.match.dto.MatchResponse;
import com.familyleague.match.dto.UpdateMatchRequest;
import com.familyleague.match.entity.MatchStatus;

public interface MatchService {

    MatchResponse create(CreateMatchRequest request);

    PagedResponse<MatchResponse> getBySeason(UUID seasonId, MatchStatus status, Pageable pageable);

    MatchResponse getById(UUID id);

    MatchResponse update(UUID id, UpdateMatchRequest request);

    void delete(UUID id);
}
