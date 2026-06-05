package com.familyleague.standing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.familyleague.standing.entity.LeagueStanding;

@Repository
public interface LeagueStandingRepository extends JpaRepository<LeagueStanding, UUID> {

    List<LeagueStanding> findBySeasonIdOrderByCurrentPosition(UUID seasonId);

    Optional<LeagueStanding> findBySeasonIdAndTeamId(UUID seasonId, UUID teamId);

    List<LeagueStanding> findBySeasonId(UUID seasonId);
}
