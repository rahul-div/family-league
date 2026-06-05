package com.familyleague.prediction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.prediction.entity.MatchPrediction;

@Repository
public interface MatchPredictionRepository extends JpaRepository<MatchPrediction, UUID> {

    Optional<MatchPrediction> findByMatchIdAndUserId(UUID matchId, UUID userId);

    Page<MatchPrediction> findByMatchId(UUID matchId, Pageable pageable);

    List<MatchPrediction> findAllByMatchId(UUID matchId);

    @Query("SELECT DISTINCT mp.user.id FROM MatchPrediction mp WHERE mp.match.id = :matchId")
    List<UUID> findUserIdsWhoSubmittedPrediction(@Param("matchId") UUID matchId);
}
