package com.phper666.sforce.api.sdk.autoconfigure;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class AppConfig {
    private String consumerKey;
    private String consumerSecret;
    private String loginEndpoint = "https://login.salesforce.com";
    private boolean debug;
    private boolean debugLogBody;

    public String getConsumerKey() { return consumerKey; }
    public void setConsumerKey(String consumerKey) { this.consumerKey = consumerKey; }
    public String getConsumerSecret() { return consumerSecret; }
    public void setConsumerSecret(String consumerSecret) { this.consumerSecret = consumerSecret; }
    public String getLoginEndpoint() { return loginEndpoint; }
    public void setLoginEndpoint(String loginEndpoint) { this.loginEndpoint = loginEndpoint; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }
    public boolean isDebugLogBody() { return debugLogBody; }
    public void setDebugLogBody(boolean debugLogBody) { this.debugLogBody = debugLogBody; }
}
