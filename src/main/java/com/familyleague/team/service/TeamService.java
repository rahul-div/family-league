package com.familyleague.team.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.team.dto.CreateTeamRequest;
import com.familyleague.team.dto.TeamResponse;
import com.familyleague.team.dto.UpdateTeamRequest;

public interface TeamService {

    TeamResponse create(CreateTeamRequest request);

    PagedResponse<TeamResponse> getAll(String query, Pageable pageable);

    TeamResponse getById(UUID id);

    TeamResponse update(UUID id, UpdateTeamRequest request);

    void delete(UUID id);
}
