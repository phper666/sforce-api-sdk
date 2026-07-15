package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;
import com.phper666.sforce.api.sdk.config.SdkTypes;

import com.phper666.sforce.api.sdk.auth.AuthenticatorFactory;
import com.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import com.phper666.sforce.api.sdk.internal.DebugInterceptor;
import com.phper666.sforce.api.sdk.internal.TimeoutInterceptor;
import com.phper666.sforce.api.sdk.serialize.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.io.IOException;

@Slf4j
public class SforceApi {
    private SobjectApi sobjectApi;
    private QueryApi queryApi;
    private CompositeApi compositeApi;
    private BulkApi bulkApi;
    private FileApi fileApi;
    private CustomCodeApi customCodeApi;

    public SforceApi(SdkConfig apiConfig) {
        OkHttpClient okHttpClient = initOkHttpClient(apiConfig);
        JsonSerializer jsonSerializer = apiConfig.getJsonSerializer();
        BaseAuthenticator authFlow = AuthenticatorFactory.create(apiConfig);
        Session session = authFlow.login();
        initApis(apiConfig, session, okHttpClient, jsonSerializer, authFlow);
    }

    private static OkHttpClient initOkHttpClient(SdkConfig config) {
        if (config.getOkHttpClient() != null) {
            return config.getOkHttpClient();
        }
        var builder = new OkHttpClient.Builder();
        if (config.isDebug()) {
            builder.addInterceptor(new DebugInterceptor(config.isDebugLogBody(), config.getDebugBodyMaxSize()));
        }
        builder.addNetworkInterceptor(new TimeoutInterceptor());
        return builder.build();
    }

    private void initApis(SdkConfig config, Session session, OkHttpClient okHttpClient, JsonSerializer jsonSerializer, BaseAuthenticator authFlow) {
        this.sobjectApi = new SobjectApi(config, session, okHttpClient, jsonSerializer, authFlow);
        this.queryApi = new QueryApi(config, session, okHttpClient, jsonSerializer, authFlow);
        this.compositeApi = new CompositeApi(config, session, okHttpClient, jsonSerializer, authFlow);
        this.bulkApi = new BulkApi(config, session, okHttpClient, jsonSerializer, authFlow);
        this.fileApi = new FileApi(config, session, okHttpClient, jsonSerializer, authFlow);
        this.customCodeApi = new CustomCodeApi(config, session, okHttpClient, jsonSerializer, authFlow);
    }

    public SobjectApi sobject() { return sobjectApi; }
    public QueryApi query() { return queryApi; }
    public CompositeApi composite() { return compositeApi; }
    public BulkApi bulk() { return bulkApi; }
    public FileApi file() { return fileApi; }
    public CustomCodeApi customCode() { return customCodeApi; }

    public String getAccessToken() {
        return sobjectApi.session.accessToken();
    }

    public String getApiEndpoint() {
        return sobjectApi.session.apiEndpoint();
    }

    @SuppressWarnings("unchecked")
    public <T> T getUserInfo(Class<T> tClass) {
        String url = sobjectApi.session.apiEndpoint() + "/services/oauth2/userinfo?format=json";
        try {
            String body = sobjectApi.executeGetBody(url, SdkTypes.HttpMethod.GET.name(), null, null, new SdkTypes.TimeoutSettings());
            return (T) sobjectApi.config.getJsonSerializer().fromJson(body, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
