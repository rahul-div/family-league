package com.familyleague.player.service;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.player.dto.CreatePlayerRequest;
import com.familyleague.player.dto.PlayerResponse;
import com.familyleague.player.dto.UpdatePlayerRequest;
import com.familyleague.player.entity.Player;
import com.familyleague.player.repository.PlayerRepository;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.TeamRepository;

@Service
@Transactional(readOnly = true)
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "players", allEntries = true)
    public PlayerResponse create(CreatePlayerRequest request) {
        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> ResourceNotFoundException.of("Team", request.teamId()));

        Player player = Player.builder()
                .team(team)
                .name(request.name())
                .jerseyNumber(request.jerseyNumber())
                .playerRole(request.playerRole())
                .build();
        return PlayerResponse.from(playerRepository.save(player));
    }

    @Override
    public PlayerResponse getById(UUID id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Player", id));
        return PlayerResponse.from(player);
    }

    @Override
    public PagedResponse<PlayerResponse> getByTeam(UUID teamId, Pageable pageable) {
        Page<Player> page = playerRepository.findByTeamId(teamId, pageable);
        List<PlayerResponse> content = page.getContent().stream()
                .map(PlayerResponse::from).toList();
        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional
    @CacheEvict(value = "players", allEntries = true)
    public PlayerResponse update(UUID id, UpdatePlayerRequest request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Player", id));

        if (StringUtils.hasText(request.name())) player.setName(request.name());
        if (request.jerseyNumber() != null) player.setJerseyNumber(request.jerseyNumber());
        if (request.playerRole() != null) player.setPlayerRole(request.playerRole());
        if (request.teamId() != null) {
            Team newTeam = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Team", request.teamId()));
            player.setTeam(newTeam);
        }

        return PlayerResponse.from(playerRepository.save(player));
    }

    @Override
    @Transactional
    @CacheEvict(value = "players", allEntries = true)
    public void delete(UUID id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Player", id));
        player.softDelete(SecurityUser.currentUserId());
        playerRepository.save(player);
    }
}
