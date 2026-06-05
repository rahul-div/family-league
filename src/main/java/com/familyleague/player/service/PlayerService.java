package com.familyleague.player.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.player.dto.CreatePlayerRequest;
import com.familyleague.player.dto.PlayerResponse;
import com.familyleague.player.dto.UpdatePlayerRequest;

public interface PlayerService {

    PlayerResponse create(CreatePlayerRequest request);

    PlayerResponse getById(UUID id);

    PagedResponse<PlayerResponse> getByTeam(UUID teamId, Pageable pageable);

    PlayerResponse update(UUID id, UpdatePlayerRequest request);

    void delete(UUID id);
}
