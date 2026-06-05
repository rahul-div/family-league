package com.familyleague.team.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.team.entity.SeasonTeam;

@Repository
public interface SeasonTeamRepository extends JpaRepository<SeasonTeam, UUID> {

    List<SeasonTeam> findBySeasonIdAndIsDeletedFalse(UUID seasonId);

    Optional<SeasonTeam> findBySeasonIdAndTeamIdAndIsDeletedFalse(UUID seasonId, UUID teamId);

    boolean existsBySeasonIdAndTeamIdAndIsDeletedFalse(UUID seasonId, UUID teamId);

    long countBySeasonIdAndIsDeletedFalse(UUID seasonId);

    @Query("SELECT st FROM SeasonTeam st WHERE st.season.id = :seasonId AND st.isDeleted = false " +
            "ORDER BY CASE WHEN st.currentPosition IS NULL THEN 1 ELSE 0 END, st.currentPosition")
    List<SeasonTeam> findBySeasonIdOrderByCurrentPositionNullsLast(@Param("seasonId") UUID seasonId);
}
