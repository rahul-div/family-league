package com.familyleague.scoring.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.scoring.entity.MatchScoreDetail;

@Repository
public interface MatchScoreDetailRepository extends JpaRepository<MatchScoreDetail, UUID> {

    @Query("SELECT COALESCE(SUM(msd.totalMatchPoints), 0) FROM MatchScoreDetail msd " +
            "WHERE msd.user.id = :userId AND msd.season.id = :seasonId")
    int sumMatchPointsForUserInSeason(@Param("userId") UUID userId, @Param("seasonId") UUID seasonId);

    List<MatchScoreDetail> findBySeasonIdAndUserId(UUID seasonId, UUID userId);

    List<MatchScoreDetail> findByMatchId(UUID matchId);
}
