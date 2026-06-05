package com.familyleague.match.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.familyleague.match.entity.MatchResult;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    Optional<MatchResult> findByMatchId(UUID matchId);

    boolean existsByMatchId(UUID matchId);
}
