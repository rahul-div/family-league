package com.familyleague.appconfig.dto;

import com.familyleague.appconfig.entity.AppConfig;

public record AppConfigResponse(
        String key,
        String value,
        String description
) {
    public static AppConfigResponse from(AppConfig config) {
        return new AppConfigResponse(config.getKey(), config.getValue(), config.getDescription());
    }
}
