package com.familyleague.scoring.service;

import java.util.UUID;

public interface ScoreCalculationService {

    void calculateMatchScores(UUID matchId);

    void calculateSeasonScores(UUID seasonId);
}
