package com.familyleague.match.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.common.dto.ApiResponse;
import com.familyleague.match.dto.MatchResultResponse;
import com.familyleague.match.dto.PublishResultRequest;
import com.familyleague.match.service.ResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/results")
@Tag(name = "Results", description = "Match and season result publication")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @PostMapping("/matches")
    @Operation(summary = "Publish match result (admin only)")
    public ResponseEntity<ApiResponse<MatchResultResponse>> publishMatchResult(
            @Valid @RequestBody PublishResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Result published", resultService.publishMatchResult(request)));
    }

    @GetMapping("/matches/{matchId}")
    @Operation(summary = "Get match result")
    public ResponseEntity<ApiResponse<MatchResultResponse>> getMatchResult(@PathVariable UUID matchId) {
        return ResponseEntity.ok(ApiResponse.success(resultService.getMatchResult(matchId)));
    }
}
