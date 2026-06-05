package com.familyleague.prediction.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.prediction.entity.LeaguePrediction;

@Repository
public interface LeaguePredictionRepository extends JpaRepository<LeaguePrediction, UUID> {

    List<LeaguePrediction> findBySeasonIdAndUserId(UUID seasonId, UUID userId);

    List<LeaguePrediction> findBySeasonId(UUID seasonId);

    @Modifying
    @Query("DELETE FROM LeaguePrediction lp WHERE lp.season.id = :seasonId AND lp.user.id = :userId")
    void deleteBySeasonIdAndUserId(@Param("seasonId") UUID seasonId, @Param("userId") UUID userId);

    @Query("SELECT DISTINCT lp.user.id FROM LeaguePrediction lp WHERE lp.season.id = :seasonId")
    List<UUID> findUserIdsWhoSubmittedPrediction(@Param("seasonId") UUID seasonId);
}
