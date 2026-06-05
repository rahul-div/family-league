package com.familyleague.scoring.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.scoring.entity.UserSeasonScore;

@Repository
public interface UserSeasonScoreRepository extends JpaRepository<UserSeasonScore, UUID> {

    @Query("SELECT uss FROM UserSeasonScore uss WHERE uss.season.id = :seasonId " +
            "ORDER BY uss.totalPoints DESC, uss.lastCalculatedAt ASC")
    Page<UserSeasonScore> findLeaderboard(@Param("seasonId") UUID seasonId, Pageable pageable);

    Optional<UserSeasonScore> findByUserIdAndSeasonId(UUID userId, UUID seasonId);
}
