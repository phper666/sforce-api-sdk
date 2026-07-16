package io.github.phper666.sforce.api.sdk.autoconfigure;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.SforceApi;
import io.github.phper666.sforce.api.sdk.config.SforceApiManager;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class SforceApiFactory {
    private final SforceApiProperties properties;

    public SforceApiFactory(SforceApiProperties properties) {
        this.properties = properties;
    }

    public SforceApi getForceClient(String appName) {
        return getForceClient(appName, null);
    }

    public SforceApi getForceClient(String appName, String domain) {
        AppConfig app = properties.getAppConfigByName(appName);
        String key = appName + ":" + (domain != null ? domain : app.getLoginEndpoint());
        return SforceApiManager.computeIfAbsent(key, buildApiConfig(app, domain));
    }

    private SdkConfig buildApiConfig(AppConfig app, String domain) {
        SdkConfig config = new SdkConfig();
        config.setClientId(app.getConsumerKey());
        config.setClientSecret(app.getConsumerSecret());
        config.setLoginEndpoint(app.getLoginEndpoint());
        config.setLoginEndpoint(domain);
        config.setCustomObjectNamespace(properties.getCustomObjectNamespace());
        config.setDebug(app.isDebug());
        config.setDebugLogBody(app.isDebugLogBody());

        return config;
    }
}
