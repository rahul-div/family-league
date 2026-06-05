package com.familyleague.scoring.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.scoring.entity.SeasonScoreDetail;

@Repository
public interface SeasonScoreDetailRepository extends JpaRepository<SeasonScoreDetail, UUID> {

    @Query("SELECT COALESCE(SUM(ssd.pointsEarned), 0) FROM SeasonScoreDetail ssd " +
            "WHERE ssd.user.id = :userId AND ssd.season.id = :seasonId")
    int sumSeasonPointsForUserInSeason(@Param("userId") UUID userId, @Param("seasonId") UUID seasonId);

    List<SeasonScoreDetail> findBySeasonIdAndUserId(UUID seasonId, UUID userId);

    @Modifying
    @Query("DELETE FROM SeasonScoreDetail ssd WHERE ssd.season.id = :seasonId")
    void deleteBySeasonId(@Param("seasonId") UUID seasonId);
}
