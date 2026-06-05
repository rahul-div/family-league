package com.familyleague.match.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchStatus;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    Page<Match> findBySeasonId(UUID seasonId, Pageable pageable);

    Page<Match> findBySeasonIdAndStatus(UUID seasonId, MatchStatus status, Pageable pageable);

    @Query("SELECT m FROM Match m WHERE m.status = 'SCHEDULED' AND m.predictionLockTime <= CURRENT_TIMESTAMP")
    List<Match> findMatchesToLock();

    @Query("SELECT MIN(m.scheduledAt) FROM Match m WHERE m.season.id = :seasonId AND m.isDeleted = false")
    Optional<Instant> findFirstMatchTime(@Param("seasonId") UUID seasonId);

    @Query("SELECT m FROM Match m WHERE m.status = 'SCHEDULED' " +
            "AND m.predictionLockTime > CURRENT_TIMESTAMP " +
            "AND m.predictionLockTime <= :reminderCutoff")
    List<Match> findMatchesNeedingReminder(@Param("reminderCutoff") Instant reminderCutoff);

    @Query("SELECT m FROM Match m WHERE m.status = 'COMPLETED' " +
            "AND m.id NOT IN (SELECT mr.match.id FROM com.familyleague.match.entity.MatchResult mr) " +
            "AND m.scheduledAt <= :alertCutoff")
    List<Match> findCompletedMatchesWithoutResult(@Param("alertCutoff") Instant alertCutoff);

    long countBySeasonIdAndIsDeletedFalse(UUID seasonId);
}
