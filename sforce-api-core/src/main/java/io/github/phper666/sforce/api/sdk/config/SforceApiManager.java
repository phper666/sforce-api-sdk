package io.github.phper666.sforce.api.sdk.config;

import io.github.phper666.sforce.api.sdk.SforceApi;
import io.github.phper666.sforce.api.sdk.config.SdkConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
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
