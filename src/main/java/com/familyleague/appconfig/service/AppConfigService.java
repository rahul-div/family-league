package com.familyleague.appconfig.service;

import java.util.List;

import com.familyleague.appconfig.dto.AppConfigResponse;
import com.familyleague.appconfig.dto.UpdateAppConfigRequest;

public interface AppConfigService {

    String getValue(String key);

    int getIntValue(String key);

    List<AppConfigResponse> getAll();

    AppConfigResponse update(String key, UpdateAppConfigRequest request);
}
