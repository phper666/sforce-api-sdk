package io.github.phper666.sforce.api.sdk.autoconfigure;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.SforceApi;
import io.github.phper666.sforce.api.sdk.config.SforceApiManager;

/**
 * Factory for creating and caching SforceApi instances per connected app.
 *
 * @author Yuzhao.Li
 */
public class SforceApiFactory {
    private final SforceApiProperties properties;

    public SforceApiFactory(SforceApiProperties properties) {
        this.properties = properties;
    }

    public SforceApi getForceClient(String appName) {
        return getForceClient(appName, null);
    }

    /**
     * Get or create a SforceApi for the given app and domain.
     *
     * @param appName connected app name in config
     * @param domain  Salesforce login domain (null to use default from AppConfig)
     * @return cached SforceApi instance
     */
    public SforceApi getForceClient(String appName, String domain) {
        AppConfig app = properties.getAppConfigByName(appName);
        String key = appName + ":" + (domain != null ? domain : app.getLoginEndpoint());
        return SforceApiManager.computeIfAbsent(key, buildApiConfig(app, domain));
    }

    private SdkConfig buildApiConfig(AppConfig app, String domain) {
        SdkConfig config = new SdkConfig();
        config.setClientId(app.getConsumerKey());
        config.setClientSecret(app.getConsumerSecret());
        config.setLoginEndpoint(domain != null ? domain : app.getLoginEndpoint());
        config.setCustomObjectNamespace(properties.getCustomObjectNamespace());
        config.setDebug(app.isDebug());
        config.setDebugLogBody(app.isDebugLogBody());

        return config;
    }
}
