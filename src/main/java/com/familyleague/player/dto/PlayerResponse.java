package com.familyleague.player.dto;

import java.util.UUID;

import com.familyleague.player.entity.Player;

public record PlayerResponse(
        UUID id,
        UUID teamId,
        String teamName,
        String name,
        Integer jerseyNumber,
        String playerRole,
        boolean active
) {
    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getTeam().getId(),
                player.getTeam().getName(),
                player.getName(),
                player.getJerseyNumber(),
                player.getPlayerRole() != null ? player.getPlayerRole().name() : null,
                player.isActive()
        );
    }
}
