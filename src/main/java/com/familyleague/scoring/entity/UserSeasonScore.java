package com.familyleague.scoring.entity;

import java.time.Instant;

import com.familyleague.common.entity.BaseEntity;
import com.familyleague.league.entity.Season;
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
@Table(name = "user_season_scores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "season_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSeasonScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(name = "match_points")
    @Builder.Default
    private int matchPoints = 0;

    @Column(name = "season_prediction_points")
    @Builder.Default
    private int seasonPredictionPoints = 0;

    @Column(name = "total_points")
    @Builder.Default
    private int totalPoints = 0;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "last_calculated_at")
    private Instant lastCalculatedAt;
}
