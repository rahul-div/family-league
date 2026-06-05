package com.familyleague.player.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.familyleague.player.entity.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    Page<Player> findByTeamId(UUID teamId, Pageable pageable);

    List<Player> findByTeamIdIn(Collection<UUID> teamIds);
}
