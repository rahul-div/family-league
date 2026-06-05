package com.familyleague.league.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.familyleague.common.entity.BaseEntity;
import com.familyleague.match.entity.Match;
import com.familyleague.team.entity.SeasonTeam;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "seasons")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "season_number")
    private Integer seasonNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private SeasonStatus status = SeasonStatus.UPCOMING;

    @Column(name = "league_prediction_lock_hours")
    @Builder.Default
    private Integer leaguePredictionLockHours = 4;

    @Column(name = "match_prediction_lock_hours")
    @Builder.Default
    private Integer matchPredictionLockHours = 1;

    @Column(name = "first_match_at")
    private Instant firstMatchAt;

    @Column(name = "prediction_locked_at")
    private Instant predictionLockedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SeasonTeam> seasonTeams = new ArrayList<>();

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Match> matches = new ArrayList<>();

    public boolean isClosed() {
        return this.status == SeasonStatus.CLOSED;
    }

    public boolean isPredictionLocked() {
        return this.status == SeasonStatus.PREDICTION_LOCKED
                || this.status == SeasonStatus.IN_PROGRESS
                || this.status == SeasonStatus.COMPLETED
                || this.status == SeasonStatus.CLOSED;
    }
}
