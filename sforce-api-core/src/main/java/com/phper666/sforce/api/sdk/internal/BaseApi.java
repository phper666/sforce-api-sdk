package com.phper666.sforce.api.sdk.internal;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;
import com.phper666.sforce.api.sdk.config.Session;
import com.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import com.phper666.sforce.api.sdk.exception.ApiException;
import com.phper666.sforce.api.sdk.serialize.JsonSerializer;
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

    protected String getCustomObjectType(String objectType) {
        return config.getCustomObjectNamespacePrefix() + objectType + config.getCustomObjectSuffix();
    }

    protected String getPlatformEventType(String eventType) {
        return config.getCustomObjectNamespacePrefix() + eventType + config.getPlatformEventSuffix();
    }

    /**
     * Resolves object type name for auto-namespace handling.
     * If autoResolveCustomObjects is enabled:
     * - objectType ending with __c → prepend namespace prefix
     * - objectType ending with __e → prepend namespace prefix
     * - otherwise → return as-is
     * If disabled → return as-is
     */
    protected String resolveType(String objectType) {
        if (!config.isAutoResolveCustomObjects()) {
            return objectType;
        }
        if (objectType.endsWith(config.getCustomObjectSuffix())) {
            return config.getCustomObjectNamespacePrefix() + objectType;
        }
        if (objectType.endsWith(config.getPlatformEventSuffix())) {
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
