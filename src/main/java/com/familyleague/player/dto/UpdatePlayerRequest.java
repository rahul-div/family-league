package com.familyleague.player.dto;

import java.util.UUID;

import com.familyleague.player.entity.PlayerRole;

import jakarta.validation.constraints.Size;

public record UpdatePlayerRequest(
        @Size(max = 255) String name,
        Integer jerseyNumber,
        PlayerRole playerRole,
        UUID teamId
) {}
