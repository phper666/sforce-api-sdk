package com.phper666.sforce.api.sdk;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;

import com.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import com.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import com.phper666.sforce.api.sdk.internal.BaseApi;
import com.phper666.sforce.api.sdk.model.CompositeRequestBody;
import com.phper666.sforce.api.sdk.model.CompositeResponseBody;
import com.phper666.sforce.api.sdk.model.CompositeResponseError;
import com.phper666.sforce.api.sdk.serialize.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
class CompositeApi extends BaseApi {
    CompositeApi(SdkConfig config, Session session, OkHttpClient okHttpClient, JsonSerializer jsonSerializer, BaseAuthenticator authFlow) {
        super(config, session, okHttpClient, jsonSerializer, authFlow);
    }

    private static final int MAX_BATCH_GET_SIZE = 2000;
    // ──────────────────────────────────────────────
    // Composite API
    // ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public CompositeResponseBody compositeRequest(CompositeRequestBody compositeRequestBody) {
        return compositeRequest(compositeRequestBody, EMPTY_HEADERS);
    }

    @SuppressWarnings("unchecked")
    public CompositeResponseBody compositeRequest(CompositeRequestBody compositeRequestBody, Map<String, String> headers) {
        var rb = RequestBody.create(JSON_MEDIA, jsonSerializer.toJson(compositeRequestBody));
        try {
            var response = executeGetBody(buildCompositeUri(), HttpMethod.POST.name(), rb, headers, DEFAULT_TIME_OUT);
            var resp = (CompositeResponseBody) jsonSerializer.fromJson(response, CompositeResponseBody.class);
            resp.getCompositeResponse().forEach(cr -> {
                if (!cr.isSuccessful()) {
                    cr.setCompositeResponseErrors(bodyToCompositeResponseErrors(cr.getBody()));
                    cr.setDuplicateValueError(cr.getCompositeResponseErrors().stream().anyMatch(e -> "DUPLICATE_VALUE".equals(e.getErrorCode())));
                }
            });
            return resp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<CompositeResponseError> bodyToCompositeResponseErrors(Object body) {
        if (body == null || "{}".equals(body)) {
            return new ArrayList<>();
        }
        return jsonSerializer.fromJsonList(jsonSerializer.toJson(body), CompositeResponseError.class);
    }

    // ──────────────────────────────────────────────
    // Composite URL helpers (public, used by builder)
    // ──────────────────────────────────────────────

    public String getCompositeObjectUrl(String objectType) {
        return "/services/data/" + config.getApiVersion() + "/sobjects/" + getCustomObjectType(objectType) + "/";
    }

    public String getCompositeSObjectUrl(String objectType) {
        return "/services/data/" + config.getApiVersion() + "/sobjects/" + objectType;
    }

    public String getCompositeExternalFieldCObjectUrl(String objectType, String externalIdField, String externalIdValue) {
        return "/services/data/" + config.getApiVersion() + "/sobjects/" + getCustomObjectType(objectType) + "/" + externalIdField + "/" + URLEncoder.encode(externalIdValue, StandardCharsets.UTF_8);
    }

}
