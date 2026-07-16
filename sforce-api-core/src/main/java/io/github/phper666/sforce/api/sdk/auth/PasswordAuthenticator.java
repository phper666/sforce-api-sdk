package io.github.phper666.sforce.api.sdk.auth;

import io.github.phper666.sforce.api.sdk.config.SdkConfig;
import io.github.phper666.sforce.api.sdk.config.Session;
import okhttp3.FormBody;

/**
 * Authenticator for OAuth 2.0 password flow.
 *
 * @author Yuzhao.Li
 */
public class PasswordAuthenticator extends BaseAuthenticator {

    public PasswordAuthenticator(SdkConfig apiConfig) {
        super(apiConfig);
    }

    @Override
    public Session login() {
        var formBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("client_id", config.getClientId())
                .add("client_secret", config.getClientSecret())
                .add("username", config.getUsername())
                .add("password", config.getPassword())
                .build();

        requestAccessToken(formBody, config.getLoginEndpoint());
        return session;
    }

    @Override
    public Session refresh() {
        return login();
    }
}
