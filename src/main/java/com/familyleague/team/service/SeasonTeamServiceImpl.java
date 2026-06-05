package com.familyleague.team.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.exception.BadRequestException;
import com.familyleague.common.exception.ConflictException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.entity.Season;
import com.familyleague.league.repository.SeasonRepository;
import com.familyleague.team.dto.EnrollTeamRequest;
import com.familyleague.team.dto.TeamResponse;
import com.familyleague.team.entity.SeasonTeam;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.SeasonTeamRepository;
import com.familyleague.team.repository.TeamRepository;

@Service
@Transactional
public class SeasonTeamServiceImpl implements SeasonTeamService {

    private final SeasonTeamRepository seasonTeamRepository;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;

    public SeasonTeamServiceImpl(SeasonTeamRepository seasonTeamRepository,
                                  SeasonRepository seasonRepository,
                                  TeamRepository teamRepository) {
        this.seasonTeamRepository = seasonTeamRepository;
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public void enrollTeam(UUID seasonId, EnrollTeamRequest request) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> ResourceNotFoundException.of("Season", seasonId));

        if (season.isClosed()) {
            throw new BadRequestException("Cannot enroll teams in a closed season");
        }

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> ResourceNotFoundException.of("Team", request.teamId()));

        if (seasonTeamRepository.existsBySeasonIdAndTeamIdAndIsDeletedFalse(seasonId, request.teamId())) {
            throw new ConflictException("Team '" + team.getName() + "' is already enrolled in this season");
        }

        SeasonTeam seasonTeam = SeasonTeam.builder()
                .season(season)
                .team(team)
                .build();
        seasonTeamRepository.save(seasonTeam);
    }

    @Override
    public void removeTeam(UUID seasonId, UUID teamId) {
        SeasonTeam seasonTeam = seasonTeamRepository.findBySeasonIdAndTeamIdAndIsDeletedFalse(seasonId, teamId)
                .orElseThrow(() -> ResourceNotFoundException.of("SeasonTeam", teamId));
        seasonTeam.softDelete(SecurityUser.currentUserId());
        seasonTeamRepository.save(seasonTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getSeasonTeams(UUID seasonId) {
        if (!seasonRepository.existsById(seasonId)) {
            throw ResourceNotFoundException.of("Season", seasonId);
        }
        return seasonTeamRepository.findBySeasonIdAndIsDeletedFalse(seasonId).stream()
                .map(st -> TeamResponse.from(st.getTeam()))
                .toList();
    }
}
