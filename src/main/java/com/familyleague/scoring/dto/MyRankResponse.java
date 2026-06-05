package com.familyleague.scoring.dto;

import java.util.List;

public record MyRankResponse(
        LeaderboardEntryResponse entry,
        List<MatchScoreDetailResponse> matchScores,
        List<SeasonScoreDetailResponse> seasonScores
) {}
