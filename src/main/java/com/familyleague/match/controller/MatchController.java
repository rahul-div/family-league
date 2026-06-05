package com.familyleague.match.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.common.dto.ApiResponse;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.match.dto.CreateMatchRequest;
import com.familyleague.match.dto.MatchResponse;
import com.familyleague.match.dto.UpdateMatchRequest;
import com.familyleague.match.entity.MatchStatus;
import com.familyleague.match.service.MatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/matches")
@Tag(name = "Matches", description = "Match scheduling")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    @Operation(summary = "Schedule a match (admin only)")
    public ResponseEntity<ApiResponse<MatchResponse>> create(@Valid @RequestBody CreateMatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Match scheduled", matchService.create(request)));
    }

    @GetMapping("/season/{seasonId}")
    @Operation(summary = "List matches for a season")
    public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> getBySeason(
            @PathVariable UUID seasonId,
            @RequestParam(required = false) MatchStatus status,
            @PageableDefault(sort = "scheduledAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getBySeason(seasonId, status, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match by ID")
    public ResponseEntity<ApiResponse<MatchResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update match schedule (admin only, SCHEDULED matches only)")
    public ResponseEntity<ApiResponse<MatchResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateMatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Match updated", matchService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a match (admin only, SCHEDULED matches only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        matchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Match deleted"));
    }
}
