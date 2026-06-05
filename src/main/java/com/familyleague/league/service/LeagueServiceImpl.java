package com.familyleague.league.service;

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
import com.familyleague.league.dto.CreateLeagueRequest;
import com.familyleague.league.dto.LeagueResponse;
import com.familyleague.league.dto.UpdateLeagueRequest;
import com.familyleague.league.entity.League;
import com.familyleague.league.repository.LeagueRepository;

@Service
@Transactional(readOnly = true)
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;

    public LeagueServiceImpl(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "leagues", allEntries = true)
    public LeagueResponse create(CreateLeagueRequest request) {
        if (leagueRepository.existsByName(request.name())) {
            throw new ConflictException("League with name '" + request.name() + "' already exists");
        }

        League league = League.builder()
                .name(request.name())
                .description(request.description())
                .sportType(request.sportType())
                .build();

        return LeagueResponse.fromWithoutSeasons(leagueRepository.save(league));
    }

    @Override
    public PagedResponse<LeagueResponse> getAll(String query, Pageable pageable) {
        Page<League> page;
        if (StringUtils.hasText(query)) {
            page = leagueRepository.search(query, pageable);
        } else {
            page = leagueRepository.findAll(pageable);
        }
        List<LeagueResponse> content = page.getContent().stream()
                .map(LeagueResponse::fromWithoutSeasons)
                .toList();
        return PagedResponse.from(page, content);
    }

    @Override
    public LeagueResponse getById(UUID id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("League", id));
        return LeagueResponse.fromWithoutSeasons(league);
    }

    @Override
    @Transactional
    @CacheEvict(value = "leagues", allEntries = true)
    public LeagueResponse update(UUID id, UpdateLeagueRequest request) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("League", id));

        if (StringUtils.hasText(request.name())) {
            league.setName(request.name());
        }
        if (request.description() != null) {
            league.setDescription(request.description());
        }
        if (request.sportType() != null) {
            league.setSportType(request.sportType());
        }

        return LeagueResponse.fromWithoutSeasons(leagueRepository.save(league));
    }

    @Override
    @Transactional
    @CacheEvict(value = "leagues", allEntries = true)
    public void delete(UUID id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("League", id));
        league.softDelete(SecurityUser.currentUserId());
        leagueRepository.save(league);
    }
}
