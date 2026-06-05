package com.familyleague.team.service;

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
import com.familyleague.common.exception.ConflictException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.team.dto.CreateTeamRequest;
import com.familyleague.team.dto.TeamResponse;
import com.familyleague.team.dto.UpdateTeamRequest;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.TeamRepository;

@Service
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "teams", allEntries = true)
    public TeamResponse create(CreateTeamRequest request) {
        if (teamRepository.existsByName(request.name())) {
            throw new ConflictException("Team with name '" + request.name() + "' already exists");
        }
        Team team = Team.builder()
                .name(request.name())
                .shortName(request.shortName())
                .logoUrl(request.logoUrl())
                .description(request.description())
                .build();
        return TeamResponse.from(teamRepository.save(team));
    }

    @Override
    public PagedResponse<TeamResponse> getAll(String query, Pageable pageable) {
        Page<Team> page;
        if (StringUtils.hasText(query)) {
            page = teamRepository.search(query, pageable);
        } else {
            page = teamRepository.findAll(pageable);
        }
        List<TeamResponse> content = page.getContent().stream()
                .map(TeamResponse::from).toList();
        return PagedResponse.from(page, content);
    }

    @Override
    public TeamResponse getById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Team", id));
        return TeamResponse.from(team);
    }

    @Override
    @Transactional
    @CacheEvict(value = "teams", allEntries = true)
    public TeamResponse update(UUID id, UpdateTeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Team", id));
        if (StringUtils.hasText(request.name())) team.setName(request.name());
        if (request.shortName() != null) team.setShortName(request.shortName());
        if (request.logoUrl() != null) team.setLogoUrl(request.logoUrl());
        if (request.description() != null) team.setDescription(request.description());
        return TeamResponse.from(teamRepository.save(team));
    }

    @Override
    @Transactional
    @CacheEvict(value = "teams", allEntries = true)
    public void delete(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Team", id));
        team.softDelete(SecurityUser.currentUserId());
        teamRepository.save(team);
    }
}
