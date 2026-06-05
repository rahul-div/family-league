package com.familyleague.appconfig.service;

import java.time.Instant;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.appconfig.dto.AppConfigResponse;
import com.familyleague.appconfig.dto.UpdateAppConfigRequest;
import com.familyleague.appconfig.entity.AppConfig;
import com.familyleague.appconfig.repository.AppConfigRepository;
import com.familyleague.common.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class AppConfigServiceImpl implements AppConfigService {

    private final AppConfigRepository appConfigRepository;

    public AppConfigServiceImpl(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    @Override
    @Cacheable(value = "app-config", key = "#key")
    public String getValue(String key) {
        return appConfigRepository.findById(key)
                .map(AppConfig::getValue)
                .orElseThrow(() -> ResourceNotFoundException.of("AppConfig", key));
    }

    @Override
    public int getIntValue(String key) {
        return Integer.parseInt(getValue(key));
    }

    @Override
    public List<AppConfigResponse> getAll() {
        return appConfigRepository.findAll().stream()
                .map(AppConfigResponse::from)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "app-config", key = "#key")
    public AppConfigResponse update(String key, UpdateAppConfigRequest request) {
        AppConfig config = appConfigRepository.findById(key)
                .orElseThrow(() -> ResourceNotFoundException.of("AppConfig", key));
        config.setValue(request.value());
        config.setUpdatedAt(Instant.now());
        return AppConfigResponse.from(appConfigRepository.save(config));
    }
}
