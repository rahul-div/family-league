package com.familyleague.scoring.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.common.dto.ApiResponse;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.scoring.dto.LeaderboardEntryResponse;
import com.familyleague.scoring.dto.MyRankResponse;
import com.familyleague.scoring.service.LeaderboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/leaderboard")
@Tag(name = "Leaderboard", description = "Season leaderboard and rankings")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/seasons/{seasonId}")
    @Operation(summary = "Get leaderboard for a season")
    public ResponseEntity<ApiResponse<PagedResponse<LeaderboardEntryResponse>>> getLeaderboard(
            @PathVariable UUID seasonId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getLeaderboard(seasonId, pageable)));
    }

    @GetMapping("/seasons/{seasonId}/me")
    @Operation(summary = "Get my rank and score breakdown")
    public ResponseEntity<ApiResponse<MyRankResponse>> getMyRank(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getMyRank(seasonId)));
    }

    @PostMapping("/seasons/{seasonId}/recalculate")
    @Operation(summary = "Manually recalculate leaderboard (admin only)")
    public ResponseEntity<ApiResponse<Void>> recalculate(@PathVariable UUID seasonId) {
        leaderboardService.recalculateLeaderboard(seasonId);
        return ResponseEntity.ok(ApiResponse.success("Leaderboard recalculation triggered"));
    }
}
