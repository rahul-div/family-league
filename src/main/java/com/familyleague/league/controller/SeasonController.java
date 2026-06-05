package com.familyleague.league.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.common.dto.ApiResponse;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.league.dto.CreateSeasonRequest;
import com.familyleague.league.dto.PublishFinalStandingsRequest;
import com.familyleague.league.dto.SeasonResponse;
import com.familyleague.league.service.SeasonService;
import com.familyleague.standing.dto.LeagueStandingResponse;
import com.familyleague.standing.service.StandingService;
import com.familyleague.team.dto.EnrollTeamRequest;
import com.familyleague.team.dto.TeamResponse;
import com.familyleague.team.service.SeasonTeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/seasons")
@Tag(name = "Seasons", description = "Season lifecycle management")
public class SeasonController {

    private final SeasonService seasonService;
    private final SeasonTeamService seasonTeamService;
    private final StandingService standingService;

    public SeasonController(SeasonService seasonService, SeasonTeamService seasonTeamService,
                            StandingService standingService) {
        this.seasonService = seasonService;
        this.seasonTeamService = seasonTeamService;
        this.standingService = standingService;
    }

    @PostMapping
    @Operation(summary = "Create a season (admin only)")
    public ResponseEntity<ApiResponse<SeasonResponse>> create(@Valid @RequestBody CreateSeasonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Season created", seasonService.create(request)));
    }

    @GetMapping("/league/{leagueId}")
    @Operation(summary = "List seasons for a league")
    public ResponseEntity<ApiResponse<PagedResponse<SeasonResponse>>> getByLeague(
            @PathVariable UUID leagueId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(seasonService.getByLeagueId(leagueId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get season by ID")
    public ResponseEntity<ApiResponse<SeasonResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seasonService.getById(id)));
    }

    @PostMapping("/{id}/open")
    @Operation(summary = "Open a season for predictions (admin only)")
    public ResponseEntity<ApiResponse<SeasonResponse>> open(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Season opened", seasonService.openSeason(id)));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a completed season (admin only)")
    public ResponseEntity<ApiResponse<SeasonResponse>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Season closed", seasonService.closeSeason(id)));
    }

    @PostMapping("/{id}/teams")
    @Operation(summary = "Enroll a team in the season (admin only)")
    public ResponseEntity<ApiResponse<Void>> enrollTeam(
            @PathVariable UUID id, @Valid @RequestBody EnrollTeamRequest request) {
        seasonTeamService.enrollTeam(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Team enrolled"));
    }

    @DeleteMapping("/{id}/teams/{teamId}")
    @Operation(summary = "Remove a team from the season (admin only)")
    public ResponseEntity<ApiResponse<Void>> removeTeam(@PathVariable UUID id, @PathVariable UUID teamId) {
        seasonTeamService.removeTeam(id, teamId);
        return ResponseEntity.ok(ApiResponse.success("Team removed"));
    }

    @GetMapping("/{id}/teams")
    @Operation(summary = "List enrolled teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeams(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seasonTeamService.getSeasonTeams(id)));
    }

    @GetMapping("/{id}/standings")
    @Operation(summary = "Get league standings")
    public ResponseEntity<ApiResponse<List<LeagueStandingResponse>>> getStandings(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(standingService.getStandings(id)));
    }

    @PostMapping("/{id}/publish-result")
    @Operation(summary = "Publish final standings and complete season (admin only)")
    public ResponseEntity<ApiResponse<SeasonResponse>> publishResult(
            @PathVariable UUID id, @Valid @RequestBody PublishFinalStandingsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Final standings published",
                seasonService.publishFinalStandings(id, request)));
    }
}
