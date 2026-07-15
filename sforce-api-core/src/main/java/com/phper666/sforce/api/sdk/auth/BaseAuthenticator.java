package com.phper666.sforce.api.sdk.auth;

import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.Session;
import com.phper666.sforce.api.sdk.exception.AuthException;
import com.phper666.sforce.api.sdk.serialize.JsonSerializer;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.Map;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public abstract class BaseAuthenticator {
    protected final static OkHttpClient HTTP_CLIENT = new OkHttpClient();

    protected SdkConfig config;
    protected Session session;
    protected JsonSerializer jsonSerializer;

    public BaseAuthenticator(SdkConfig apiConfig) {
        this.config = apiConfig;
        jsonSerializer = config.getJsonSerializer();
    }

    public abstract Session login();

    public abstract Session refresh();

    protected void requestAccessToken(FormBody formBody, String domain) {
        var url = "%s/services/oauth2/token".formatted(domain);
        var request = new Request.Builder().url(url)
                .addHeader("Accept", "application/json").post(formBody).build();

        try (var response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                var responseBody = response.body();
                if (responseBody == null) {
                    throw new AuthException(0, "auth login flow failed: empty response body");
                }
                var body = responseBody.string();
                var resp = (Map<String, Object>) jsonSerializer.fromJson(body, Map.class);
                session = new Session((String) resp.get("access_token"), (String) resp.get("instance_url"));
            } else {
                var responseBody = response.body();
                var errorBody = responseBody != null ? responseBody.string() : "";
                throw new AuthException(response.code(), "auth login flow failed: " + errorBody);
            }
        } catch (IOException e) {
            throw new RuntimeException("auth login flow failed: " + e.getMessage(), e);
        }
    }
}
