package io.github.phper666.sforce.api.sdk.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration properties for Salesforce API connections.
 *
 * @author Yuzhao.Li
 */
@ConfigurationProperties(prefix = "sforce.api")
public class SforceApiProperties {
    private String customObjectNamespace;
    private Map<String, AppConfig> connectedApps = new LinkedHashMap<>();

    public String getCustomObjectNamespace() { return customObjectNamespace; }
    public void setCustomObjectNamespace(String customObjectNamespace) { this.customObjectNamespace = customObjectNamespace; }
    public Map<String, AppConfig> getConnectedApps() { return connectedApps; }
    public void setConnectedApps(Map<String, AppConfig> connectedApps) { this.connectedApps = connectedApps; }

    /**
     * Look up an AppConfig by name.
     *
     * @param appName connected app name
     * @return matching AppConfig
     * @throws IllegalArgumentException if not found
     */
    public AppConfig getAppConfigByName(String appName) {
        AppConfig app = connectedApps.get(appName);
        if (app == null) {
            throw new IllegalArgumentException("Can't find connected app: " + appName);
        }
        return app;
    }
}
