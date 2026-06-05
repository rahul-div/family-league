package com.familyleague.team.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.familyleague.team.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    boolean existsByName(String name);

    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Team> search(@Param("query") String query, Pageable pageable);
}
