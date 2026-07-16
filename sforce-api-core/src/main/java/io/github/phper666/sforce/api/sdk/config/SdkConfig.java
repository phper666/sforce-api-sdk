package io.github.phper666.sforce.api.sdk.config;

import io.github.phper666.sforce.api.sdk.serialize.GsonJsonSerializer;
import io.github.phper666.sforce.api.sdk.serialize.JsonSerializer;
import okhttp3.OkHttpClient;

/**
 * Configuration for Salesforce API SDK connection.
 *
 * @author Yuzhao.Li
 */
public class SdkConfig {
    private static final String DEFAULT_LOGIN_ENDPOINT = "https://login.salesforce.com";
    private String apiVersion = SdkTypes.ApiVersion.DEFAULT_VERSION.version();
    private String username;
    private String password;
    private String loginEndpoint = DEFAULT_LOGIN_ENDPOINT;
    private AuthFlow authFlow = AuthFlow.CLIENT_CREDENTIAL;
    private String clientId;
    private String clientSecret;
    private String authorizationCode;
    private String redirectUri;
    private JsonSerializer jsonSerializer;
    private String customObjectNamespace;
    private String customObjectSuffix = "__c";
    private String platformEventSuffix = "__e";
    private String accessToken;
    private boolean debug;
    private boolean debugLogBody;
    private int debugBodyMaxSize = 4096;
    private OkHttpClient okHttpClient;
    private boolean autoResolveCustomObjects = true;

    public SdkConfig() {
        jsonSerializer = GsonJsonSerializer.INSTANCE();
    }

    // All getters and setters with fluent setter pattern
    public String getApiVersion() { return apiVersion; }
    public SdkConfig setApiVersion(String apiVersion) { this.apiVersion = apiVersion; return this; }
    
    public String getUsername() { return username; }
    public SdkConfig setUsername(String username) { this.username = username; return this; }
    
    public String getPassword() { return password; }
    public SdkConfig setPassword(String password) { this.password = password; return this; }
    
    public String getLoginEndpoint() { return loginEndpoint; }
    public SdkConfig setLoginEndpoint(String loginEndpoint) { this.loginEndpoint = loginEndpoint; return this; }
    
    public AuthFlow getAuthFlow() { return authFlow; }
    public SdkConfig setAuthFlow(AuthFlow authFlow) { this.authFlow = authFlow; return this; }
    
    public String getClientId() { return clientId; }
    public SdkConfig setClientId(String clientId) { this.clientId = clientId; return this; }
    
    public String getClientSecret() { return clientSecret; }
    public SdkConfig setClientSecret(String clientSecret) { this.clientSecret = clientSecret; return this; }
    
    public String getAuthorizationCode() { return authorizationCode; }
    public SdkConfig setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; return this; }
    
    public String getRedirectUri() { return redirectUri; }
    public SdkConfig setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; return this; }
    
    public JsonSerializer getJsonSerializer() { return jsonSerializer; }
    public SdkConfig setJsonSerializer(JsonSerializer jsonSerializer) { this.jsonSerializer = jsonSerializer; return this; }
    
    public String getCustomObjectNamespace() { return customObjectNamespace; }
    public SdkConfig setCustomObjectNamespace(String customObjectNamespace) { this.customObjectNamespace = customObjectNamespace; return this; }
    
    public String getCustomObjectNamespacePrefix() {
        return customObjectNamespace != null && !customObjectNamespace.isEmpty() ? customObjectNamespace + "__" : "";
    }
    
    public String getCustomObjectSuffix() { return customObjectSuffix; }
    
    public String getPlatformEventSuffix() { return platformEventSuffix; }
    
    public String getAccessToken() { return accessToken; }
    public SdkConfig setAccessToken(String accessToken) { this.accessToken = accessToken; return this; }
    
    public boolean isDebug() { return debug; }
    public SdkConfig setDebug(boolean debug) { this.debug = debug; return this; }
    
    public boolean isDebugLogBody() { return debugLogBody; }
    public SdkConfig setDebugLogBody(boolean debugLogBody) { this.debugLogBody = debugLogBody; return this; }
    
    public int getDebugBodyMaxSize() { return debugBodyMaxSize; }
    public SdkConfig setDebugBodyMaxSize(int debugBodyMaxSize) { this.debugBodyMaxSize = debugBodyMaxSize; return this; }
    
    public OkHttpClient getOkHttpClient() { return okHttpClient; }
    public SdkConfig setOkHttpClient(OkHttpClient okHttpClient) { this.okHttpClient = okHttpClient; return this; }

    public boolean isAutoResolveCustomObjects() { return autoResolveCustomObjects; }
    public SdkConfig setAutoResolveCustomObjects(boolean autoResolveCustomObjects) { this.autoResolveCustomObjects = autoResolveCustomObjects; return this; }
}
