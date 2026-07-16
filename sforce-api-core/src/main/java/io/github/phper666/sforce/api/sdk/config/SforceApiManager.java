package io.github.phper666.sforce.api.sdk.config;

import io.github.phper666.sforce.api.sdk.SforceApi;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for caching and retrieving SforceApi instances.
 *
 * @author Yuzhao.Li
 */
public class SforceApiManager {
    private static final Map<String, SforceApi> CACHE = new ConcurrentHashMap<>();

    public static SforceApi computeIfAbsent(String key, SdkConfig apiConfig) {
        return CACHE.computeIfAbsent(key, k -> new SforceApi(apiConfig));
    }

    public static void clear() {
        CACHE.clear();
    }
}
