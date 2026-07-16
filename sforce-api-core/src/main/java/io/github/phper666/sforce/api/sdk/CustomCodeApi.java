package io.github.phper666.sforce.api.sdk;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;

import io.github.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;
import io.github.phper666.sforce.api.sdk.auth.BaseAuthenticator;
import io.github.phper666.sforce.api.sdk.internal.BaseApi;
import io.github.phper666.sforce.api.sdk.model.ListInvocableActionResult;
import io.github.phper666.sforce.api.sdk.serialize.JsonSerializer;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.IOException;

class CustomCodeApi extends BaseApi {
    CustomCodeApi(SdkConfig config, Session session, OkHttpClient okHttpClient, JsonSerializer jsonSerializer, BaseAuthenticator authFlow) {
        super(config, session, okHttpClient, jsonSerializer, authFlow);
    }

    // ──────────────────────────────────────────────
    // Apex
    // ──────────────────────────────────────────────

    public String runApex(String apexUriPostfix, HttpMethod method, Object body) {
        String json = body == null ? null : (body instanceof String ? (String) body : jsonSerializer.toJson(body));
        RequestBody rb = json != null ? RequestBody.create(JSON_MEDIA, json) : EMPTY_BODY;
        try {
            return executeGetBody(apexUri(apexUriPostfix), method.name(), rb, EMPTY_HEADERS, DEFAULT_TIME_OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ──────────────────────────────────────────────
    // Invocable Actions
    // ──────────────────────────────────────────────

    public String invokeInvocableActions(String url, Object inputs) {
        String json = inputs == null ? null : (inputs instanceof String ? (String) inputs : jsonSerializer.toJson(inputs));
        RequestBody rb = json != null ? RequestBody.create(JSON_MEDIA, json) : EMPTY_BODY;
        try {
            return executeGetBody(invocableActionUrl(url), HttpMethod.POST.name(), rb, EMPTY_HEADERS, DEFAULT_TIME_OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getInvocableActionSchema(String url) {
        try {
            return executeGetBody(invocableActionUrl(url), HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ListInvocableActionResult listStandardInvocableActions() {
        String url = "/services/data/" + config.getApiVersion() + "/actions/standard";
        try {
            String response = executeGetBody(invocableActionUrl(url), HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            return (ListInvocableActionResult) jsonSerializer.fromJson(response, ListInvocableActionResult.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getQuickAction(String objApiName, String quickActionName) {
        String url = "/services/data/" + config.getApiVersion() + "/sobjects/" + objApiName + "/quickActions/" + quickActionName;
        try {
            return executeGetBody(invocableActionUrl(url), HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public ListInvocableActionResult listCustomInvocableActions(CustomCodeApi.CustomActionType type) {
        String url = "/services/data/" + config.getApiVersion() + "/actions/custom/" + type.value();
        try {
            String response = executeGetBody(invocableActionUrl(url), HttpMethod.GET.name(), EMPTY_BODY, EMPTY_HEADERS, DEFAULT_TIME_OUT);
            return (ListInvocableActionResult) jsonSerializer.fromJson(response, ListInvocableActionResult.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public enum CustomActionType {
        FLOW("flow"),
        APEX("apex"),
        GENERATE_PROMPT_RESPONSE("generatePromptResponse");

        private final String value;
        CustomActionType(String value) { this.value = value; }
        public String value() { return value; }
    }
}
