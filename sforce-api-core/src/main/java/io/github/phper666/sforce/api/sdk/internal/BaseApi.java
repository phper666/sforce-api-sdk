package io.github.phper666.sforce.api.sdk.internal;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import io.github.phper666.sforce.api.sdk.config.Session;
import io.github.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import io.github.phper666.sforce.api.sdk.exception.ApiException;
import io.github.phper666.sforce.api.sdk.serialize.JsonSerializer;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public abstract class BaseApi {
    protected OkHttpClient okHttpClient;
    public Session session;
    public SdkConfig config;
    protected JsonSerializer jsonSerializer;
    protected BaseAuthenticator authFlow;

    protected static final MediaType JSON_MEDIA = MediaType.parse("application/json");
    protected static final MediaType TEXT_CSV_MEDIA = MediaType.parse("text/csv");
    protected static final RequestBody EMPTY_BODY = null;
    protected static final TimeoutSettings DEFAULT_TIME_OUT = new TimeoutSettings();
    protected static final Map<String, String> EMPTY_HEADERS = null;

    public BaseApi(SdkConfig config, Session session, OkHttpClient okHttpClient, JsonSerializer jsonSerializer, BaseAuthenticator authFlow) {
        this.config = config;
        this.session = session;
        this.okHttpClient = okHttpClient;
        this.jsonSerializer = jsonSerializer;
        this.authFlow = authFlow;
    }

    protected String uriBase(String objectType) {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/sobjects/" + objectType;
    }

    protected String apexUri(String apexUriPostfix) {
        String ns = config.getCustomObjectNamespace();
        String prefix = (ns != null && !ns.isEmpty()) ? "/" + ns : "";
        return session.apiEndpoint() + "/services/apexrest" + prefix + apexUriPostfix;
    }

    protected String invocableActionUrl(String url) {
        return session.apiEndpoint() + url;
    }

    protected String soqlUri(String query) {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/query?q=" + query;
    }

    protected String soslUri(String query) {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/search?q=" + query;
    }

    protected String soslParameterizedSearchUri(String query) {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/parameterizedSearch?" + query;
    }

    protected String soslParameterizedSearchUri() {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/parameterizedSearch";
    }

    protected String buildCompositeObjectUri() {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/composite/sobjects";
    }

    protected String buildCompositeUri() {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/composite";
    }

    protected String buildSoqlNextRequestUrl(String nextRecordsUrl) {
        return session.apiEndpoint() + nextRecordsUrl;
    }

    protected String toolingApiUriBase() {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/tooling";
    }

    protected String bulkApiUriBase() {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/jobs/ingest";
    }

    protected String chatterFileUploadUrl() {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/connect/files/users/me";
    }

    protected String getFileContentUrl(String fileId) {
        return session.apiEndpoint() + "/services/data/" + config.getApiVersion() + "/connect/files/" + fileId + "/content";
    }

    /**
     * Returns true when the name already carries a namespace prefix.
     * <p>
     * Detected by checking for {@code __} before the trailing suffix.
     * This allows callers to pass either short names ({@code MyObj__c})
     * or fully-qualified names ({@code myns__MyObj__c}) — the latter
     * skips the global namespace prefix to avoid double-prefixing.
     */
    private boolean hasNamespacePrefix(String name, String suffix) {
        String base = suffix != null && name.endsWith(suffix)
                ? name.substring(0, name.length() - suffix.length())
                : name;
        return base.contains("__");
    }

    /**
     * Builds the full API name for a custom object.
     * <p>
     * Accepts the name with or without the {@code __c} suffix, and with or
     * without a namespace prefix:
     * <pre>
     *   getCustomObjectType("MyObj")         → "myns__MyObj__c"  (global ns)
     *   getCustomObjectType("otherns__MyObj")→ "otherns__MyObj__c" (auto-detect)
     *   getCustomObjectType("otherns__MyObj__c")→ "otherns__MyObj__c"
     * </pre>
     */
    protected String getCustomObjectType(String objectType) {
        String suffix = config.getCustomObjectSuffix();
        String base = objectType.endsWith(suffix)
                ? objectType.substring(0, objectType.length() - suffix.length())
                : objectType;
        if (hasNamespacePrefix(base, null)) {
            return base + suffix;
        }
        return config.getCustomObjectNamespacePrefix() + base + suffix;
    }

    /**
     * Builds the full API name for a platform event.
     * Same auto-detection as {@link #getCustomObjectType}.
     */
    protected String getPlatformEventType(String eventType) {
        String suffix = config.getPlatformEventSuffix();
        String base = eventType.endsWith(suffix)
                ? eventType.substring(0, eventType.length() - suffix.length())
                : eventType;
        if (hasNamespacePrefix(base, null)) {
            return base + suffix;
        }
        return config.getCustomObjectNamespacePrefix() + base + suffix;
    }

    /**
     * Resolves object type name for auto-namespace handling.
     * <p>
     * If {@code autoResolveCustomObjects} is enabled and the name ends in
     * {@code __c} or {@code __e} <em>without</em> an existing namespace
     * prefix, the global namespace is prepended. Names that already carry a
     * prefix (e.g. {@code otherns__MyObj__c}) are returned unchanged.
     */
    protected String resolveType(String objectType) {
        if (!config.isAutoResolveCustomObjects()) {
            return objectType;
        }
        if (objectType.endsWith(config.getCustomObjectSuffix())
                && !hasNamespacePrefix(objectType, config.getCustomObjectSuffix())) {
            return config.getCustomObjectNamespacePrefix() + objectType;
        }
        if (objectType.endsWith(config.getPlatformEventSuffix())
                && !hasNamespacePrefix(objectType, config.getPlatformEventSuffix())) {
            return config.getCustomObjectNamespacePrefix() + objectType;
        }
        return objectType;
    }

    public String executeGetBody(String url, String method, RequestBody requestBody, Map<String, String> headers, TimeoutSettings timeOutConfig) throws IOException {
        return execute(url, method, requestBody, headers, timeOutConfig).body().string();
    }

    protected Response execute(String url, String method, RequestBody requestBody, Map<String, String> headers, TimeoutSettings timeOutConfig) throws IOException {
        Request request = buildRequest(url, method, requestBody, headers, timeOutConfig);
        Response response = okHttpClient.newCall(request).execute();
        if (response.code() == 401) {
            session = authFlow.refresh();
            request = buildRequest(url, method, requestBody, headers, timeOutConfig);
            response = okHttpClient.newCall(request).execute();
        }
        if (!response.isSuccessful()) {
            String body = response.body() != null ? response.body().string() : "";
            throw new ApiException(response.code(), body, response.request().method(), response.request().url().toString());
        }
        return response;
    }

    protected Request buildRequest(String url, String method, RequestBody requestBody, Map<String, String> headers, TimeoutSettings timeOutConfig) {
        var builder = new Request.Builder()
                .url(url)
                .method(method, requestBody)
                .addHeader("Authorization", "Bearer " + session.accessToken())
                .tag(timeOutConfig);
        if (headers != null) {
            headers.forEach(builder::addHeader);
        }
        return builder.build();
    }

    protected static boolean isSuccess(int httpStatusCode) {
        return httpStatusCode >= 200 && httpStatusCode < 300;
    }

    protected static RuntimeException requestFailed(String method, IOException e) {
        return new RuntimeException("%s failed: %s".formatted(method, e.getMessage()), e);
    }

}
