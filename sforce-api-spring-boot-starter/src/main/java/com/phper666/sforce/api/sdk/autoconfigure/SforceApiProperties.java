package com.phper666.sforce.api.sdk.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@ConfigurationProperties(prefix = "sforce.api")
public class SforceApiProperties {
    private String customObjectNamespace;
    private Map<String, AppConfig> connectedApps = new LinkedHashMap<>();

    public String getCustomObjectNamespace() { return customObjectNamespace; }
    public void setCustomObjectNamespace(String customObjectNamespace) { this.customObjectNamespace = customObjectNamespace; }
    public Map<String, AppConfig> getConnectedApps() { return connectedApps; }
    public void setConnectedApps(Map<String, AppConfig> connectedApps) { this.connectedApps = connectedApps; }

    public AppConfig getAppConfigByName(String appName) {
        AppConfig app = connectedApps.get(appName);
        if (app == null) {
            throw new IllegalArgumentException("Can't find connected app: " + appName);
        }
        return app;
    }
}
