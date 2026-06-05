package com.familyleague.league.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.familyleague.league.entity.Season;

@Repository
public interface SeasonRepository extends JpaRepository<Season, UUID> {

    Page<Season> findByLeagueId(UUID leagueId, Pageable pageable);

    @Query("SELECT s FROM Season s WHERE s.status = 'PREDICTION_OPEN' " +
            "AND s.predictionLockedAt IS NOT NULL AND s.predictionLockedAt <= CURRENT_TIMESTAMP")
    List<Season> findSeasonsToLock();
}
