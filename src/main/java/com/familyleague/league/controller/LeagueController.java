package com.familyleague.league.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.common.dto.ApiResponse;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.league.dto.CreateLeagueRequest;
import com.familyleague.league.dto.LeagueResponse;
import com.familyleague.league.dto.UpdateLeagueRequest;
import com.familyleague.league.service.LeagueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/leagues")
@Tag(name = "Leagues", description = "League management")
public class LeagueController {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @PostMapping
    @Operation(summary = "Create a league (admin only)")
    public ResponseEntity<ApiResponse<LeagueResponse>> create(@Valid @RequestBody CreateLeagueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("League created", leagueService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List all leagues")
    public ResponseEntity<ApiResponse<PagedResponse<LeagueResponse>>> getAll(
            @RequestParam(required = false) String q,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(leagueService.getAll(q, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get league by ID")
    public ResponseEntity<ApiResponse<LeagueResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(leagueService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a league (admin only)")
    public ResponseEntity<ApiResponse<LeagueResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateLeagueRequest request) {
        return ResponseEntity.ok(ApiResponse.success("League updated", leagueService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a league (admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        leagueService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("League deleted"));
    }
}
