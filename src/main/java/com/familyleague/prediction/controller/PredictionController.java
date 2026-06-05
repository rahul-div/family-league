package com.familyleague.prediction.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.common.dto.ApiResponse;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.prediction.dto.*;
import com.familyleague.prediction.service.PredictionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/predictions")
@Tag(name = "Predictions", description = "Match and league predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/matches/{matchId}")
    @Operation(summary = "Submit or update a match prediction")
    public ResponseEntity<ApiResponse<MatchPredictionResponse>> submitMatchPrediction(
            @PathVariable UUID matchId, @Valid @RequestBody SubmitMatchPredictionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Prediction submitted",
                predictionService.submitMatchPrediction(matchId, request)));
    }

    @GetMapping("/matches/{matchId}/me")
    @Operation(summary = "Get my prediction for a match")
    public ResponseEntity<ApiResponse<MatchPredictionResponse>> getMyMatchPrediction(@PathVariable UUID matchId) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.getMyMatchPrediction(matchId)));
    }

    @GetMapping("/matches/{matchId}")
    @Operation(summary = "Get all predictions for a match (visible after lock)")
    public ResponseEntity<ApiResponse<PagedResponse<MatchPredictionResponse>>> getAllMatchPredictions(
            @PathVariable UUID matchId, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.getAllMatchPredictions(matchId, pageable)));
    }

    @GetMapping("/matches/{matchId}/head-to-head")
    @Operation(summary = "Compare predictions with another user (after lock)")
    public ResponseEntity<ApiResponse<HeadToHeadResponse>> headToHead(
            @PathVariable UUID matchId, @RequestParam UUID opponentId) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.getHeadToHead(matchId, opponentId)));
    }

    @PostMapping("/seasons/{seasonId}/league")
    @Operation(summary = "Submit league predictions (all team positions)")
    public ResponseEntity<ApiResponse<List<LeaguePredictionResponse>>> submitLeaguePredictions(
            @PathVariable UUID seasonId, @Valid @RequestBody SubmitLeaguePredictionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("League predictions submitted",
                predictionService.submitLeaguePredictions(seasonId, request)));
    }

    @GetMapping("/seasons/{seasonId}/league/me")
    @Operation(summary = "Get my league predictions")
    public ResponseEntity<ApiResponse<List<LeaguePredictionResponse>>> getMyLeaguePredictions(
            @PathVariable UUID seasonId) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.getMyLeaguePredictions(seasonId)));
    }

    @GetMapping("/seasons/{seasonId}/league")
    @Operation(summary = "Get all league predictions (visible after lock)")
    public ResponseEntity<ApiResponse<List<LeaguePredictionResponse>>> getAllLeaguePredictions(
            @PathVariable UUID seasonId) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.getAllLeaguePredictions(seasonId)));
    }
}
