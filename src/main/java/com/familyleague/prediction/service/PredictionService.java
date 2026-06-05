package com.familyleague.prediction.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.prediction.dto.HeadToHeadResponse;
import com.familyleague.prediction.dto.LeaguePredictionResponse;
import com.familyleague.prediction.dto.MatchPredictionResponse;
import com.familyleague.prediction.dto.SubmitLeaguePredictionRequest;
import com.familyleague.prediction.dto.SubmitMatchPredictionRequest;

public interface PredictionService {

    MatchPredictionResponse submitMatchPrediction(UUID matchId, SubmitMatchPredictionRequest request);

    MatchPredictionResponse getMyMatchPrediction(UUID matchId);

    PagedResponse<MatchPredictionResponse> getAllMatchPredictions(UUID matchId, Pageable pageable);

    HeadToHeadResponse getHeadToHead(UUID matchId, UUID opponentId);

    List<LeaguePredictionResponse> submitLeaguePredictions(UUID seasonId, SubmitLeaguePredictionRequest request);

    List<LeaguePredictionResponse> getMyLeaguePredictions(UUID seasonId);

    List<LeaguePredictionResponse> getAllLeaguePredictions(UUID seasonId);
}
