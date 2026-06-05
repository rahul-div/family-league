package com.familyleague.league.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.league.entity.League;

@Repository
public interface LeagueRepository extends JpaRepository<League, UUID> {

    boolean existsByName(String name);

    @Query("SELECT l FROM League l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<League> search(@Param("query") String query, Pageable pageable);
}
