package com.familyleague.scoring.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.scoring.dto.LeaderboardEntryResponse;
import com.familyleague.scoring.dto.MyRankResponse;

public interface LeaderboardService {

    PagedResponse<LeaderboardEntryResponse> getLeaderboard(UUID seasonId, Pageable pageable);

    MyRankResponse getMyRank(UUID seasonId);

    void recalculateLeaderboard(UUID seasonId);
}
