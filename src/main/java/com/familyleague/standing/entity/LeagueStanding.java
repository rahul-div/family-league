package com.familyleague.standing.entity;

import com.familyleague.common.entity.BaseEntity;
import com.familyleague.league.entity.Season;
import com.familyleague.team.entity.Team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "league_standings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"season_id", "team_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueStanding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "current_position")
    private Integer currentPosition;

    @Column(name = "matches_played")
    @Builder.Default
    private int matchesPlayed = 0;

    @Column(name = "wins")
    @Builder.Default
    private int wins = 0;

    @Column(name = "draws")
    @Builder.Default
    private int draws = 0;

    @Column(name = "losses")
    @Builder.Default
    private int losses = 0;

    @Column(name = "points_in_league")
    @Builder.Default
    private int pointsInLeague = 0;
}
