package com.familyleague.match.service;

import com.familyleague.match.dto.MatchResultResponse;
import com.familyleague.match.dto.PublishResultRequest;

import java.util.UUID;

public interface ResultService {

    MatchResultResponse publishMatchResult(PublishResultRequest request);

    MatchResultResponse getMatchResult(UUID matchId);
}
