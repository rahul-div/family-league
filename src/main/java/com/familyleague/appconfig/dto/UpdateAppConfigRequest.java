package com.familyleague.appconfig.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAppConfigRequest(
        @NotBlank String value
) {}
