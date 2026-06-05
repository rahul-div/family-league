package com.familyleague.scoring.entity;

import java.time.Instant;

import com.familyleague.common.entity.BaseEntity;
import com.familyleague.league.entity.Season;
import com.familyleague.match.entity.Match;
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
@Table(name = "match_score_details", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"match_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchScoreDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(name = "winner_correct")
    @Builder.Default
    private boolean winnerCorrect = false;

    @Column(name = "toss_winner_correct")
    @Builder.Default
    private boolean tossWinnerCorrect = false;

    @Column(name = "potm_correct")
    @Builder.Default
    private boolean potmCorrect = false;

    @Column(name = "total_match_points")
    @Builder.Default
    private int totalMatchPoints = 0;

    @Column(name = "calculated_at")
    private Instant calculatedAt;
}
