package com.familyleague.team.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.common.dto.ApiResponse;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.team.dto.CreateTeamRequest;
import com.familyleague.team.dto.TeamResponse;
import com.familyleague.team.dto.UpdateTeamRequest;
import com.familyleague.team.service.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/teams")
@Tag(name = "Teams", description = "Team management")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @Operation(summary = "Create a team (admin only)")
    public ResponseEntity<ApiResponse<TeamResponse>> create(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created", teamService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List all teams")
    public ResponseEntity<ApiResponse<PagedResponse<TeamResponse>>> getAll(
            @RequestParam(required = false) String q,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getAll(q, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<ApiResponse<TeamResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a team (admin only)")
    public ResponseEntity<ApiResponse<TeamResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team updated", teamService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a team (admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted"));
    }
}
