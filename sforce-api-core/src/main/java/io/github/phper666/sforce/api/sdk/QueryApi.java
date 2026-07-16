package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;

import io.github.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import io.github.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import io.github.phper666.sforce.api.sdk.internal.BaseApi;
import io.github.phper666.sforce.api.sdk.model.PageQueryResponse;
import io.github.phper666.sforce.api.sdk.model.ParameterizedSearchRequestBody;
import io.github.phper666.sforce.api.sdk.model.SOSLQueryResponse;
import io.github.phper666.sforce.api.sdk.serialize.CustomParameterizedType;
import io.github.phper666.sforce.api.sdk.serialize.JsonSerializer;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

class QueryApi extends BaseApi {
    QueryApi(SdkConfig config, Session session, OkHttpClient okHttpClient, JsonSerializer jsonSerializer, BaseAuthenticator authFlow) {
        super(config, session, okHttpClient, jsonSerializer, authFlow);
    }

    // ──────────────────────────────────────────────
    // SOQL Query
    // ──────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> PageQueryResponse<T> doSoqlQuery(String url, Class<T> tClass) {
        try {
            String body = executeGetBody(url, HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            Type type = new CustomParameterizedType(PageQueryResponse.class, new Class[]{tClass});
            return (PageQueryResponse<T>) jsonSerializer.fromJson(body, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> PageQueryResponse<T> soqlQuery(String query, Class<T> tClass) {
        return doSoqlQuery(soqlUri(query), tClass);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public int soqlQueryCount(String query) {
        try {
            String body = executeGetBody(soqlUri(query), HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            PageQueryResponse resp = (PageQueryResponse) jsonSerializer.fromJson(body, PageQueryResponse.class);
            return resp.getTotalSize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> PageQueryResponse<T> soqlQueryNext(String nextRecordsUrl, Class<T> tClass) {
        return doSoqlQuery(buildSoqlNextRequestUrl(nextRecordsUrl), tClass);
    }

    @SuppressWarnings("unchecked")
    public <T> PageQueryResponse<T> soqlQueryAll(String query, Class<T> tClass) {
        var response = soqlQuery(query, tClass);
        var allRecords = new ArrayList<>(response.getRecords());
        var next = response.getNextRecordsUrl();
        while (next != null) {
            var nextPage = soqlQueryNext(next, tClass);
            if (nextPage.getRecords() != null) {
                allRecords.addAll(nextPage.getRecords());
            }
            next = nextPage.getNextRecordsUrl();
        }
        var result = new PageQueryResponse<T>();
        result.setDone(true);
        result.setRecords(allRecords);
        result.setTotalSize(allRecords.size());
        return result;
    }

    // ──────────────────────────────────────────────
    // SOSL Search
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public SOSLQueryResponse soslQuery(String query) {
        try {
            String body = executeGetBody(soslUri(query), HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            return (SOSLQueryResponse) jsonSerializer.fromJson(body, SOSLQueryResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public SOSLQueryResponse soslQueryWithParameter(String query) {
        try {
            String body = executeGetBody(soslParameterizedSearchUri(query), HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            return (SOSLQueryResponse) jsonSerializer.fromJson(body, SOSLQueryResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public SOSLQueryResponse soslQueryWithParameter(ParameterizedSearchRequestBody searchRequestBody) {
        try {
            RequestBody rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(searchRequestBody));
            String body = executeGetBody(soslParameterizedSearchUri(), HttpMethod.POST.name(), rb, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            return (SOSLQueryResponse) jsonSerializer.fromJson(body, SOSLQueryResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ──────────────────────────────────────────────
    // Tooling API
    // ──────────────────────────────────────────────

    public <T> PageQueryResponse<T> toolingApiSoqlQuery(String query, Class<T> tClass) {
        return doSoqlQuery(toolingApiUriBase() + "/query?q=" + query, tClass);
    }

}
