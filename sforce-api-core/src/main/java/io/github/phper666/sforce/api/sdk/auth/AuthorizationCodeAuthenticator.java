package io.github.phper666.sforce.api.sdk.auth;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;
import okhttp3.FormBody;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public class AuthorizationCodeAuthenticator extends BaseAuthenticator {

    public static String buildAuthUrl(String loginEndpoint, String clientId, String redirectUri, String state) {
        return "%s/services/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s"
                .formatted(loginEndpoint, clientId, redirectUri, state);
    }

    public static String buildAuthUrl(String clientId, String redirectUri, String state) {
        return buildAuthUrl("https://login.salesforce.com", clientId, redirectUri, state);
    }

    public AuthorizationCodeAuthenticator(SdkConfig apiConfig) {
        super(apiConfig);
    }

    @Override
    public Session login() {
        var formBody = new FormBody.Builder()
                .addEncoded("grant_type", "authorization_code")
                .addEncoded("client_id", config.getClientId())
                .addEncoded("client_secret", config.getClientSecret())
                .addEncoded("redirect_uri", config.getRedirectUri())
                .addEncoded("code", config.getAuthorizationCode())
                .build();
        requestAccessToken(formBody, config.getLoginEndpoint());
        return session;
    }

    @Override
    public Session refresh() {
        // todo: 目前业务暂时不需要 authorization code 的 refresh，所以暂时不刷新，以后需要的时候再实现
        return session;
    }
}
