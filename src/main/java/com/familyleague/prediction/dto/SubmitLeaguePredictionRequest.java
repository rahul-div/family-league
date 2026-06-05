package com.familyleague.prediction.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SubmitLeaguePredictionRequest(
        @NotEmpty @Valid List<Entry> predictions
) {
    public record Entry(
            @NotNull UUID teamId,
            @NotNull @Min(1) Integer predictedPosition
    ) {}
}
