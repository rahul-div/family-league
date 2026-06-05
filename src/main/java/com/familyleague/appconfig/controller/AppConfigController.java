package com.familyleague.appconfig.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.appconfig.dto.AppConfigResponse;
import com.familyleague.appconfig.dto.UpdateAppConfigRequest;
import com.familyleague.appconfig.service.AppConfigService;
import com.familyleague.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/config")
@Tag(name = "Configuration", description = "Runtime application configuration (admin only)")
public class AppConfigController {

    private final AppConfigService appConfigService;

    public AppConfigController(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @GetMapping
    @Operation(summary = "List all config entries")
    public ResponseEntity<ApiResponse<List<AppConfigResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(appConfigService.getAll()));
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update a config value")
    public ResponseEntity<ApiResponse<AppConfigResponse>> update(
            @PathVariable String key, @Valid @RequestBody UpdateAppConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Config updated", appConfigService.update(key, request)));
    }
}
