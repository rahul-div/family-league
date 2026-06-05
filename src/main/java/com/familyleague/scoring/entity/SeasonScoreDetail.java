package com.familyleague.scoring.entity;

import java.time.Instant;

import com.familyleague.common.entity.BaseEntity;
import com.familyleague.league.entity.Season;
import com.familyleague.team.entity.Team;
import com.familyleague.user.entity.User;

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
@Table(name = "season_score_details", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"season_id", "user_id", "team_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonScoreDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "predicted_position", nullable = false)
    private int predictedPosition;

    @Column(name = "actual_position")
    private Integer actualPosition;

    @Column(name = "points_earned")
    @Builder.Default
    private int pointsEarned = 0;

    @Column(name = "calculated_at")
    private Instant calculatedAt;
}
