package com.familyleague.player.dto;

import java.util.UUID;

import com.familyleague.player.entity.PlayerRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePlayerRequest(
        @NotNull UUID teamId,
        @NotBlank @Size(max = 255) String name,
        Integer jerseyNumber,
        PlayerRole playerRole
) {}
