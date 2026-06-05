package com.familyleague.player.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.common.dto.ApiResponse;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.player.dto.CreatePlayerRequest;
import com.familyleague.player.dto.PlayerResponse;
import com.familyleague.player.dto.UpdatePlayerRequest;
import com.familyleague.player.service.PlayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/players")
@Tag(name = "Players", description = "Player management")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    @Operation(summary = "Create a player (admin only)")
    public ResponseEntity<ApiResponse<PlayerResponse>> create(@Valid @RequestBody CreatePlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Player created", playerService.create(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID")
    public ResponseEntity<ApiResponse<PlayerResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(playerService.getById(id)));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "List players by team")
    public ResponseEntity<ApiResponse<PagedResponse<PlayerResponse>>> getByTeam(
            @PathVariable UUID teamId,
            @PageableDefault(size = 25) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(playerService.getByTeam(teamId, pageable)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a player (admin only)")
    public ResponseEntity<ApiResponse<PlayerResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdatePlayerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Player updated", playerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a player (admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        playerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Player deleted"));
    }
}
